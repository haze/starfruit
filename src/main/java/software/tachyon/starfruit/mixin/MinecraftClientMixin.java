package software.tachyon.starfruit.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.Session;

@Mixin(MinecraftClient.class)
public interface MinecraftClientMixin {
    @Accessor
    void setSession(Session session);
}
