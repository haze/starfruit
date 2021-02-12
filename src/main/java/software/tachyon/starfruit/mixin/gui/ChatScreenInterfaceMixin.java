package software.tachyon.starfruit.mixin.gui;

import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ChatScreen.class)
public interface ChatScreenInterfaceMixin {
    @Accessor
    TextFieldWidget getChatField();
}
