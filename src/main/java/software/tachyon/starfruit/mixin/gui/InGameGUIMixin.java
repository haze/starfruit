package software.tachyon.starfruit.mixin.gui;

import net.engio.mbassy.bus.MBassador;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.hud.InGameHud;
import software.tachyon.starfruit.StarfruitMod;
import software.tachyon.starfruit.module.ModuleInfo;
import software.tachyon.starfruit.module.StatefulModule;
import software.tachyon.starfruit.module.event.api.Event;
import software.tachyon.starfruit.module.event.gui.InGameHudDrawEvent;

import java.util.Iterator;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.Color;

@Mixin(InGameHud.class)
public abstract class InGameGUIMixin extends DrawableHelper {

    private final float MODULE_PADDING = 1F;
    private final float START_X = 2.5F;
    private final float START_Y = 2.5F;

    @Shadow
    private MinecraftClient client;

    @Inject(method = "render", at = @At("RETURN"))
    public void render(float tickDelta, CallbackInfo ci) {
        final MBassador<Event> bus = StarfruitMod.getModuleManager().getBus();
        bus.post(new InGameHudDrawEvent(InGameHudDrawEvent.State.PRE)).now();
        final boolean shouldDraw = !this.client.options.hudHidden && !this.client.options.debugEnabled;
        if (shouldDraw) {
            float y = START_Y;
            final Iterator<StatefulModule> iter = StarfruitMod.getModuleManager().getDisplay().iterator();
            while (iter.hasNext()) {
                final ModuleInfo info = iter.next().getInfo();
                drawStringWithShadow(info.name, START_X, y, info.color, 0.5F);
                y += this.client.textRenderer.fontHeight + MODULE_PADDING;
            }
        }
        bus.post(new InGameHudDrawEvent(InGameHudDrawEvent.State.POST)).now();
    }

    void drawStringWithShadow(String text, float x, float y, Color color, float shadowDist) {
        final Color shadowColor = color.darker().darker();
        this.client.textRenderer.draw(text, x + shadowDist, y + shadowDist, shadowColor.getRGB());
        this.client.textRenderer.draw(text, x, y, color.getRGB());
    }
}
