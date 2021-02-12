package software.tachyon.starfruit.mixin.world;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import software.tachyon.starfruit.StarfruitMod;
import software.tachyon.starfruit.module.event.gui.NametagRenderEvent;

@Mixin(EntityRenderer.class)
public class EntityRendererMixin<E extends Entity> {
    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void doRender(E entity, float entityYaw, float partialTicks, MatrixStack matrixStack,
            VertexConsumerProvider vertexConsumerProvider, int light, CallbackInfo ci) {
        final NametagRenderEvent<E> event = new NametagRenderEvent<>(entity);
        StarfruitMod.getModuleManager().getBus().post(event).now();
        if (event.isCancelled())
            ci.cancel();
    }
}
