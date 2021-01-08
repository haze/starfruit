package software.tachyon.starfruit.module.render;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import software.tachyon.starfruit.StarfruitMod;
import software.tachyon.starfruit.module.ModuleInfo;
import software.tachyon.starfruit.module.StatefulModule;

public class CustomChat extends StatefulModule {

  private ChatScreen customChat;

  public CustomChat(Integer defaultKeyCode) {
    super(defaultKeyCode, ModuleInfo.init().name("Chat").hidden(true).build());
    this.customChat = new ChatScreen(StarfruitMod.minecraft.textRenderer);
  }

  public ChatScreen getCustomChat() {
    return this.customChat;
  }

  public static class ChatScreen extends DrawableHelper {

    final TextRenderer textRenderer;

    public ChatScreen(TextRenderer textRenderer) {
      this.textRenderer = textRenderer;
    }

    public void render(double mouseX, double mouseY, float delta) {
//      this.textRenderer.draw(String.format("jordin %f %f", mouseX, mouseY), 2, 2, 0xFFFFFFFF);
    }
  }
}
