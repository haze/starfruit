package software.tachyon.starfruit.mixin.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.hud.InGameHud;
import software.tachyon.starfruit.StarfruitMod;
import software.tachyon.starfruit.module.ModuleManager;
import software.tachyon.starfruit.module.StatefulModule;

import java.util.Iterator;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public abstract class InGameGUIMixin extends DrawableHelper {

    private final int MODULE_PADDING = 2;
    @Shadow
    private MinecraftClient client;

    @Inject(method = "render", at = @At("RETURN"))
    public void render(float tickDelta, CallbackInfo ci) {
        final boolean shouldDraw = !this.client.options.hudHidden;
        if (shouldDraw) {
            int y = 2;
            final Iterator<StatefulModule> iter = ModuleManager.getModuleManager().getDisplay().iterator();
            while (iter.hasNext()) {
                final StatefulModule mod = iter.next();
                this.client.textRenderer.drawWithShadow(mod.getInfo().name, 2, y,
                    mod.getInfo().color.getRGB());
                y += this.client.textRenderer.fontHeight + MODULE_PADDING;
            }
        }
    }
}
