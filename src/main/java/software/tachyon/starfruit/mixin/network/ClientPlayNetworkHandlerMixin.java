package software.tachyon.starfruit.mixin.network;

import net.engio.mbassy.bus.MBassador;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.packet.GameJoinS2CPacket;
import net.minecraft.network.Packet;
import net.minecraft.network.listener.PacketListener;
import software.tachyon.starfruit.StarfruitMod;
import software.tachyon.starfruit.module.event.GameJoinEvent;
import software.tachyon.starfruit.module.event.SendPacketEvent;
import software.tachyon.starfruit.module.event.api.Event;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
class ClientPlayNetworkHandlerMixin {

    @Inject(at = @At("HEAD"), method = "sendPacket", cancellable = true)
    <T extends PacketListener> void sendPacket(Packet<T> packet, CallbackInfo ci) {
        final SendPacketEvent<T> event = new SendPacketEvent<>(packet);

        MBassador<Event> bus = StarfruitMod.getModuleManager().getBus();
        bus.post(event).now();

        if (event.isCancelled()) {
            ci.cancel();
        }
    }

    @Inject(at = @At("RETURN"), method = "onGameJoin")
    public void onGameJoin(GameJoinS2CPacket packet, CallbackInfo ci) {
        StarfruitMod.getModuleManager().getBus().post(new GameJoinEvent()).now();
    }
}
