package software.tachyon.starfruit.mixin.network;

import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(EntityVelocityUpdateS2CPacket.class)
public interface EntityVelocityUpdateS2CPacketMixin {

    @Accessor
    void setVelocityX(int velocityX);

    @Accessor
    void setVelocityY(int velocityY);

    @Accessor
    void setVelocityZ(int velocityZ);

    @Accessor
    int getId();

    @Accessor
    int getVelocityX();

    @Accessor
    int getVelocityY();

    @Accessor
    int getVelocityZ();
}
