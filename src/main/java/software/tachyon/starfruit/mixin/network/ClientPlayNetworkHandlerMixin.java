package software.tachyon.starfruit.mixin.network;

import net.engio.mbassy.bus.MBassador;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.GameJoinS2CPacket;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.Packet;
import net.minecraft.network.listener.PacketListener;
import software.tachyon.starfruit.StarfruitMod;
import software.tachyon.starfruit.module.event.GameJoinEvent;
import software.tachyon.starfruit.module.event.SendPacketEvent;
import software.tachyon.starfruit.module.event.api.Event;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class ClientPlayNetworkHandlerMixin {

    @Shadow
    @Final
    private ClientConnection connection;

    /// Mumphrey suck my cock
    @Overwrite
    public <T extends PacketListener> void sendPacket(Packet<T> packet) {
        final SendPacketEvent<T> event = new SendPacketEvent<>(packet);

        MBassador<Event> bus = StarfruitMod.getModuleManager().getBus();
        bus.post(event).now();

        if (!event.isCancelled()) {
            this.connection.send(event.getPacket());
        }
    }

    @Inject(at = @At("RETURN"), method = "onGameJoin")
    public void onGameJoin(GameJoinS2CPacket packet, CallbackInfo ci) {
        StarfruitMod.getModuleManager().getBus().post(new GameJoinEvent()).now();
    }
}
