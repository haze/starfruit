package software.tachyon.starfruit.mixin.gui;

import net.engio.mbassy.bus.MBassador;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.hud.BossBarHud;
import net.minecraft.client.gui.hud.ClientBossBar;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import software.tachyon.starfruit.StarfruitMod;
import software.tachyon.starfruit.module.ModuleInfo;
import software.tachyon.starfruit.module.StatefulModule;
import software.tachyon.starfruit.module.event.api.Event;
import software.tachyon.starfruit.module.event.gui.InGameHudDrawEvent;
import software.tachyon.starfruit.utility.DrawUtility;

import java.awt.*;
import java.util.*;

@Mixin(InGameHud.class)
public abstract class InGameGUIMixin extends DrawableHelper {

  private final double MODULE_PADDING = 1;
  private final double START_X = 2.5;
  private final double START_Y = 2.5;

  @Final
  @Shadow
  private MinecraftClient client;

  @Inject(method = "render", at = @At("HEAD"))
  public void renderPre(MatrixStack matrices, float tickDelta, CallbackInfo ci) {
    final MBassador<Event> bus = StarfruitMod.getModuleManager().getBus();
    bus.post(new InGameHudDrawEvent(InGameHudDrawEvent.State.PRE, matrices, (double) tickDelta)).now();
  }

  @Inject(method = "render", at = @At("RETURN"))
  public void renderPost(MatrixStack matrices, float tickDelta, CallbackInfo ci) {
    final MBassador<Event> bus = StarfruitMod.getModuleManager().getBus();
    final boolean shouldDraw = !this.client.options.hudHidden && !this.client.options.debugEnabled;
    bus.post(new InGameHudDrawEvent(InGameHudDrawEvent.State.POST, matrices, (double) tickDelta)).now();
    if (shouldDraw) {
      this.drawModules(matrices);
    }
  }

  void drawModules(MatrixStack stack) {
    double y = isBossBarConflictingWithModules().orElse(START_Y);
    final Iterator<StatefulModule> iter = StarfruitMod.getModuleManager().getDisplay().iterator();
    while (iter.hasNext()) {
      final ModuleInfo info = iter.next().getInfo();
      drawStringWithShadow(stack, info.name, START_X, y, info.color, 0.5F);
      y += this.client.textRenderer.fontHeight + MODULE_PADDING;
    }
  }

  // returns the additional space needed to outspace the boss bars
  Optional<Double> isBossBarConflictingWithModules() {
    final BossBarHud bar = StarfruitMod.minecraft.inGameHud.getBossBarHud();
    final boolean bossBarExists = bar != null;
    StatefulModule firstModule = null;
    try {
      firstModule = StarfruitMod.getModuleManager().getDisplay().first();
    } catch (NoSuchElementException ignored) {
    }

    if (firstModule != null && bossBarExists) {
      final String longestModuleName = firstModule.getInfo().name;
      final double moduleEndX = START_X + this.client.textRenderer.getWidth(longestModuleName);

      int lastConflictingBossBarIndex = 0;
      int bossBarIdx = 0;

      final Set<Map.Entry<UUID, ClientBossBar>> bossBars = ((BossBarHudMixinInterface) bar).getBossBars().entrySet();

      final double minecraftMiddleX = this.client.getWindow().getScaledWidth() / 2.0;

      for (final Map.Entry<UUID, ClientBossBar> entry : bossBars) {
        final String bossBarText = DrawUtility.asString(entry.getValue().getName().asOrderedText());
        final int bossBarTextWidth = this.client.textRenderer.getWidth((bossBarText));
        if (moduleEndX >= (minecraftMiddleX - (bossBarTextWidth / 2))) {
          lastConflictingBossBarIndex = bossBarIdx + 1;
        }
        bossBarIdx += 1;
      }

      // System.out.printf("bbs = %d\n", lastConflictingBossBarIndex);

      if (lastConflictingBossBarIndex != 0)
        return Optional.of(MODULE_PADDING + 12 + ((lastConflictingBossBarIndex - 1) * 19.0));
    }
    return Optional.empty();
  }

  void drawStringWithShadow(MatrixStack stack, String text, double x, double y, Color color, double shadowDist) {
    final Color shadowColor = color.darker().darker();
    this.client.textRenderer.draw(stack, text, (float) (x + shadowDist), (float) (y + shadowDist), shadowColor.getRGB());
    this.client.textRenderer.draw(stack, text, (float) x, (float) y, color.getRGB());
  }
}
