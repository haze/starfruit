package software.tachyon.starfruit.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.Keyboard;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.ParentElement;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.util.Session;
import software.tachyon.starfruit.module.ModuleManager;
import software.tachyon.starfruit.module.event.KeyPressEvent;

import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Keyboard.class)
public abstract class KeyboardMixin {

    @Inject(method = "onKey", at = @At("RETURN"))
    public void onKey(long window, int key, int scancode, int i, int j, CallbackInfo ci) {
      if (this.getClient().currentScreen == null && i == 0) {
          System.out.printf("I pressed the key %d", key);
          ModuleManager.getModuleManager().getBus().post(new KeyPressEvent(key)).asynchronously();
      }
    }

    @Accessor
    public abstract MinecraftClient getClient();
}
