package software.tachyon.starfruit.mixin.gui;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.util.ChatMessages;
import net.minecraft.text.OrderedText;
import net.minecraft.text.StringVisitable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import software.tachyon.starfruit.utility.GlobalIridenscencePrefixedText;

import java.util.List;

@Mixin(ChatHud.class)
public abstract class ChatHudMixin {
    @Redirect(method = "addMessage(Lnet/minecraft/text/Text;IIZ)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/ChatMessages;breakRenderedChatMessageLines(Lnet/minecraft/text/StringVisitable;ILnet/minecraft/client/font/TextRenderer;)Ljava/util/List;"))
    private List<OrderedText> redirectBreakRenderedChatMessageLines(StringVisitable stringVisitable, int width, TextRenderer textRenderer) {
        final List<OrderedText> result = ChatMessages.breakRenderedChatMessageLines(stringVisitable, width, textRenderer);

        if (stringVisitable instanceof GlobalIridenscencePrefixedText) {
            final GlobalIridenscencePrefixedText globalIridenscencePrefixedText = (GlobalIridenscencePrefixedText) stringVisitable;
            final OrderedText prefix = globalIridenscencePrefixedText.getPrefix();
            final int prefixLen = globalIridenscencePrefixedText.getPrefixStr().length();

            // would this ever occur?
            if (result.isEmpty() || result.get(0) == OrderedText.EMPTY) {
                result.add(prefix);
            } else {
                final OrderedText original = result.remove(0);
                final OrderedText originalSubstring = visitor -> original.accept((index, style, codePoint) -> {
                    if (index < prefixLen) {
                        return true;
                    }

                    return visitor.accept(index, style, codePoint);
                });
                result.add(0, OrderedText.concat(prefix, originalSubstring));
            }

            return result;
        }

        return ChatMessages.breakRenderedChatMessageLines(stringVisitable, width, textRenderer);
    }
}
