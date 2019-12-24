package software.tachyon.starfruit.mixin.gui;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.util.NarratorManager;
import net.minecraft.text.TranslatableText;
import software.tachyon.starfruit.StarfruitMod;

import java.awt.Color;
import java.awt.color.ColorSpace;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(TitleScreen.class)
public class TitleScreenMixin extends Screen {

    private final int brandPadding = 3;

    protected TitleScreenMixin() {
        super(NarratorManager.EMPTY);
        throw new UnsupportedOperationException();
    }

    // TODO(haze) figure out why the FAILHARD fails at initialization
    @Inject(method = "render", at = @At("RETURN"), locals = LocalCapture.CAPTURE_FAILSOFT)
    protected void render(int mouseX, int mouseY, float delta, CallbackInfo ci, float f, int i, int j, float g) {
        // g = opacity
        final int l = ((int) Math.ceil(g * 255.0F)) << 24;
        final boolean shouldDraw = (l & -67108864) != 0;
        if (shouldDraw) {
            final int rgb = Color.HSBtoRGB((float) (f / 8) % 360, 0.4F, 1F);
            final int red = (rgb >> 16) & 0xFF;
            final int green = (rgb >> 8) & 0xFF;
            final int blue = rgb & 0xFF;
            int brandColor = (StarfruitMod.getGlobalIridescence().getRGB() & 0x00FFFFFF) | l;
            this.drawString(this.font, StarfruitMod.DISPLAY_NAME, 2,
                    this.height - (this.font.fontHeight * 2) - brandPadding, brandColor);
        }
    }
}
