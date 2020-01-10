package software.tachyon.starfruit;

import java.nio.file.*;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.awt.Color;

import net.fabricmc.api.ModInitializer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.MessageType;
import net.minecraft.text.LiteralText;
import software.tachyon.starfruit.mixin.client.MinecraftClientInterfaceMixin;
import software.tachyon.starfruit.module.ModuleManager;
import software.tachyon.starfruit.utility.AccountUtil;
import software.tachyon.starfruit.utility.HexShift;

public class StarfruitMod implements ModInitializer {

  public static final char COLOR_SEPARATOR = '\u00A7';
  private static ModuleManager moduleManager;

  // TODO(haze): move to own class
  public static class Friends {
    static class FriendInformation {
      final String username, alias;

      public FriendInformation(String username, String alias) {
        this.username = username;
        this.alias = alias;
      }
    }

    void read(File from) throws IOException {
      final Properties props = new Properties();
      props.load(new FileInputStream(from));
      for (final Map.Entry<Object, Object> entry : props.entrySet()) {
        final UUID uuid = UUID.fromString((String) entry.getKey());
        final String usernameData = (String) entry.getValue();
        if (usernameData.contains("=")) {
          final String[] info = usernameData.split("=");
          this.addFriend(uuid, info[0], info[1]);
        } else {
          try {
            this.addFriend(uuid, usernameData, usernameData, false);
          } catch (IOException impossible) {
            // this can never happen
          }
        }
      }
    }

    void save(File to) throws IOException {
      final Properties props = new Properties();
      for (final Map.Entry<UUID, FriendInformation> ent : this.friends.entrySet()) {
        final FriendInformation info = ent.getValue();
        if (info.username.equals(info.alias)) {
          props.setProperty(ent.getKey().toString(), info.username);
        } else {
          props.setProperty(ent.getKey().toString(), info.username + "=" + info.alias);
        }
      }
      props.store(new FileOutputStream(to), "Starfruit friend configuration");
    }

    final Map<UUID, FriendInformation> friends;
    final File file;

    private Friends(File file) {
      this.friends = new LinkedHashMap<>();
      this.file = file;
    }

    public void save() throws IOException {
      this.save(this.file);
    }

    public void read() throws IOException {
      this.read(this.file);
    }

    void addFriend(UUID uuid, String username, String alias, boolean save) throws IOException {
      this.friends.put(uuid, new FriendInformation(username, alias));
      if (save)
        this.save();
    }

    public void addFriend(UUID uuid, String username, String alias) throws IOException {
      this.addFriend(uuid, username, alias, true);
    }

    public void removeFriend(UUID uuid) throws IOException {
      this.friends.remove(uuid);
      this.save();
    }

    public boolean isFriend(UUID uuid) {
      return this.friends.containsKey(uuid);
    }

    // public final static String friendColorLiteral = "32ffb9";

    // static Color friendColor = null;
    public static Color getFriendColor() {
      return StarfruitMod.getGlobalIridescence();
      // if (Friends.friendColor == null)
      // Friends.friendColor = new Color(50, 255, 185);
      // return Friends.friendColor;
    }
    // wtf java fucking garbage language
    // public final static Color friendColor = new Color(50, 255, 185);

    public String normalizeString(String source, boolean colorize) {
      String buf = source;
      for (final Map.Entry<UUID, FriendInformation> ent : this.friends.entrySet()) {
        if (StringUtils.containsIgnoreCase(buf, ent.getValue().username)) {
          final String alias =
              colorize ? HexShift.colorize(ent.getValue().alias, Friends.getFriendColor())
                  : ent.getValue().alias;
          buf = buf.replaceAll("(?i)" + ent.getValue().username, alias);
        }
      }
      return buf;
    }
  }

  private static Friends friends;

  public static Friends getFriends() {
    if (friends == null)
      friends = new Friends(StarfruitMod.FRIENDS_FILE);
    return friends;
  }

  public static ModuleManager getModuleManager() {
    if (moduleManager == null)
      moduleManager = new ModuleManager(MODULE_SETTINGS_FILE);
    return moduleManager;
  }

  public static class Colors {
    static float MODULE_SAT = 0.5F;
    static float MODULE_LUM = 0.9F;

    public static Color moduleColor(float hue) {
      System.out.printf("Interpolating color %f\n", hue);
      return Color.getHSBColor(hue, MODULE_SAT, MODULE_LUM);
    }

    public static String colorize(String input, char code) {
      return String.format("%c%c%s%cr", COLOR_SEPARATOR, code, input, COLOR_SEPARATOR);
    }

    // 0 to 1
    public static Color RGBA(double r, double g, double b, double a) {
      return new Color((float) r, (float) g, (float) b, (float) a);
    }

    // 0 to 1
    public static Color HSBA(double h, double s, double b, double a) {
      final Color col = Color.getHSBColor((float) h, (float) s, (float) b);
      return new Color(col.getRed() / 255F, col.getBlue() / 255F, col.getGreen() / 255F, (float) a);
    }
  }

  public final static String DISPLAY_NAME = "Starfruit";
  public final static File FOLDER =
      Paths.get(System.getProperty("user.home"), ".starfruit").toFile();
  private final static File DEV_ACCOUNT_FILE =
      Paths.get(System.getProperty("user.home"), ".secret/.minecraft").toFile();

  private final static File FRIENDS_FILE = new File(FOLDER, "friend.properties");
  private final static File MODULE_SETTINGS_FILE = new File(FOLDER, "module.properties");

  public final static MinecraftClient minecraft = MinecraftClient.getInstance();

  final static float iridescenceSaturation = 0.4F;
  final static float iridescenceBrightness = 1F;

  public static Color getGlobalIridescence() {
    final float hue = (System.currentTimeMillis() / 8) % 360;
    return Color.getHSBColor(hue / 360, iridescenceSaturation, iridescenceBrightness);
  }

  public static void info(String format, Object... items) {
    consoleInfo(format, items);

    final String formatted = String.format("%cFFB972Starfruit%cr %s", HexShift.CATALYST_CHAR,
        COLOR_SEPARATOR, String.format(format, items));

    if (minecraft.inGameHud != null)
      minecraft.inGameHud.addChatMessage(MessageType.SYSTEM, new LiteralText(formatted));
  }

  public static void consoleInfo(String format, Object... items) {
    final String consoleFormatted =
        String.format("Starfruit:info %s", String.format(format, items)).trim();
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
        ((MinecraftClientInterfaceMixin) minecraft)
            .setSession(AccountUtil.createSession(email, password));
      } catch (Exception ignored) {
      }
    }
  }

  private void registerShutdownHook() {
    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      try {
        StarfruitMod.getFriends().save();
        StarfruitMod.getModuleManager().save();
      } catch (Throwable t) {
        t.printStackTrace();
      }
    }));
  }

  void touch(File f) throws IOException {
    new FileOutputStream(f).close();
    f.setLastModified(System.currentTimeMillis());
  }

  @Override
  public void onInitialize() {
    attemptDirectLogin();
    if (!FOLDER.exists())
      FOLDER.mkdirs();
    try {
      if (!MODULE_SETTINGS_FILE.exists())
        touch(MODULE_SETTINGS_FILE);
      // else is covered by module settings constructor
      if (!FRIENDS_FILE.exists())
        touch(FRIENDS_FILE);
      else
        StarfruitMod.getFriends().read();
    } catch (IOException ignored) {
      ignored.printStackTrace();
    }
    registerShutdownHook();
    info("onInitialize()");
  }
}
