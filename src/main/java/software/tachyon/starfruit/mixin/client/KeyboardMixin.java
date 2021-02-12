package software.tachyon.starfruit.mixin.client;

import net.minecraft.client.Keyboard;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import software.tachyon.starfruit.StarfruitMod;
import software.tachyon.starfruit.module.event.KeyPressEvent;

@Mixin(Keyboard.class)
public abstract class KeyboardMixin {

    @Inject(method = "onKey", at = @At("RETURN"), cancellable = true)
    public void onKey(long window, int key, int scancode, int i, int j, CallbackInfo ci) {
        if (this.getClient().currentScreen == null && i == 0) {
            final KeyPressEvent event = new KeyPressEvent(key);
            StarfruitMod.getModuleManager().getBus().post(event).now();
            if (event.isCancelled())
                ci.cancel();
        }
    }

    @Accessor
    public abstract MinecraftClient getClient();
}
