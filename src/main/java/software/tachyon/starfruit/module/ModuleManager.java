package software.tachyon.starfruit.module;

import net.engio.mbassy.bus.MBassador;
import net.engio.mbassy.listener.Handler;
import net.engio.mbassy.listener.Listener;
import net.engio.mbassy.listener.References;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.network.listener.ServerPlayPacketListener;
import net.minecraft.server.network.packet.ChatMessageC2SPacket;
import software.tachyon.starfruit.module.event.api.Event;
import software.tachyon.starfruit.module.event.gui.InGameHudDrawEvent;
import software.tachyon.starfruit.module.event.GameJoinEvent;
import software.tachyon.starfruit.module.event.KeyPressEvent;
import software.tachyon.starfruit.module.event.SendPacketEvent;
import software.tachyon.starfruit.module.movement.Flight;
import software.tachyon.starfruit.module.movement.Sprint;
import software.tachyon.starfruit.module.movement.Velocity;
import software.tachyon.starfruit.module.render.Luminance;
import software.tachyon.starfruit.module.render.ESP;
import software.tachyon.starfruit.module.variable.Variable;
import software.tachyon.starfruit.utility.DrawUtility;
import software.tachyon.starfruit.utility.GLFWKeyMapping;
import software.tachyon.starfruit.utility.HexShift;
import software.tachyon.starfruit.StarfruitMod;
import software.tachyon.starfruit.command.Parser;
import software.tachyon.starfruit.mixin.gui.ChatScreenInterfaceMixin;

import static org.lwjgl.glfw.GLFW.*;

import java.awt.Color;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.Queue;
import java.util.Set;
import java.util.SortedSet;
import java.util.StringTokenizer;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import me.xdrop.fuzzywuzzy.FuzzySearch;
import me.xdrop.fuzzywuzzy.model.ExtractedResult;

import java.util.Map;

// TODO(haze): Redesign 'setBind' to be more ergonomic (if need be)
// TODO(haze): KillAura, BlockESP, NoFall, Freecam, FastPlace, AutoRespawn, Speedmine, AutoArmor,
// TODO(haze): Sneak, Step,

@Listener(references = References.Strong)
public class ModuleManager {

    private MBassador<Event> bus = null;

    private Map<Integer, StatefulModule> modules = null;

    private SortedSet<StatefulModule> display = null;
    private Map<String, StatefulModule> moduleNameCache = null;
    private Map<StatefulModule, Map<String, Variable<?>>> variableCache = null;

    // for fuzzy search
    private Set<String> moduleNames = null;
    private Map<StatefulModule, Set<String>> moduleVariableNameCache = null;

    private final Parser commandParser;
    private final File saveFile;

    private final Queue<StatefulModule> toEnableOnceWorldLoads;

    private ExecutorService threadPool = null;

    int moduleNameComparator(StatefulModule a, StatefulModule b) {
        final String an = a.getInfo().name;
        final String bn = b.getInfo().name;
        final double al = StarfruitMod.minecraft.textRenderer.getStringWidth(an);
        final double bl = StarfruitMod.minecraft.textRenderer.getStringWidth(bn);
        if (al == bl)
            return an.compareTo(bn);
        return Double.compare(bl, al);
    }

    public ModuleManager(File settingsFile) {
        this.bus = new MBassador<Event>();
        this.commandParser = new Parser();
        this.saveFile = settingsFile;

        this.modules = new ConcurrentHashMap<>();
        this.moduleNames = new HashSet<>();
        this.moduleVariableNameCache = new HashMap<>();

        this.display = new ConcurrentSkipListSet<>((a, b) -> moduleNameComparator(a, b));

        this.moduleNameCache = new HashMap<>();
        this.variableCache = new HashMap<>();
        this.threadPool = Executors.newCachedThreadPool();

        this.toEnableOnceWorldLoads = new LinkedList<>();

        this.bus.subscribe(this);
        this.registerDefaultModules();

        for (final StatefulModule mod : this.modules.values()) {
            this.moduleNames.add(mod.getInfo().name);
            final Set<String> varNameCache = new HashSet<>();
            Optional.ofNullable(this.variableCache.get(mod)).ifPresent(vars -> {
                for (final Variable<?> variable : vars.values()) {
                    variable.getName().ifPresent(name -> varNameCache.add(name));
                }
            });
            // manually add bind hint
            varNameCache.add("bind");
            this.moduleVariableNameCache.put(mod, varNameCache);
        }

        try {
            this.readModuleSettings(this.saveFile);
        } catch (FileNotFoundException fnfe) {
            try {
                this.saveFile.createNewFile();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    void registerDefaultModules() {
        this.register(new Sprint(GLFW_KEY_C));
        this.register(new Luminance(GLFW_KEY_B));
        this.register(new ESP(GLFW_KEY_J));
        this.register(new Flight(GLFW_KEY_K));
        this.register(new Velocity(GLFW_KEY_V));
    }

    public void save() throws IOException {
        this.saveModuleSettings(this.saveFile);
    }

    public void read() throws IOException {
        this.readModuleSettings(this.saveFile);
    }

    // saveModuleSettings loops through all the modules, saving them
    // to the specified format in a format like so:
    // Either:
    // ModuleName.bind = GLFW_KEY_K
    // or
    // ModuleName.variableName = setting
    void saveModuleSettings(File to) throws IOException {
        final Properties props = new Properties();
        final Set<StatefulModule> sortedMods = new ConcurrentSkipListSet<>((a, b) -> moduleNameComparator(a, b));
        sortedMods.addAll(this.modules.values());
        for (final StatefulModule mod : sortedMods) {
            // save bind
            props.setProperty(String.format("%s.bind", mod.getInfo().name), mod.getKeyCode().toString());
            // save state
            props.setProperty(String.format("%s.state", mod.getInfo().name), Boolean.toString(mod.getState()));
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
            final String expectedBind = String.format("%s.bind", mod.getInfo().name);
            Optional.ofNullable(props.get(expectedBind)).ifPresent(bindObj -> {
                final String bind = (String) bindObj;
                if (bind.equalsIgnoreCase("null"))
                    mod.setKeyCode(null);
                else {
                    try {
                        this.setBindWithoutSaving(mod, Integer.parseInt(bind));
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                }
            });
            // check for state
            final String expectedState = String.format("%s.state", mod.getInfo().name);
            Optional.ofNullable(props.get(expectedState)).ifPresent(stateObj -> {
                final String state = (String) stateObj;
                if (Boolean.parseBoolean(state)) {
                    this.toEnableOnceWorldLoads.add(mod);
                }
            });
            // check for variables
            Optional.ofNullable(this.variableCache.get(mod)).ifPresent(vals -> {
                for (final Variable<?> variable : vals.values()) {
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
    public void onGameLoad(GameJoinEvent event) {
        while (!this.toEnableOnceWorldLoads.isEmpty()) {
            final StatefulModule mod = this.toEnableOnceWorldLoads.poll();
            this.setModuleState(mod, true, false);
        }
    }

    @Handler
    public void onKeyPress(KeyPressEvent event) {
        Optional.ofNullable(this.modules.get(event.getKeyPressed())).ifPresent((module) -> {
            event.setCancelled(true);
            final boolean newState = !module.getState();
            this.setModuleState(module, newState, true);
        });
    }

    void setModuleState(StatefulModule module, boolean newState, boolean save) {
        module.setState(newState);
        if (save) {
            try {
                this.saveModuleSettings(this.saveFile);
            } catch (IOException ignored) {
                ignored.printStackTrace();
            }
        }
        if (newState) {
            module.onEnable();
            this.display.add(module);
        } else {
            this.display.remove(module);
            module.onDisable();
        }
    }

    boolean isInternalCommand(String input) {
        return input.charAt(0) == '.';
    }

    public void changeModVariable(StatefulModule mod, Parser.Result result) {
        final Map<String, Variable<?>> modVariableCache = this.variableCache.get(mod);
        if (modVariableCache == null) {
            StarfruitMod.info("%s has no configurable variables", mod.getInfo().hexDisplayString());
            return;
        }
        Variable<?> variable = null;
        final List<ExtractedResult> fuzzedVariables = this.fuzzySearchVariables(mod, result.selector);
        if (fuzzedVariables.size() > 0) {
            if (fuzzedVariables.get(0).getString().equalsIgnoreCase("bind")) { // check for special bind case
                this.onBindCommand(mod, result);
                return;
            } else {
                variable = this.variableCache.get(mod).get(fuzzedVariables.get(0).getString().toLowerCase());
            }
        }

        if (variable == null) {
            StarfruitMod.info("%s does not have variable %s", mod.getInfo().hexDisplayString(), result.selector);
            return;
        }

        final Class<?> variableType = (Class<?>) ((ParameterizedType) variable.getClass().getGenericSuperclass())
                .getActualTypeArguments()[0];
        final boolean isInt = variableType.isAssignableFrom(Integer.class);
        final boolean isDouble = variableType.isAssignableFrom(Double.class);
        final boolean isBoolean = variableType.isAssignableFrom(Boolean.class);
        final boolean isString = variableType.isAssignableFrom(String.class);

        final String output, oldValueDisplay;

        if (!result.value.isPresent()) {
            StarfruitMod.info("No value provided");
            return;
        } else {
            final Object value = result.value.get();
            if (isInt) {
                final Variable.Int typedVar = (Variable.Int) variable;
                oldValueDisplay = typedVar.getDisplay();
                typedVar.set((Integer) value);
                output = typedVar.getDisplay();
            } else if (isDouble) {
                final Variable.Dbl typedVar = (Variable.Dbl) variable;
                oldValueDisplay = typedVar.getDisplay();
                final Double newValue = value instanceof Integer ? ((Integer) value).doubleValue() : (Double) value;
                typedVar.set(newValue);
                output = typedVar.getDisplay();
            } else if (isBoolean) {
                final Variable.Bool typedVar = (Variable.Bool) variable;
                oldValueDisplay = typedVar.getDisplay();
                typedVar.set((Boolean) value);
                output = typedVar.getDisplay();
            } else if (isString) {
                final Variable.Str typedVar = (Variable.Str) variable;
                oldValueDisplay = typedVar.getDisplay();
                typedVar.set((String) value);
                output = typedVar.getDisplay();
            } else {
                StarfruitMod.info("\"%s\" could not be parsed as a proper value", result.value);
                return;
            }
        }

        try {
            this.saveModuleSettings(this.saveFile);
        } catch (IOException e) {
            StarfruitMod.info("Failed to save to file: %s", HexShift.colorizeLiteral(e.getMessage(), grayColor));
        }

        final String end = HexShift.colorizeLiteral(")", grayColor);
        final String oldValue = HexShift.colorize(oldValueDisplay, Color.WHITE);
        final String wasText = HexShift.colorizeLiteral(String.format("(was %s%s", oldValue, end), grayColor);
        final String varRef = HexShift.colorizeLiteral(variable.getName().orElse(result.selector), grayColor);
        StarfruitMod.info("%s %s is %s %s", mod.getInfo().hexDisplayString(), varRef, output, wasText);
    }

    final String grayColor = "C8C8C8";

    @Handler
    @SuppressWarnings("unchecked")
    public void onChatPacket(SendPacketEvent<ServerPlayPacketListener> event) {
        if (event.getPacket() instanceof ChatMessageC2SPacket) {
            final ChatMessageC2SPacket packet = (ChatMessageC2SPacket) event.getPacket();
            final String chatMessage = packet.getChatMessage();
            event.setCancelled(this.isInternalCommand(chatMessage));
            this.commandParser.parseSetCommand(chatMessage).ifPresent(result -> {
                if (result.command.equalsIgnoreCase("friend")) {
                    onFriendCommand(result);
                    return;
                }

                StatefulModule mod = null;
                final List<ExtractedResult> fuzzedModules = this.fuzzySearchModules(result.command);
                if (fuzzedModules.size() > 0)
                    mod = this.moduleNameCache.get(fuzzedModules.get(0).getString().toLowerCase());

                if (mod == null) {
                    StarfruitMod.info("No module named %s found", result.command);
                    return;
                }

                if (result.selector.equalsIgnoreCase("bind")) {
                    onBindCommand(mod, result);
                } else {
                    changeModVariable(mod, result);
                }
            });
        }

    }

    // TODO(haze): make more clear how we use result (de-couple from modules)
    // .friend Jordin jordin -- toggle w/ alias
    // .friend Jordin -- toggle
    void onFriendCommand(Parser.Result result) {
        final String username = result.selector;
        final String alias = (String) result.value.orElse(username);
        final Optional<PlayerListEntry> optionalPlayer = this.lookupUser(username);
        if (!optionalPlayer.isPresent()) {
            StarfruitMod.info("Player %s not found", HexShift.colorizeLiteral(username, grayColor));
            return;
        }
        final PlayerListEntry player = optionalPlayer.get();
        final UUID uuid = player.getProfile().getId();
        final StarfruitMod.Friends friends = StarfruitMod.getFriends();
        try {
            if (friends.isFriend(uuid)) {
                friends.removeFriend(uuid);
                StarfruitMod.info("No longer friendly towards %s", username);
            } else {
                friends.addFriend(uuid, player.getProfile().getName(), alias);
                StarfruitMod.info("Now friendly towards %s", username);
            }
        } catch (IOException e) {
            StarfruitMod.info("Failed to save friends file: %s", e.getMessage());
        }
    }

    Optional<PlayerListEntry> lookupUser(String username) {
        for (final PlayerListEntry e : StarfruitMod.minecraft.getNetworkHandler().getPlayerList()) {
            if (e.getProfile().getName().equalsIgnoreCase(username)) {
                return Optional.of(e);
            }
        }
        return Optional.empty();
    }

    void onBindCommand(StatefulModule mod, Parser.Result result) {
        if (!result.value.isPresent()) {
            StarfruitMod.info("No bind provided");
            return;
        }
        final String key = result.value.get().toString().toUpperCase();
        final Integer maybeKey = GLFWKeyMapping.KEY_MAP.get(key);
        // TODO(haze): try and use optional
        final String newBind = HexShift.colorizeLiteral(key, grayColor);
        try {
            if (maybeKey != null) {
                final Optional<StatefulModule> replaced = this.setBindAndSave(mod, maybeKey);
                final String replacementNotification = replaced.isPresent()
                        ? String.format(" (unbound %s)", replaced.get().getInfo().hexDisplayString())
                        : "";
                StarfruitMod.info("%s bind is now %s%s", mod.getInfo().hexDisplayString(), newBind,
                        replacementNotification);
            } else {
                StarfruitMod.info("No key found for %s", newBind);
            }
        } catch (Throwable t) {
            // TODO(haze): inspect
            t.printStackTrace();
        }
    }

    public Optional<StatefulModule> setBindAndSave(StatefulModule mod, int newKey) {
        return this.setBind(mod, newKey, true);
    }

    public Optional<StatefulModule> setBindWithoutSaving(StatefulModule mod, int newKey) {
        return this.setBind(mod, newKey, false);
    }

    Optional<StatefulModule> setBind(StatefulModule mod, int newKey, boolean save) {
        if (this.modules.containsKey(mod.getKeyCode()))
            this.modules.remove(mod.getKeyCode());
        final StatefulModule removed = this.modules.remove(newKey);
        if (removed != null)
            removed.setKeyCode(null);
        mod.setKeyCode(newKey);
        this.modules.put(newKey, mod);
        if (save) {
            try {
                this.saveModuleSettings(this.saveFile);
                System.out.println("Saved...");
            } catch (Exception e) {
                final String error = HexShift.colorizeLiteral(e.getMessage(), grayColor);
                StarfruitMod.info("Failed to save module settings file: %s", error);
                e.printStackTrace();
            }
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

    // Fuzzy Shit (+ Hints)

    @Handler
    void onDrawScreen(InGameHudDrawEvent event) {
        final MinecraftClient mc = StarfruitMod.minecraft;
        final ChatHud chat = mc.inGameHud.getChatHud();
        final TextRenderer textRenderer = mc.textRenderer;
        final double y = mc.getWindow().getScaledHeight() - ((textRenderer.fontHeight * 2) + 8);
        if (chat != null && chat.isChatFocused()) {
            final String text = ((ChatScreenInterfaceMixin) mc.currentScreen).getChatField().getText();
            final StringTokenizer tokenizer = new StringTokenizer(text, " ");
            if (text.charAt(0) != '.')
                return;
            final List<String> parts = new ArrayList<>();
            while (tokenizer.hasMoreTokens()) {
                parts.add(tokenizer.nextToken());
            }
            // .fl (fuzzy modules)
            try {
                if (parts.size() > 0) {
                    final List<ExtractedResult> fuzzedModules = this.fuzzySearchModules(parts.get(0).substring(1));
                    final boolean showModuleSelector = parts.size() == 1;
                    final boolean showVariableSelectorPartsPredicate = parts.size() >= 2;
                    final boolean showVariableSelector = showModuleSelector || showVariableSelectorPartsPredicate;
                    System.out.println(Arrays.toString(fuzzedModules.toArray(new ExtractedResult[] {})));
                    if (showModuleSelector && !text.endsWith(" ")) {
                        if (text.trim().equals(".")) {
                            drawStringHints(this.moduleNames, "modules", 4, y);
                        } else {
                            drawFuzzedResults(fuzzedModules, "modules", 4, y);
                        }
                    } else if (showVariableSelector) {
                        final StatefulModule selectedModule = this.moduleNameCache
                                .get(fuzzedModules.get(0).getString().toLowerCase());
                        final double selectedModuleWidth = textRenderer.getStringWidth(selectedModule.getInfo().name);
                        final double x = 4 + selectedModuleWidth;
                        DrawUtility.fill(2, y - 2, x + 2, y + textRenderer.fontHeight + 1,
                                StarfruitMod.Colors.RGBA(0.10, 0.10, 0.10, 0.20).getRGB());
                        textRenderer.drawWithShadow(selectedModule.getInfo().name, 4, (float) y,
                                selectedModule.getInfo().color.getRGB());
                        final double offsetX = x + 10;
                        if (showVariableSelectorPartsPredicate) {
                            final List<ExtractedResult> fuzzedVariables = this.fuzzySearchVariables(selectedModule,
                                    parts.get(1));
                            this.drawFuzzedResults(fuzzedVariables, "variables", offsetX, y);
                        } else {
                            drawStringHints(this.moduleVariableNameCache.get(selectedModule), "variables", offsetX, y);
                        }
                    }
                }
            } catch (Throwable t) {
                t.printStackTrace();
            }
            // TODO(haze): magic variables
        }
    }

    void drawFuzzedResults(List<ExtractedResult> results, String missingItemType, double x, double y) {
        final List<String> strings = new ArrayList<>();
        for (final ExtractedResult res : results) {
            strings.add(res.getString());
        }
        this.drawStringHints(strings, missingItemType, x, y);
    }

    void drawStringHints(Iterable<String> results, String missingItemType, double x, double y) {
        final TextRenderer textRenderer = StarfruitMod.minecraft.textRenderer;
        if (!results.iterator().hasNext()) {
            final String text = "No " + missingItemType;
            final double textWidth = textRenderer.getStringWidth(text);
            DrawUtility.fill(x - 2, y - 2, x + textWidth + 2, y + textRenderer.fontHeight + 1,
                    StarfruitMod.Colors.RGBA(0.10, 0.10, 0.10, 0.20).getRGB());
            textRenderer.drawWithShadow(text, (float) x, (float) y, 0xFFC8C8C8);
            return;
        }
        boolean first = true;
        for (final String str : results) {
            final double textWidth = textRenderer.getStringWidth(str);
            if (x + textWidth >= StarfruitMod.minecraft.getWindow().getScaledWidth())
                break;
            final StatefulModule mod = this.moduleNameCache.get(str.toLowerCase());
            final int color = first ? 0xFFF8F1AC : mod == null ? 0xFFC8C8C8 : mod.getInfo().color.getRGB();

            DrawUtility.fill(x - 2, y - 2, x + textWidth + 2, y + textRenderer.fontHeight + 1,
                    first ? StarfruitMod.Colors.RGBA(0.50, 0.50, 0.50, 0.50).getRGB()
                            : StarfruitMod.Colors.RGBA(0.10, 0.10, 0.10, 0.20).getRGB());
            textRenderer.drawWithShadow(str, (float) x, (float) y, color);
            x += textWidth + 8;
            if (first)
                first = false;
        }
    }

    List<ExtractedResult> fuzzySearchModules(String input) {
        return FuzzySearch.extractSorted(input, this.moduleNames, 50);
    }

    List<ExtractedResult> fuzzySearchVariables(StatefulModule mod, String input) {
        return FuzzySearch.extractSorted(input, this.moduleVariableNameCache.get(mod), 50);
    }
}
