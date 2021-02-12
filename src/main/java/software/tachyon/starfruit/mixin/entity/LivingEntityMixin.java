package software.tachyon.starfruit.mixin.entity;

import net.engio.mbassy.bus.MBassador;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import software.tachyon.starfruit.StarfruitMod;
import software.tachyon.starfruit.module.event.SprintChangeEvent;
import software.tachyon.starfruit.module.event.api.Event;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {
    @Inject(method = "setSprinting", at = @At("HEAD"), cancellable = true)
    public void setSprinting(boolean sprinting, CallbackInfo ci) {
        final SprintChangeEvent event = new SprintChangeEvent(sprinting);
        MBassador<Event> bus = StarfruitMod.getModuleManager().getBus();
        bus.post(event).now();

        if (event.isCancelled()) {
            ci.cancel();
        }
    }
}
