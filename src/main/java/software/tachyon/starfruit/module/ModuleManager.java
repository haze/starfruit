package software.tachyon.starfruit.module;

import net.engio.mbassy.bus.MBassador;
import net.engio.mbassy.listener.Handler;
import net.engio.mbassy.listener.Listener;
import net.engio.mbassy.listener.References;
import net.minecraft.network.listener.ServerPlayPacketListener;
import net.minecraft.server.network.packet.ChatMessageC2SPacket;
import software.tachyon.starfruit.module.event.api.Event;
import software.tachyon.starfruit.module.event.KeyPressEvent;
import software.tachyon.starfruit.module.event.SendPacketEvent;
import software.tachyon.starfruit.module.movement.Sprint;
import software.tachyon.starfruit.module.render.Luminance;
import software.tachyon.starfruit.module.variable.Variable;
import software.tachyon.starfruit.utility.HexShift;
import software.tachyon.starfruit.StarfruitMod;
import software.tachyon.starfruit.command.Parser;

import static org.lwjgl.glfw.GLFW.*;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.HashMap;

import java.util.Optional;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.Map;

@Listener(references = References.Strong)
public class ModuleManager {

    private MBassador<Event> bus = null;
    private static ModuleManager INSTANCE = null;

    private Map<Integer, StatefulModule> modules = null;
    private SortedSet<StatefulModule> display = null;
    private Map<String, StatefulModule> moduleNameCache = null;
    private Map<StatefulModule, Map<String, Variable<?>>> variableCache = null;

    private final Parser commandParser;

    private ExecutorService threadPool = null;

    public ModuleManager() {
        this.bus = new MBassador<Event>();
        this.commandParser = new Parser();

        this.modules = new HashMap<>();
        this.display = new TreeSet<>(
                (StatefulModule m1, StatefulModule m2) -> m1.getInfo().name.compareTo(m2.getInfo().name));
        this.moduleNameCache = new HashMap<>();
        this.variableCache = new HashMap<>();
        this.threadPool = Executors.newCachedThreadPool();

        this.bus.subscribe(this);
        this.register(new Sprint(GLFW_KEY_C));
        this.register(new Luminance(GLFW_KEY_B));
    }

    public void registerVariables(StatefulModule mod) {
        for (Field field : mod.getClass().getFields()) {
            if (Variable.class.isAssignableFrom(field.getType())) {
                final String variableName = field.getName();
                Map<String, Variable<?>> varMap = this.variableCache.get(mod);
                if (varMap == null) {
                    varMap = new HashMap<>();
                }
                if (!varMap.containsKey(variableName)) {
                    try {
                        final Variable<?> variable = (Variable<?>) field.get(mod);
                        variable.setFieldName(field.getName());
                        varMap.put(field.getName().toLowerCase(), variable);
                        StarfruitMod.consoleInfo("%s, %d\n", varMap.toString(), varMap.size());
                        this.variableCache.put(mod, varMap);
                    } catch (IllegalAccessException iae) {
                        StarfruitMod.consoleInfo("Found variable with bad access (%s) in %s.\n", variableName,
                                mod.getInfo().name);
                    }
                } else {
                    StarfruitMod.consoleInfo("Found variable redefinition (%s) in %s.\n", variableName,
                            mod.getInfo().name);
                }

                StarfruitMod.consoleInfo("Found %s variable %s!\n", mod.getInfo().name, field.getName());
            }
        }
    }

    public void register(StatefulModule mod) {
        this.modules.put(mod.getKeyCode(), mod);
        this.moduleNameCache.put(mod.getInfo().name.toLowerCase(), mod);
        // register variables
        this.registerVariables(mod);
    }

    @Handler
    public void onKeyPress(KeyPressEvent event) {
        Optional.ofNullable(this.modules.get(event.getKeyPressed())).ifPresent((module) -> {
            final boolean newState = !module.getState();
            module.setState(newState);

            if (newState) {
                module.onEnable();
                this.display.add(module);
            } else {
                this.display.remove(module);
                module.onDisable();
            }
        });
    }

    boolean isInternalCommand(String input) {
        final String lower = input.toLowerCase();
        return lower.startsWith("set") || lower.startsWith(".");
    }

    @Handler
    @SuppressWarnings("unchecked")
    public void onChatPacket(SendPacketEvent<ServerPlayPacketListener> event) {
        if (event.getPacket() instanceof ChatMessageC2SPacket) {
            final ChatMessageC2SPacket packet = (ChatMessageC2SPacket) event.getPacket();
            final String chatMessage = packet.getChatMessage();
            event.setCancelled(this.isInternalCommand(chatMessage));
            this.commandParser.parseSetCommand(chatMessage).ifPresent(result -> {
                StatefulModule mod = null;
                if (result.literal) {
                    mod = this.moduleNameCache.get(result.moduleName);
                } else {
                    for (Map.Entry<String, StatefulModule> entry : this.moduleNameCache.entrySet()) {
                        if (entry.getKey().startsWith(result.moduleName)) {
                            mod = entry.getValue();
                            break;
                        }
                    }
                }
                if (mod == null) {
                    StarfruitMod.info("No module named %s found", result.moduleName);
                    return;
                }
                final Map<String, Variable<?>> modVariableCache = this.variableCache.get(mod);
                if (modVariableCache == null) {
                    StarfruitMod.info("%s has no configurable variables", mod.getInfo().hexDisplayString());
                    return;
                }
                Variable<?> variable = null;
                if (result.literal) {
                    variable = modVariableCache.get(result.valueName.toLowerCase());
                } else {
                    for (Map.Entry<String, Variable<?>> entry : modVariableCache.entrySet()) {
                        if (entry.getKey().startsWith(result.valueName)) {
                            variable = entry.getValue();
                            break;
                        }
                    }
                }
                if (variable == null) {
                    StarfruitMod.info("%s does not have variable %s", mod.getInfo().hexDisplayString(),
                            result.valueName);
                    return;
                }

                final Class<?> variableType = (Class<?>) ((ParameterizedType) variable.getClass()
                        .getGenericSuperclass()).getActualTypeArguments()[0];
                final boolean isInt = variableType.isAssignableFrom(Integer.class);
                final boolean isDouble = variableType.isAssignableFrom(Double.class);
                final boolean isBoolean = variableType.isAssignableFrom(Boolean.class);
                final boolean isString = variableType.isAssignableFrom(String.class);

                final String output, oldValueDisplay;

                if (isInt) {
                    final Variable.Int typedVar = (Variable.Int) variable;
                    oldValueDisplay = typedVar.getDisplay();
                    typedVar.set((Integer) result.value);
                    output = typedVar.getDisplay();
                } else if (isDouble) {
                    final Variable.Dbl typedVar = (Variable.Dbl) variable;
                    oldValueDisplay = typedVar.getDisplay();
                    typedVar.set((Double) result.value);
                    output = typedVar.getDisplay();
                } else if (isBoolean) {
                    final Variable.Bool typedVar = (Variable.Bool) variable;
                    oldValueDisplay = typedVar.getDisplay();
                    typedVar.set((Boolean) result.value);
                    output = typedVar.getDisplay();
                } else if (isString) {
                    final Variable.Str typedVar = (Variable.Str) variable;
                    oldValueDisplay = typedVar.getDisplay();
                    typedVar.set((String) result.value);
                    output = typedVar.getDisplay();
                } else {
                    StarfruitMod.info("\"%s\" could not be parsed as a proper value", result.value);
                    return;
                }

                final String wasText = HexShift.colorizeLiteral(String.format("(was %s)", oldValueDisplay), "C8C8C8");
                final String varRef = HexShift.colorizeLiteral(variable.getName().orElse(result.valueName), "C8C8C8");
                StarfruitMod.info("%s %s is %s %s", mod.getInfo().hexDisplayString(), varRef, output, wasText);
            });
        }

    }

    public MBassador<Event> getBus() {
        return this.bus;
    }

    public SortedSet<StatefulModule> getDisplay() {
        return this.display;
    }

    public ExecutorService getThreadPool() {
        return this.threadPool;
    }

    public static ModuleManager getInstance() {
        if (INSTANCE == null)
            INSTANCE = new ModuleManager();

        return INSTANCE;
    }
}
