package software.tachyon.starfruit.mixin.gui;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;

@Mixin(ChatScreen.class)
public interface ChatScreenInterfaceMixin {
    @Accessor
    public TextFieldWidget getChatField();
}
