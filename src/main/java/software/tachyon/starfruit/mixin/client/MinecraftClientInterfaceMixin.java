package software.tachyon.starfruit.mixin.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.Session;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.net.Proxy;

@Mixin(MinecraftClient.class)
public interface MinecraftClientInterfaceMixin {
    @Mutable
    @Accessor
    void setSession(Session session);

    @Accessor
    boolean getIntegratedServerRunning();

    @Accessor
    ParticleManager getParticleManager();

    @Accessor
    WorldRenderer getWorldRenderer();

    @Accessor
    Proxy getNetProxy();

    @Accessor
    void setItemUseCooldown(int itemUseCooldown);
}
