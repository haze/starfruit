package software.tachyon.starfruit.module;

import me.xdrop.fuzzywuzzy.FuzzySearch;
import me.xdrop.fuzzywuzzy.model.ExtractedResult;
import net.engio.mbassy.bus.MBassador;
import net.engio.mbassy.listener.Handler;
import net.engio.mbassy.listener.Listener;
import net.engio.mbassy.listener.References;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.network.listener.ServerPlayPacketListener;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import software.tachyon.starfruit.StarfruitMod;
import software.tachyon.starfruit.command.Parser;
import software.tachyon.starfruit.mixin.gui.ChatScreenInterfaceMixin;
import software.tachyon.starfruit.module.event.GameJoinEvent;
import software.tachyon.starfruit.module.event.KeyPressEvent;
import software.tachyon.starfruit.module.event.SendPacketEvent;
import software.tachyon.starfruit.module.event.api.Event;
import software.tachyon.starfruit.module.event.gui.InGameHudDrawEvent;
import software.tachyon.starfruit.module.movement.Flight;
import software.tachyon.starfruit.module.movement.NoFall;
import software.tachyon.starfruit.module.movement.Sprint;
import software.tachyon.starfruit.module.movement.Velocity;
import software.tachyon.starfruit.module.network.Crash;
import software.tachyon.starfruit.module.network.PacketLogger;
import software.tachyon.starfruit.module.render.Camera;
import software.tachyon.starfruit.module.render.ESP;
import software.tachyon.starfruit.module.render.Luminance;
import software.tachyon.starfruit.module.utility.AutoArmor;
import software.tachyon.starfruit.module.utility.FastPlace;
import software.tachyon.starfruit.module.variable.Variable;
import software.tachyon.starfruit.utility.DrawUtility;
import software.tachyon.starfruit.utility.GLFWKeyMapping;
import software.tachyon.starfruit.utility.HexShift;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.lwjgl.glfw.GLFW.*;
import static software.tachyon.starfruit.utility.StarfruitTextFactory.throwable;
import static software.tachyon.starfruit.utility.TextFactory.*;

// TODO(haze): Redesign 'setBind' to be more ergonomic (if need be)
// TODO(haze): KillAura, BlockESP, FastPlace, AutoRespawn, Speedmine, AutoArmor,
// TODO(haze): Sneak, Step, Speed,

@Listener(references = References.Strong)
public class ModuleManager {

    private MBassador<Event> bus = null;

    private Set<StatefulModule> modules = null;
    private Map<Integer, StatefulModule> toggleModules = null;

    private SortedSet<StatefulModule> display = null;
    private Map<String, StatefulModule> moduleNameCache = null;
    private Map<StatefulModule, Map<String, Variable<?>>> variableCache = null;

    // for fuzzy search
    private Set<String> moduleNames = null;
    private Map<StatefulModule, Set<String>> moduleVariableNameCache = null;


    private Map<Class<? extends StatefulModule>, StatefulModule> moduleClassCache = null;

    private final Parser commandParser;
    private final File saveFile;

    private final Queue<StatefulModule> toEnableOnceWorldLoads;

    private ExecutorService threadPool = null;

    int moduleNameComparator(StatefulModule a, StatefulModule b) {
        final String an = a.getInfo().name;
        final String bn = b.getInfo().name;
        final double al = StarfruitMod.minecraft.textRenderer.getWidth(an);
        final double bl = StarfruitMod.minecraft.textRenderer.getWidth(bn);
        if (al == bl)
            return an.compareTo(bn);
        return Double.compare(bl, al);
    }

    public ModuleManager(File settingsFile) {
        this.bus = new MBassador<Event>();
        this.commandParser = new Parser();
        this.saveFile = settingsFile;

        this.toggleModules = new ConcurrentHashMap<>();
        this.modules = new HashSet<>();

        this.moduleNames = new HashSet<>();
        this.moduleVariableNameCache = new HashMap<>();

        this.display = new ConcurrentSkipListSet<>((a, b) -> moduleNameComparator(a, b));

        this.moduleNameCache = new HashMap<>();
        this.variableCache = new HashMap<>();
        this.moduleClassCache = new HashMap<>();

        this.threadPool = Executors.newCachedThreadPool();

        this.toEnableOnceWorldLoads = new LinkedList<>();

        this.bus.subscribe(this);
        this.registerDefaultModules();

        for (final StatefulModule mod : this.modules) {
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
        this.register(new Camera(GLFW_KEY_M));
        this.register(new Crash(GLFW_KEY_L));
        this.register(new AutoArmor(null));
        this.register(new PacketLogger(null));
        this.register(new NoFall(null));
        this.register(new FastPlace(GLFW_KEY_P));
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
        for (final StatefulModule mod : this.modules) {
            // save bind
            props.setProperty(String.format("%s.bind", mod.getInfo().name), mod.getKeyCodeString());
            // save state
            props.setProperty(String.format("%s.state", mod.getInfo().name),
                    Boolean.toString(mod.getState()));
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
        for (final StatefulModule mod : this.modules) {
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
                        final String expectedKey =
                                String.format("%s.%s", mod.getInfo().name, varName);
                        Optional.ofNullable(props.get(expectedKey)).ifPresent(val -> {
                            variable.getKind().ifPresent(kind -> {
                                switch (kind) {
                                    case Boolean:
                                        ((Variable.Bool) variable)
                                                .set(Boolean.parseBoolean((String) val));
                                        break;
                                    case Double:
                                        ((Variable.Dbl) variable)
                                                .set(Double.parseDouble((String) val));
                                        break;
                                    case Integer:
                                        ((Variable.Int) variable)
                                                .set(Integer.parseInt((String) val));
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
                        StarfruitMod.consoleInfo("Found variable with bad access (%s) in %s.\n",
                                variableName, mod.getInfo().name);
                    }
                } else {
                    StarfruitMod.consoleInfo("Found variable redefinition (%s) in %s.\n",
                            variableName, mod.getInfo().name);
                }

                StarfruitMod.consoleInfo("Found %s variable %s!\n", mod.getInfo().name,
                        field.getName());
            }
        }
    }

    public void register(StatefulModule mod) {
        this.modules.add(mod);
        if (mod.getKeyCode() != null)
            this.toggleModules.put(mod.getKeyCode(), mod);
        this.moduleNameCache.put(mod.getInfo().name.toLowerCase(), mod);
        this.moduleClassCache.put(mod.getClass(), mod);
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
        Optional.ofNullable(this.toggleModules.get(event.getKeyPressed())).ifPresent((module) -> {
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
            if (!module.getInfo().isHidden())
                this.display.add(module);
        } else {
            if (!module.getInfo().isHidden())
                this.display.remove(module);
            module.onDisable();
        }
    }

    public static boolean isInternalCommand(String input) {
        return input.charAt(0) == '.';
    }

    public void changeModVariable(StatefulModule mod, Parser.Result result) {
        try {
            final Map<String, Variable<?>> modVariableCache = this.variableCache.get(mod);
            Variable<?> variable = null;
            if (!result.selector.isPresent()) {
                StarfruitMod.info(text("No variable name provided"));
                return;
            }
            final List<ExtractedResult> fuzzedVariables =
                    this.fuzzySearchVariables(mod, result.selector.get());
            if (fuzzedVariables.size() > 0) {
                if (fuzzedVariables.get(0).getString().equalsIgnoreCase("bind")) {
                    this.onBindCommand(mod, result);
                    return;
                } else {
                    if (modVariableCache == null) {
                        StarfruitMod.info(join(
                                mod.getInfo().displayText(),
                                text(" has no configurable variables")
                        ));

                        return;
                    }
                    variable =
                            modVariableCache.get(fuzzedVariables.get(0).getString().toLowerCase());
                }
            }

            if (variable == null) {
                StarfruitMod.info(join(
                        mod.getInfo().displayText(),
                        text("does not have variable " + result.selector.get())
                ));
                return;
            }

            final Class<?> variableType =
                    (Class<?>) ((ParameterizedType) variable.getClass().getGenericSuperclass())
                            .getActualTypeArguments()[0];
            final boolean isInt = variableType.isAssignableFrom(Integer.class);
            final boolean isDouble = variableType.isAssignableFrom(Double.class);
            final boolean isBoolean = variableType.isAssignableFrom(Boolean.class);
            final boolean isString = variableType.isAssignableFrom(String.class);

            final String output, oldValueDisplay;
            final Style outputStyle;

            final String varRef = HexShift.colorizeLiteral(
                    variable.getName().orElse(result.selector.orElse("???")), grayColor);

            if (result.value.isEmpty()) {
                StarfruitMod.info(join(
                        mod.getInfo().displayText(),
                        text(varRef),
                        text("is"),
                        text(variable.getDisplay()).setStyle(variable.getStyle())
                ));
                return;
            } else {
                final Object value = result.value.get();
                if (isInt) {
                    final Variable.Int typedVar = (Variable.Int) variable;
                    oldValueDisplay = typedVar.getDisplay();
                    typedVar.set((Integer) value);
                    output = typedVar.getDisplay();
                    outputStyle = typedVar.getStyle();
                } else if (isDouble) {
                    final Variable.Dbl typedVar = (Variable.Dbl) variable;
                    oldValueDisplay = typedVar.getDisplay();
                    final Double newValue =
                            value instanceof Integer ? ((Integer) value).doubleValue()
                                    : (Double) value;
                    typedVar.set(newValue);
                    output = typedVar.getDisplay();
                    outputStyle = typedVar.getStyle();
                } else if (isBoolean) {
                    final Variable.Bool typedVar = (Variable.Bool) variable;
                    oldValueDisplay = typedVar.getDisplay();
                    typedVar.set((Boolean) value);
                    output = typedVar.getDisplay();
                    outputStyle = typedVar.getStyle();
                } else if (isString) {
                    final Variable.Str typedVar = (Variable.Str) variable;
                    oldValueDisplay = typedVar.getDisplay();
                    typedVar.set((String) value);
                    output = typedVar.getDisplay();
                    outputStyle = typedVar.getStyle();
                } else {
                    StarfruitMod.info(text("\"" + result.value + "\" could not be parsed as a proper value"));
                    return;
                }
            }

            try {
                this.saveModuleSettings(this.saveFile);
            } catch (IOException e) {
                StarfruitMod.info(join(red("Failed to save to file:"), throwable(e)));
            }

            StarfruitMod.info(join(
                    mod.getInfo().displayText(),
                    text(varRef),
                    text("is"),
                    text(output).setStyle(outputStyle),
                    gray("(was "),
                    white(oldValueDisplay),
                    gray(")")
            ));
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    final String grayColor = "C8C8C8";

    @Handler
    @SuppressWarnings("unchecked")
    public void onChatPacket(SendPacketEvent<ServerPlayPacketListener> event) {
        if (event.getPacket() instanceof ChatMessageC2SPacket) {
            final ChatMessageC2SPacket packet = (ChatMessageC2SPacket) event.getPacket();
            final String chatMessage = packet.getChatMessage();
            event.setCancelled(ModuleManager.isInternalCommand(chatMessage));
            this.commandParser.parseSetCommand(chatMessage).ifPresent(result -> {
                System.out.println(result);
                if (result.command.equalsIgnoreCase("friend")) {
                    onFriendCommand(result);
                    return;
                }

                StatefulModule mod = null;
                final List<ExtractedResult> fuzzedModules = this.fuzzySearchModules(result.command);
                if (fuzzedModules.size() > 0)
                    mod = this.moduleNameCache.get(fuzzedModules.get(0).getString().toLowerCase());

                if (mod == null) {
                    StarfruitMod.info(text("No module named " + result.command + " found"));
                    return;
                }

                final boolean selectorIsPresent = result.selector.isPresent();
                if (selectorIsPresent && result.selector.get().equalsIgnoreCase("bind")) {
                    onBindCommand(mod, result);
                } else if (!selectorIsPresent) {
                    System.out.println("cusdfkasdfj");
                    this.setModuleState(mod, !mod.getState(), true);
                    if (mod.getInfo().isHidden())
                        StarfruitMod.info(join(
                                mod.getInfo().displayText(),
                                text("is"),
                                Variable.Bool.displayGiven(mod.getState())
                        ));
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
        if (!result.selector.isPresent()) {
            StarfruitMod.info(text("No username provided"));
            return;
        }
        final String username = result.selector.get();
        final String alias = (String) result.value.orElse(username);
        final Optional<PlayerListEntry> optionalPlayer = this.lookupUser(username);
        if (!optionalPlayer.isPresent()) {
            StarfruitMod.info(join(
                text("Player"),
                gray(username),
                text("not found")
            ));
            return;
        }
        final PlayerListEntry player = optionalPlayer.get();
        final UUID uuid = player.getProfile().getId();
        final StarfruitMod.Friends friends = StarfruitMod.getFriends();
        try {
            if (friends.isFriend(uuid)) {
                friends.removeFriend(uuid);
                StarfruitMod.info(text("No longer friendly towards " + username));
            } else {
                friends.addFriend(uuid, player.getProfile().getName(), alias);
                StarfruitMod.info(text("Now friendly towards " + username));
            }
        } catch (IOException e) {
            StarfruitMod.info(join(red("Failed to save friends file:"), throwable(e)));
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
            StarfruitMod.info(text("No bind provided"));
            return;
        }
        final String key = result.value.get().toString().toUpperCase();
        final Integer maybeKey = GLFWKeyMapping.KEY_MAP.get(key);
        // TODO(haze): try and use optional
        final String newBind = HexShift.colorizeLiteral(key, grayColor);
        try {
            if (maybeKey != null) {
                final Optional<StatefulModule> replaced = this.setBindAndSave(mod, maybeKey);

                final List<MutableText> bindMessage = new ArrayList<>(3 + 3);
                bindMessage.add(mod.getInfo().displayText());
                bindMessage.add(text(" bind is now "));
                bindMessage.add(gray(newBind));

                replaced.ifPresent(rep -> {
                    bindMessage.add(text(" (unbound"));
                    bindMessage.add(rep.getInfo().displayText());
                    bindMessage.add(text(")"));
                });

                StarfruitMod.info(concat(bindMessage));
            } else {
                StarfruitMod.info(join(
                        text("No key found for"),
                        gray(newBind)
                ));
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
        if (this.toggleModules.containsKey(mod.getKeyCode()))
            this.toggleModules.remove(mod.getKeyCode());
        final StatefulModule removed = this.toggleModules.remove(newKey);
        if (removed != null)
            removed.setKeyCode(null);
        mod.setKeyCode(newKey);
        this.toggleModules.put(newKey, mod);
        if (save) {
            try {
                this.saveModuleSettings(this.saveFile);
                System.out.println("Saved...");
            } catch (Exception e) {
                StarfruitMod.info(join(red("Failed to save module settings file:"), throwable(e)));
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

    final String[] disallowedCommands = new String[] {"friend"};

    boolean doesCommandNotRequireSuggestion(String inputPart) {
        for (final String check : this.disallowedCommands)
            if (inputPart.equalsIgnoreCase(check))
                return true;
        return false;
    }

    final String[] disallowedSelectors = new String[] {"bind"};

    boolean doesSelectorNotRequireSuggestion(String inputPart) {
        for (final String check : this.disallowedSelectors)
            if (inputPart.equalsIgnoreCase(check))
                return true;
        return false;
    }

    @Handler
    void onDrawScreen(InGameHudDrawEvent event) {
        final MinecraftClient mc = StarfruitMod.minecraft;
        final ChatHud chat = mc.inGameHud.getChatHud();
        final TextRenderer textRenderer = mc.textRenderer;
        final double y = mc.getWindow().getScaledHeight() - ((textRenderer.fontHeight * 2) + 8);
        if (chat != null && mc.currentScreen instanceof ChatScreen) {
            final String text =
                    ((ChatScreenInterfaceMixin) mc.currentScreen).getChatField().getText();
            if (text.length() <= 0 || text.charAt(0) != '.')
                return;
            final StringTokenizer tokenizer = new StringTokenizer(text, " ");
            final List<String> parts = new ArrayList<>();
            while (tokenizer.hasMoreTokens()) {
                parts.add(tokenizer.nextToken());
            }
            // .fl (fuzzy modules)
            if (parts.size() > 0) {
                // TODO(haze): extract into method
                final String command = parts.get(0).substring(1);
                if (doesCommandNotRequireSuggestion(command))
                    return;
                final List<ExtractedResult> fuzzedModules = this.fuzzySearchModules(command);
                final boolean showModuleSelector = parts.size() == 1;
                final boolean showVariableSelectorPartsPredicate = parts.size() >= 2;
                final boolean showVariableSelector =
                        showModuleSelector || showVariableSelectorPartsPredicate;
                if (showModuleSelector && !text.endsWith(" ")) {
                    if (text.trim().equals(".")) {
                        drawStringHints(event.getMatrices(), this.moduleNames, "modules", 4, y);
                    } else {
                        drawFuzzedResults(event.getMatrices(), fuzzedModules, "modules", 4, y);
                    }
                } else if (showVariableSelector) {
                    if (doesSelectorNotRequireSuggestion(parts.get(1)))
                        return;
                    final StatefulModule selectedModule = this.moduleNameCache
                            .get(fuzzedModules.get(0).getString().toLowerCase());
                    final double selectedModuleWidth =
                            textRenderer.getWidth(selectedModule.getInfo().name);
                    final double x = 4 + selectedModuleWidth;
                    DrawUtility.fill(2, y - 2, x + 2, y + textRenderer.fontHeight + 1,
                            StarfruitMod.Colors.RGBA(0.10, 0.10, 0.10, 0.20).getRGB());
                    textRenderer.drawWithShadow(event.getMatrices(), selectedModule.getInfo().name, 4, (float) y,
                            selectedModule.getInfo().color.getRGB());
                    final double offsetX = x + 10;
                    if (showVariableSelectorPartsPredicate) {
                        final List<ExtractedResult> fuzzedVariables =
                                this.fuzzySearchVariables(selectedModule, parts.get(1));
                        this.drawFuzzedResults(event.getMatrices(), fuzzedVariables, "variables", offsetX, y);
                    } else {
                        drawStringHints(event.getMatrices(), this.moduleVariableNameCache.get(selectedModule),
                                "variables", offsetX, y);
                    }
                }
            }
            // TODO(haze): magic variables
        }
    }

    void drawFuzzedResults(MatrixStack matrices, List<ExtractedResult> results, String missingItemType, double x,
            double y) {
        final List<String> strings = new ArrayList<>();
        for (final ExtractedResult res : results) {
            strings.add(res.getString());
        }
        this.drawStringHints(matrices, strings, missingItemType, x, y);
    }

    // TODO(haze): un-magic variableify
    void drawStringHints(MatrixStack stack, Iterable<String> results, String missingItemType, double x, double y) {
        final TextRenderer textRenderer = StarfruitMod.minecraft.textRenderer;
        if (!results.iterator().hasNext()) {
            final String text = "No " + missingItemType;
            final double textWidth = textRenderer.getWidth(text);
            DrawUtility.fill(x - 2, y - 2, x + textWidth + 2, y + textRenderer.fontHeight + 1,
                    StarfruitMod.Colors.RGBA(0.10, 0.10, 0.10, 0.20).getRGB());
            textRenderer.drawWithShadow(stack, text, (float) x, (float) y, 0xFFC8C8C8);
            return;
        }
        boolean first = true;
        for (final String str : results) {
            final double textWidth = textRenderer.getWidth(str);
            if (x + textWidth >= StarfruitMod.minecraft.getWindow().getScaledWidth())
                break;
            final StatefulModule mod = this.moduleNameCache.get(str.toLowerCase());
            final int color =
                    first ? 0xFFF8F1AC : mod == null ? 0xFFC8C8C8 : mod.getInfo().color.getRGB();

            DrawUtility.fill(x - 2, y - 2, x + textWidth + 2, y + textRenderer.fontHeight + 1,
                    first ? StarfruitMod.Colors.RGBA(0.10, 0.10, 0.10, 0.20).getRGB()
                            : StarfruitMod.Colors.RGBA(0.20, 0.20, 0.20, 0.10).getRGB());
            textRenderer.drawWithShadow(stack, str, (float) x, (float) y, color);
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

    @SuppressWarnings("unchecked")
    public <T extends StatefulModule> T getStatefulModule(Class<T> moduleClass) {
        return (T) this.moduleClassCache.get(moduleClass);
    }
}
