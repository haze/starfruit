package software.tachyon.starfruit.module.event;

import net.minecraft.network.Packet;
import net.minecraft.network.listener.PacketListener;
import software.tachyon.starfruit.module.event.api.CancellableEvent;

public class SendPacketEvent<T extends PacketListener> extends CancellableEvent {

    private Packet<T> packet;

    public SendPacketEvent(Packet<T> packet) {
        this.setPacket(packet);
    }

    public Packet<T> getPacket() {
        return this.packet;
    }

    public void setPacket(Packet<T> packet) {
        this.packet = packet;
    }
}
