package software.tachyon.starfruit.mixin.network;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;

import net.engio.mbassy.bus.MBassador;
import net.minecraft.network.packet.s2c.play.ContainerSlotUpdateS2CPacket;;
import net.minecraft.network.packet.s2c.play.InventoryS2CPacket;
import net.minecraft.container.Container;
import net.minecraft.network.NetworkThreadUtils;
import net.minecraft.network.Packet;
import net.minecraft.network.listener.PacketListener;
import software.tachyon.starfruit.StarfruitMod;
import software.tachyon.starfruit.module.event.RecvPacketEvent;
import software.tachyon.starfruit.module.event.api.Event;
import software.tachyon.starfruit.module.event.player.InventoryUpdateEvent;

@Mixin(NetworkThreadUtils.class)
public class NetworkThreadUtilsMixin {
    @Inject(method = "method_11072", cancellable = true, at = @At(value = "INVOKE",
            target = "Lnet/minecraft/network/Packet;apply(Lnet/minecraft/network/listener/PacketListener;)V"))
    private static <T extends PacketListener> void redirectHandlePacketPre(PacketListener handler,
            Packet<T> packet, CallbackInfo ci) {
        final RecvPacketEvent<T> event = new RecvPacketEvent<T>(packet);

        MBassador<Event> bus = StarfruitMod.getModuleManager().getBus();
        bus.post(event).now();

        if (event.isCancelled()) {
            ci.cancel();
        }
    }

    @Inject(method = "method_11072", cancellable = true, at = @At(value = "RETURN"))
    private static <T extends PacketListener> void redirectHandlePacketPost(PacketListener handler,
            Packet<T> packet, CallbackInfo ci) {
        MBassador<Event> bus = StarfruitMod.getModuleManager().getBus();
        if (packet instanceof Container) {
            bus.post(new InventoryUpdateEvent((ContainerSlotUpdateS2CPacket) packet)).now();
        }
    }
}
