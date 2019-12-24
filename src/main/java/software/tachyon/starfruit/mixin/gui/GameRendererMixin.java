package software.tachyon.starfruit.mixin.gui;

import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import software.tachyon.starfruit.StarfruitMod;
import software.tachyon.starfruit.utility.ProjectionUtility;

@Mixin(GameRenderer.class)
public class GameRendererMixin {

    @Shadow
    @Final
    public Camera camera;

    @Inject(method = "renderWorld(FJLnet/minecraft/client/util/math/MatrixStack;)V", at = @At(value = "INVOKE_STRING", target = "Lnet/minecraft/util/profiler/Profiler;swap(Ljava/lang/String;)V", args = "ldc=hand", shift = At.Shift.BEFORE))
    private void renderWorld(float f, long j, MatrixStack stack, CallbackInfo ci) {
        GL11.glPushMatrix();

        GL11.glColor4d(1, 1, 1, 1);
        GL11.glRotated(camera.getPitch(), 1, 0, 0);
        GL11.glRotated(camera.getYaw() - 180, 0, 1, 0);

        ProjectionUtility.updateViewport(StarfruitMod.minecraft.getWindow());

        // Energetic.getEnergetic().getEventManager().fireEvent(new
        // RenderWorldEvent(matrixStack, partialTicks));
        GL11.glPopMatrix();
    }
}
