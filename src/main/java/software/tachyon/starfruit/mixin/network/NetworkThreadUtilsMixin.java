package software.tachyon.starfruit.mixin.network;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;

import net.engio.mbassy.bus.MBassador;
import net.minecraft.network.NetworkThreadUtils;
import net.minecraft.network.Packet;
import net.minecraft.network.listener.PacketListener;
import software.tachyon.starfruit.StarfruitMod;
import software.tachyon.starfruit.module.event.RecvPacketEvent;
import software.tachyon.starfruit.module.event.api.Event;

@Mixin(NetworkThreadUtils.class)
public class NetworkThreadUtilsMixin {
    @Inject(method = "method_11072", cancellable = true, at = @At(value = "INVOKE", target = "Lnet/minecraft/network/Packet;apply(Lnet/minecraft/network/listener/PacketListener;)V"))
    private static <T extends PacketListener> void redirectHandlePacket(PacketListener handler, Packet<T> packet,
            CallbackInfo ci) {
        final RecvPacketEvent<T> event = new RecvPacketEvent<T>(packet);

        MBassador<Event> bus = StarfruitMod.getModuleManager().getBus();
        bus.post(event).now();

        if (event.isCancelled()) {
            ci.cancel();
        }
    }
}
