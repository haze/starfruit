package software.tachyon.starfruit;

import java.nio.file.*;
import java.util.Scanner;
import java.io.File;
import java.awt.Color;

import net.fabricmc.api.ModInitializer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.MessageType;
import net.minecraft.text.LiteralText;
import net.minecraft.util.math.MathHelper;
import software.tachyon.starfruit.mixin.MinecraftClientMixin;
import software.tachyon.starfruit.module.ModuleManager;
import software.tachyon.starfruit.utility.AccountUtil;
import software.tachyon.starfruit.utility.HexShift;

public class StarfruitMod implements ModInitializer {

    public static final char COLOR_SEPARATOR = '\u00A7';
    private static ModuleManager moduleManager;

    public static class Colors {
        static float MODULE_SAT = 0.5F;
        static float MODULE_LUM = 0.9F;

        public static Color moduleColor(float hue) {
            return Color.getHSBColor(hue, MODULE_SAT, MODULE_LUM);
        }

        public static String colorize(String input, char code) {
            return String.format("%c%c%s%cr", COLOR_SEPARATOR, code, input, COLOR_SEPARATOR);
        }
    }

    public final static String DISPLAY_NAME = "Starfruit";
    private final static File DEV_ACCOUNT_FILE = Paths.get(System.getProperty("user.home"), ".secret/.minecraft")
            .toFile();
    private final static File MODULE_SETTINGS_FILE = Paths.get(System.getProperty("user.home"), ".starfruit.properties")
            .toFile();

    public final static MinecraftClient minecraft = MinecraftClient.getInstance();

    public static ModuleManager getModuleManager() {
        if (moduleManager == null)
            moduleManager = new ModuleManager(MODULE_SETTINGS_FILE);
        return moduleManager;
    }

    final static float iridescenceSaturation = 0.4F;
    final static float iridescenceBrightness = 1F;

    public static Color getGlobalIridescence() {
        final float hue = (System.currentTimeMillis() / 8) % 360;
        return Color.getHSBColor(hue / 360, iridescenceSaturation, iridescenceBrightness);
    }

    public static void info(String format, Object... items) {
        consoleInfo(format, items);

        final String formatted = String.format("%cFFB972Starfruit%cr %s", HexShift.CATALYST_CHAR, COLOR_SEPARATOR,
                String.format(format, items));

        if (minecraft.inGameHud != null)
            minecraft.inGameHud.addChatMessage(MessageType.SYSTEM, new LiteralText(formatted));
    }

    public static void consoleInfo(String format, Object... items) {
        final String consoleFormatted = String.format("Starfruit:info %s", String.format(format, items)).trim();
        System.out.println(consoleFormatted);
    }

    final static float iridescenceSaturation = 0.4F;
    final static float iridescenceBrightness = 1F;

    public static Color getGlobalIridescence() {
        final float hue = (System.currentTimeMillis() / 8) % 360;
        return Color.getHSBColor(hue / 360, iridescenceSaturation, iridescenceBrightness);
    }

    public static void info(String format, Object... items) {
        consoleInfo(format, items);

        final String formatted = String.format("%cFFB972Starfruit%cr %s", HexShift.CATALYST_CHAR, COLOR_SEPARATOR,
                String.format(format, items));

        if (minecraft.inGameHud != null)
            minecraft.inGameHud.addChatMessage(MessageType.SYSTEM, new LiteralText(formatted));
    }

    public static void consoleInfo(String format, Object... items) {
        final String consoleFormatted = String.format("Starfruit:info %s", String.format(format, items)).trim();
        System.out.println(consoleFormatted);
    }

    // attemptDirectLogin tries to login given a minecraft account
    // specified in $HOME/.secret/minecraft for easy access to a session
    // during development
    private void attemptDirectLogin() {
        if (DEV_ACCOUNT_FILE.exists()) {
            try { // Ignore exception from Scanner, we check if the file exists above
                final Scanner scanner = new Scanner(DEV_ACCOUNT_FILE);
                final String email = scanner.nextLine();
                final String password = scanner.nextLine();
                scanner.close();
                ((MinecraftClientMixin) minecraft).setSession(AccountUtil.createSession(email, password));
            } catch (Exception ignored) {
            }
        }
    }

    private void registerShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("hi");
        }));
    }

    @Override
    public void onInitialize() {
        attemptDirectLogin();
        registerShutdownHook();
        info("onInitialize()");
    }
}
