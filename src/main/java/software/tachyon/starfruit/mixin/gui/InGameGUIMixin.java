package software.tachyon.starfruit.mixin.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.hud.InGameHud;
import software.tachyon.starfruit.StarfruitMod;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(InGameHud.class)
public abstract class InGameGUIMixin extends DrawableHelper {

    @Shadow
    private MinecraftClient client;

    @Inject(method = "render", at = @At("RETURN"))
    public void render(float tickDelta, CallbackInfo ci) {
        final boolean shouldDraw = !this.client.options.hudHidden;
        if (shouldDraw) {
            this.client.textRenderer.drawWithShadow(StarfruitMod.DISPLAY_NAME, 2, 2,
                    StarfruitMod.Colors.GREEN.getRGB());
        }
    }
}
