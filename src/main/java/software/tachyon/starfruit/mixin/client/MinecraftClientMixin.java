package software.tachyon.starfruit.mixin.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import software.tachyon.starfruit.StarfruitMod;
import software.tachyon.starfruit.module.event.TickEvent;
import software.tachyon.starfruit.module.event.TickEvent.State;
import software.tachyon.starfruit.module.event.WorldLoadEvent;

import java.io.File;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {
    @Shadow
    public ClientWorld world;

    @Final
    @Shadow
    public File runDirectory;

    @Shadow
    protected abstract void reset(Screen screen);

    @Inject(method = "tick", at = @At("HEAD"))
    public void preTick(CallbackInfo ci) {
        StarfruitMod.getModuleManager().getBus().post(new TickEvent(State.PRE)).now();
    }

    @Inject(method = "tick", at = @At("RETURN"))
    public void postTick(CallbackInfo ci) {
        StarfruitMod.getModuleManager().getBus().post(new TickEvent(State.POST)).now();
    }

    @Inject(method = "setWorld", at = @At("HEAD"))
    private void onSetWorld(ClientWorld world, CallbackInfo ci) {
        StarfruitMod.getModuleManager().getBus().post(new WorldLoadEvent()).asynchronously();
    }
}
