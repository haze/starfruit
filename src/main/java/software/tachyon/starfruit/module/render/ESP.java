package software.tachyon.starfruit.module.render;

import java.awt.Color;
import java.util.Optional;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;

import org.lwjgl.opengl.GL11;

import net.engio.mbassy.listener.Handler;
import net.engio.mbassy.listener.Listener;
import net.engio.mbassy.listener.References;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.util.Window;
import net.minecraft.client.util.math.Vector3d;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import software.tachyon.starfruit.StarfruitMod;
import software.tachyon.starfruit.module.ModuleInfo;
import software.tachyon.starfruit.module.StatefulModule;
import software.tachyon.starfruit.module.ModuleInfo.Category;
import software.tachyon.starfruit.module.event.gui.InGameHudDrawEvent;
import software.tachyon.starfruit.module.variable.Variable;
import software.tachyon.starfruit.utility.ProjectionUtility;

@Listener(references = References.Strong)
public class ESP extends StatefulModule {

  public final Variable.Bool items;
  public final Variable.Bool players;
  public final Variable.Bool mobs;
  public final Variable.Bool animals;

  public final Variable.Dbl tracerWidth;
  public final Variable.Bool tracers;

  public ESP(int keyCode) {
    super(keyCode);
    this.info = ModuleInfo.init().name("ESP").category(Category.RENDER).build();

    this.items = new Variable.Bool(false);
    this.players = new Variable.Bool(true);
    this.mobs = new Variable.Bool(false);
    this.animals = new Variable.Bool(false);

    this.tracerWidth = new Variable.Dbl(1.3);
    this.tracers = new Variable.Bool(true);
  }

  void renderSetup() {
    RenderSystem.pushMatrix();
    RenderSystem.lineWidth((float) (double) this.tracerWidth.get());

    RenderSystem.disableDepthTest();
    RenderSystem.disableLighting();
    RenderSystem.disableTexture();
    RenderSystem.enableBlend();
    RenderSystem.blendFuncSeparate(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA,
        GlStateManager.SrcFactor.ONE, GlStateManager.DstFactor.ZERO);
  }

  void renderTeardown() {
    GL11.glDisable(GL11.GL_LINE_SMOOTH);
    RenderSystem.disableBlend();
    RenderSystem.popMatrix();
  }

  Color colorize(Entity ent) {
    if (ent instanceof PlayerEntity && ((PlayerEntity) ent).isSneaking())
      return Color.getHSBColor(0, 0.3F, 1F);
    final double n = StarfruitMod.minecraft.player.distanceTo(ent);
    final float hue = (float) MathHelper.lerp(n / 50, 0, 0.35F);
    return Color.getHSBColor(hue, 0.7F, 1F);
    // return StarfruitMod.getGlobalIridescence();
  }

  boolean shouldDraw(Entity ent) {
    if (ent instanceof PlayerEntity && this.players.get())
      return true;
    if (ent instanceof AnimalEntity && this.animals.get())
      return true;
    if (ent instanceof MobEntity && this.mobs.get())
      return true;
    if (ent instanceof ItemEntity && this.items.get())
      return true;

    return false;
  }

  void drawTracer(InGameHudDrawEvent event, Entity ent) {
    if (ent == StarfruitMod.minecraft.player)
      return;
    if (!shouldDraw(ent))
      return;
    final double x = MathHelper.lerp(event.getPartialTicks(), ent.lastRenderX, ent.getPos().getX());
    final double y = MathHelper.lerp(event.getPartialTicks(), ent.lastRenderY, ent.getPos().getY());
    final double z = MathHelper.lerp(event.getPartialTicks(), ent.lastRenderZ, ent.getPos().getZ());
    final Vec3d interpolatedPos = new Vec3d(x, y, z);
    final Window window = StarfruitMod.minecraft.getWindow();
    try {
      final Optional<Vector3f> feetScreenPos = ProjectionUtility.project(x, y, z, true);
      final Optional<Vector3f> eyeScreenPos = ProjectionUtility.project(x, y + ent.getEyeHeight(ent.getPose()), z,
          false);
      feetScreenPos.ifPresent(pos -> {
        this.renderSetup();

        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST);

        final Color col = colorize(ent);
        RenderSystem.color3f(col.getRed() / 255F, col.getGreen() / 255F, col.getBlue() / 255F);

        GL11.glBegin(GL11.GL_LINE_STRIP);
        // center of screen
        GL11.glVertex2d(window.getScaledWidth() * 0.5F, window.getScaledHeight() * 0.5F);
        GL11.glVertex2d(pos.getX(), pos.getY());
        eyeScreenPos.ifPresent(eyePos -> {
          if (ent instanceof PlayerEntity && ((PlayerEntity) ent).isSneaking()) {
            final PlayerEntity entPlayer = (PlayerEntity) ent;
            final Vec3d lookVec = Vec3d.fromPolar(0.0F, (float) entPlayer.bodyYaw);
            final Vector3f shiftedFeedPos = ProjectionUtility
                .project(interpolatedPos.subtract(lookVec.multiply(0.2)), false).orElseGet(() -> feetScreenPos.get());

            GL11.glVertex2d(shiftedFeedPos.getX(), shiftedFeedPos.getY());

            // compute ass position
            final Vector3f assPos = ProjectionUtility
                .project(interpolatedPos.add(0.0, 2.5 / 5, 0.0).subtract(lookVec.multiply(0.35)), true).get();
            final Optional<Vector3f> sneakingEyePos = ProjectionUtility.project(
                interpolatedPos.add(0.0, ent.getEyeHeight(ent.getPose()), 0.0).add(lookVec.multiply(0.20)), false);

            GL11.glVertex2d(assPos.getX(), assPos.getY());
            sneakingEyePos.ifPresent(p -> GL11.glVertex2d(p.getX(), p.getY()));
          } else {
            GL11.glVertex2d(eyePos.getX(), eyePos.getY());
          }
        });

        GL11.glEnd();
        this.renderTeardown();
      });
    } catch (Throwable t) {
      t.printStackTrace();
    }
  }

  @Handler
  public void onScreenDraw(InGameHudDrawEvent event) {
    if (event.getState() == InGameHudDrawEvent.State.POST) {
      for (Entity ent : StarfruitMod.minecraft.world.getEntities()) {
        if (this.tracers.get())
          this.drawTracer(event, ent);
      }
    }
  }
}