
package software.tachyon.starfruit.module.event;

import net.minecraft.network.Packet;
import net.minecraft.network.listener.PacketListener;
import software.tachyon.starfruit.module.event.api.CancellableEvent;

public class RecvPacketEvent<T extends PacketListener> extends CancellableEvent {

    private final Packet<T> packet;

    public RecvPacketEvent(Packet<T> packet) {
        this.packet = packet;
    }

    public Packet<T> getPacket() {
        return this.packet;
    }
}
