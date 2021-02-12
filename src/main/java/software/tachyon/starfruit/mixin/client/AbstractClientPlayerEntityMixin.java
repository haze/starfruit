package software.tachyon.starfruit.mixin.client;

import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import software.tachyon.starfruit.StarfruitMod;
import software.tachyon.starfruit.module.render.Camera;

@Mixin(AbstractClientPlayerEntity.class)
public abstract class AbstractClientPlayerEntityMixin extends PlayerEntity {

    protected AbstractClientPlayerEntityMixin() {
        super(null, null, 0, null);
        throw new UnsupportedOperationException();
    }

    @Inject(method = "isSpectator", at = @At("HEAD"), cancellable = true)
    public void onIsSpectator(CallbackInfoReturnable<Boolean> cir) {
        if (this.getEntityId() == StarfruitMod.minecraft.player.getEntityId()
                && StarfruitMod.getModuleManager().getStatefulModule(Camera.class).getState()) {
            cir.setReturnValue(true);
        }
    }
}
