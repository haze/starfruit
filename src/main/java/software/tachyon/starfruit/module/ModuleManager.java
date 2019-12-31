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
import software.tachyon.starfruit.module.movement.Flight;
import software.tachyon.starfruit.module.movement.Sprint;
import software.tachyon.starfruit.module.movement.Velocity;
import software.tachyon.starfruit.module.render.Luminance;
import software.tachyon.starfruit.module.render.ESP;
import software.tachyon.starfruit.module.variable.Variable;
import software.tachyon.starfruit.utility.GLFWKeyMapping;
import software.tachyon.starfruit.utility.HexShift;
import software.tachyon.starfruit.StarfruitMod;
import software.tachyon.starfruit.command.Parser;

import static org.lwjgl.glfw.GLFW.*;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Optional;
import java.util.Properties;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.lwjgl.glfw.GLFW;

import java.util.Map;

// TODO(haze): Remove hardcoded C8C8C8's
// TODO(haze): Redesign 'setBind' to be more ergonomic (if need be)

@Listener(references = References.Strong)
public class ModuleManager {

    private MBassador<Event> bus = null;
    private static ModuleManager INSTANCE = null;

    private Map<Integer, StatefulModule> modules = null;
    private SortedSet<StatefulModule> display = null;
    private Map<String, StatefulModule> moduleNameCache = null;
    private Map<StatefulModule, Map<String, Variable<?>>> variableCache = null;

    private final Parser commandParser;
    private final File saveFile;

    private ExecutorService threadPool = null;

    public ModuleManager(File settingsFile) {
        this.bus = new MBassador<Event>();
        this.commandParser = new Parser();
        this.saveFile = settingsFile;

        this.modules = new HashMap<>();

        this.display = new ConcurrentSkipListSet<>((StatefulModule a, StatefulModule b) -> {
            if (a == b)
                return 0;
            final String an = a.getInfo().name;
            final String bn = b.getInfo().name;
            final int al = an.length();
            final int bl = bn.length();
            if (al == bl)
                return an.compareTo(bn);
            return ((Integer) bl).compareTo(al);
        });

        this.moduleNameCache = new HashMap<>();
        this.variableCache = new HashMap<>();
        this.threadPool = Executors.newCachedThreadPool();

        this.bus.subscribe(this);
        this.register(new Sprint(GLFW_KEY_C));
        this.register(new Luminance(GLFW_KEY_B));
        this.register(new ESP(GLFW_KEY_J));
        this.register(new Flight(GLFW_KEY_K));
        this.register(new Velocity(GLFW_KEY_V));

        try {
            this.readModuleSettings(this.saveFile);
        } catch (FileNotFoundException fnfe) {
            try {
                this.saveFile.createNewFile();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
            fnfe.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    // saveModuleSettings loops through all the modules, saving them
    // to the specified format in a format like so:
    // Either:
    // ModuleName.bind = GLFW_KEY_K
    // or
    // ModuleName.variableName = setting
    void saveModuleSettings(File to) throws IOException {
        final Properties props = new Properties();
        for (final StatefulModule mod : this.modules.values()) {
            // save bind
            props.setProperty(String.format("%s.bind", mod.getInfo().name), mod.getKeyCode().toString());
            // for all registered variables, save them
            Optional.ofNullable(this.variableCache.get(mod)).ifPresent(vals -> {
                for (final Variable<?> variable : vals.values()) {
                    variable.getName().ifPresent(varName -> {
                        props.setProperty(String.format("%s.%s", mod.getInfo().name, varName),
                                variable.get().toString());
                    });
                }
            });
        }
        props.store(new FileOutputStream(to), "Starfruit module configuration");
    }

    String getModuleBindFormat(StatefulModule mod) {
        return String.format("%s.bind", mod.getInfo().name);
    }

    // TODO(haze || fredi) rewrite this shit, please
    // readModuleSettings loops through all the properties of the given file
    // and assigns the custom binds and settings
    void readModuleSettings(File from) throws IOException, FileNotFoundException {
        final Properties props = new Properties();
        props.load(new FileInputStream(from));
        // only try and load what we've registered
        for (final StatefulModule mod : this.modules.values()) {
            // check for the binding
            Optional.ofNullable(this.variableCache.get(mod)).ifPresent(vals -> {
                for (final Variable<?> variable : vals.values()) {
                    System.out.println(variable.getName().orElse("bruh moment cum"));
                    variable.getName().ifPresent(varName -> {
                        final String expectedKey = String.format("%s.%s", mod.getInfo().name, varName);
                        Optional.ofNullable(props.get(expectedKey)).ifPresent(val -> {
                            variable.getKind().ifPresent(kind -> {
                                switch (kind) {
                                case Boolean:
                                    ((Variable.Bool) variable).set(Boolean.parseBoolean((String) val));
                                    break;
                                case Double:
                                    ((Variable.Dbl) variable).set(Double.parseDouble((String) val));
                                    break;
                                case Integer:
                                    ((Variable.Int) variable).set(Integer.parseInt((String) val));
                                    break;
                                case String:
                                    ((Variable.Str) variable).set((String) val);
                                    break;
                                }
                            });
                        });
                    });
                }
            });

        }

    }

    // registerVariables searches the provided module's class for public Variable
    // class declarations and caches the field name for future with the command
    // system
    public void registerVariables(StatefulModule mod) {
        for (Field field : mod.getClass().getFields()) {
            if (Variable.class.isAssignableFrom(field.getType())) {
                final String variableName = field.getName();
                Map<String, Variable<?>> varMap = this.variableCache.remove(mod);
                if (varMap == null) {
                    varMap = new HashMap<>();
                }
                if (!varMap.containsKey(variableName)) {
                    try {
                        final Variable<?> variable = (Variable<?>) field.get(mod);
                        variable.setFieldName(field.getName());
                        varMap.put(field.getName().toLowerCase(), variable);
                        StarfruitMod.consoleInfo("%s: %d\n", mod.getInfo().name, varMap.size());
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

    public void changeModVariable(StatefulModule mod, Parser.SetVariable result) {
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
            StarfruitMod.info("%s does not have variable %s", mod.getInfo().hexDisplayString(), result.valueName);
            return;
        }

        final Class<?> variableType = (Class<?>) ((ParameterizedType) variable.getClass().getGenericSuperclass())
                .getActualTypeArguments()[0];
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
            final Double newValue = result.value instanceof Integer ? ((Integer) result.value).doubleValue()
                    : (Double) result.value;
            typedVar.set(newValue);
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

        final String wasText = HexShift.colorizeLiteral(String.format("(was %s\u0666C8C8C8)", oldValueDisplay),
                "C8C8C8");
        final String varRef = HexShift.colorizeLiteral(variable.getName().orElse(result.valueName), "C8C8C8");
        StarfruitMod.info("%s %s is %s %s", mod.getInfo().hexDisplayString(), varRef, output, wasText);
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
                if (result.valueName.equalsIgnoreCase("bind")) {
                    final String key = result.value.toString().toUpperCase();
                    final Integer maybeKey = GLFWKeyMapping.KEY_MAP.get(key);
                    // TODO(haze): try and use optional
                    final String newBind = HexShift.colorizeLiteral(key, "C8C8C8");
                    if (maybeKey != null) {
                        final Optional<StatefulModule> replaced = this.setBind(mod, maybeKey);
                        final String replacementNotification = replaced.isPresent()
                                ? String.format(" (unbound %s)", replaced.get().getInfo().hexDisplayString())
                                : "";
                        StarfruitMod.info("%s bind is now %s%s", mod.getInfo().hexDisplayString(), newBind,
                                replacementNotification);
                    } else {
                        StarfruitMod.info("No key found for %s", newBind);
                    }
                } else {
                    changeModVariable(mod, result);
                }
            });
        }
    }

    public Optional<StatefulModule> setBind(StatefulModule mod, int newKey) {
        this.modules.remove(mod.getKeyCode());
        final StatefulModule removed = this.modules.remove(newKey);
        mod.setKeyCode(newKey);
        this.modules.put(newKey, mod);
        try {
            this.saveModuleSettings(this.saveFile);
        } catch (Exception e) {
            final String error = HexShift.colorizeLiteral(e.getMessage(), "C8C8C8");
            StarfruitMod.info("Failed to save module settings file: %s", error);
            e.printStackTrace();
        }
        return Optional.ofNullable(removed);
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
}
