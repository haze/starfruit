package software.tachyon.starfruit.mixin.client;

import com.mojang.authlib.GameProfile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.GameMode;
import software.tachyon.starfruit.StarfruitMod;
import software.tachyon.starfruit.module.render.Camera;

@Mixin(AbstractClientPlayerEntity.class)
public abstract class AbstractClientPlayerEntityMixin extends PlayerEntity {

    protected AbstractClientPlayerEntityMixin() {
        super(null, null, 0, null);
        throw new UnsupportedOperationException();
    }

    @Overwrite
    public boolean isSpectator() {
        if (this.getEntityId() == StarfruitMod.minecraft.player.getEntityId()
                && StarfruitMod.getModuleManager().getStatefulModule(Camera.class).getState()) {
            return true;
        }
        return originalIsSpectator();
    }

    boolean originalIsSpectator() {
        PlayerListEntry playerListEntry = StarfruitMod.minecraft.getNetworkHandler()
                .getPlayerListEntry(this.getGameProfile().getId());
        return playerListEntry != null && playerListEntry.getGameMode() == GameMode.SPECTATOR;
    }
}
