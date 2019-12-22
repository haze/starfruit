package software.tachyon.starfruit.mixin.entity;

import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.engio.mbassy.bus.MBassador;
import net.minecraft.entity.LivingEntity;
import software.tachyon.starfruit.module.ModuleManager;
import software.tachyon.starfruit.module.event.SprintChangeEvent;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {
    @Inject(method = "setSprinting", at = @At("HEAD"), cancellable = true)
    public void setSprinting(boolean sprinting, CallbackInfo ci) {
       final SprintChangeEvent event = new SprintChangeEvent(sprinting);
       MBassador bus = ModuleManager.getModuleManager().getBus();
       bus.post(event).now();

       if (event.isCancelled()) {
           ci.cancel();
       }
    }
}
