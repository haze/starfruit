package software.tachyon.starfruit.mixin.network;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import net.minecraft.server.network.packet.ClientCommandC2SPacket;

@Mixin(ClientCommandC2SPacket.class)
public interface ClientCommandC2SPacketMixin {
    @Accessor
    void setMode(ClientCommandC2SPacket.Mode mode);
}
