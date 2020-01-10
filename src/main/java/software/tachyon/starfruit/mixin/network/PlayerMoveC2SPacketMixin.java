package software.tachyon.starfruit.mixin.network;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import net.minecraft.server.network.packet.PlayerMoveC2SPacket;

@Mixin(PlayerMoveC2SPacket.class)
public interface PlayerMoveC2SPacketMixin {

    @Accessor
    public void setOnGround(boolean onGround);
}
