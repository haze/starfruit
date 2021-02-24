package software.tachyon.starfruit.mixin.network;

import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PlayerMoveC2SPacket.class)
public interface PlayerMoveC2SPacketMixin {
    // setOnGround
    @Accessor
    void setField_29179(boolean onGround);
}
