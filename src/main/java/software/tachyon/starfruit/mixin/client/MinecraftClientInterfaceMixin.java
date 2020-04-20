package software.tachyon.starfruit.mixin.client;

import java.net.Proxy;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.Session;

@Mixin(MinecraftClient.class)
public interface MinecraftClientInterfaceMixin {
    @Accessor
    public void setSession(Session session);

    @Accessor
    public boolean getIsIntegratedServerRunning();

    @Accessor
    public ParticleManager getParticleManager();

    @Accessor
    public WorldRenderer getWorldRenderer();

    @Accessor
    public Proxy getNetProxy();

    @Accessor
    void setItemUseCooldown(int itemUseCooldown);
}
