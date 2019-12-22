package software.tachyon.starfruit;

import java.nio.file.*;
import java.util.Scanner;
import java.io.File;
import java.awt.Color;

import net.fabricmc.api.ModInitializer;
import net.minecraft.client.MinecraftClient;
import software.tachyon.starfruit.mixin.MinecraftClientMixin;
import software.tachyon.starfruit.util.AccountUtil;

public class StarfruitMod implements ModInitializer {

    public static class Colors {
        public static final Color GREEN = new Color(130, 235, 224, 124); // #82ebe0
    }

    public final static String DISPLAY_NAME = "Starfruit";
    private final static File DEV_ACCOUNT_FILE = Paths.get(System.getProperty("user.home"), ".secret/.minecraft")
            .toFile();

    public final static MinecraftClientMixin minecraft = (MinecraftClientMixin) MinecraftClient.getInstance();

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
                minecraft.setSession(AccountUtil.createSession(email, password));
            } catch (Exception ignored) {
            }
        }
    }

    @Override
    public void onInitialize() {
        attemptDirectLogin();
        System.out.println("Hello Fabric world!");
    }
}
