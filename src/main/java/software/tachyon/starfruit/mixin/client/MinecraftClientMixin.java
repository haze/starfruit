package software.tachyon.starfruit.mixin.client;

import com.mojang.authlib.AuthenticationService;
import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.block.entity.SkullBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ProgressScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.UserCache;
import software.tachyon.starfruit.StarfruitMod;
import software.tachyon.starfruit.module.event.TickEvent;
import software.tachyon.starfruit.module.event.WorldLoadEvent;
import software.tachyon.starfruit.module.event.TickEvent.State;
import java.io.File;
import java.util.UUID;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {
    @Shadow
    private ClientWorld world;

    @Shadow
    private File runDirectory;

    @Shadow
    public abstract void reset(Screen screen);

    @Inject(method = "tick", at = @At("HEAD"))
    public void preTick(CallbackInfo ci) {
        StarfruitMod.getModuleManager().getBus().post(new TickEvent(State.PRE)).now();
    }

    @Inject(method = "tick", at = @At("RETURN"))
    public void postTick(CallbackInfo ci) {
        StarfruitMod.getModuleManager().getBus().post(new TickEvent(State.POST)).now();
    }

    @Overwrite
    public void joinWorld(ClientWorld world) {
        final boolean isNull = StarfruitMod.minecraft.world == null;
        final MinecraftClientInterfaceMixin mixin = (MinecraftClientInterfaceMixin) this;
        ProgressScreen progressScreen = new ProgressScreen();
        progressScreen.method_15412(new TranslatableText("connect.joining", new Object[0]));
        this.reset(progressScreen);
        mixin.getWorldRenderer().setWorld(world);
        mixin.getParticleManager().setWorld(world);
        BlockEntityRenderDispatcher.INSTANCE.setWorld(world);
        this.world = world;
        if (isNull)
            StarfruitMod.getModuleManager().getBus().post(new WorldLoadEvent()).asynchronously();
        if (!mixin.getIsIntegratedServerRunning()) {
            AuthenticationService authenticationService = new YggdrasilAuthenticationService(
                    mixin.getNetProxy(), UUID.randomUUID().toString());
            MinecraftSessionService minecraftSessionService =
                    authenticationService.createMinecraftSessionService();
            GameProfileRepository gameProfileRepository =
                    authenticationService.createProfileRepository();
            UserCache userCache = new UserCache(gameProfileRepository,
                    new File(this.runDirectory, MinecraftServer.USER_CACHE_FILE.getName()));
            SkullBlockEntity.setUserCache(userCache);
            SkullBlockEntity.setSessionService(minecraftSessionService);
            UserCache.setUseRemote(false);
        }
    }
}
