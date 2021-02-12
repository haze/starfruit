package software.tachyon.starfruit.mixin.world;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import software.tachyon.starfruit.StarfruitMod;
import software.tachyon.starfruit.module.event.render.BeforeHandRenderEvent;
import software.tachyon.starfruit.utility.ProjectionUtility;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
    @Inject(method = "renderWorld(FJLnet/minecraft/client/util/math/MatrixStack;)V", at = @At(value = "INVOKE_STRING", target = "Lnet/minecraft/util/profiler/Profiler;swap(Ljava/lang/String;)V", args = "ldc=hand", shift = At.Shift.BEFORE))
    private void preRenderHand(float partialTicks, long finishTimeNano, MatrixStack matrixStack, CallbackInfo ci) {
        StarfruitMod.getModuleManager().getBus().post(new BeforeHandRenderEvent()).now();
    }

    @Inject(method = "renderWorld(FJLnet/minecraft/client/util/math/MatrixStack;)V", at = @At(value = "INVOKE_STRING", target = "Lnet/minecraft/util/profiler/Profiler;swap(Ljava/lang/String;)V", args = "ldc=hand", shift = At.Shift.BEFORE))
    private void renderWorld(float f, long j, MatrixStack stack, CallbackInfo ci) {
        RenderSystem.pushMatrix();
        ProjectionUtility.updateViewport(stack, StarfruitMod.minecraft.getWindow());
        RenderSystem.popMatrix();
    }
}
