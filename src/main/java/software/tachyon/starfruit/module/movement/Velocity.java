package software.tachyon.starfruit.module.movement;

import software.tachyon.starfruit.StarfruitMod;
import software.tachyon.starfruit.mixin.network.EntityVelocityUpdateS2CPacketMixin;
import software.tachyon.starfruit.module.ModuleInfo;
import software.tachyon.starfruit.module.StatefulModule;
import software.tachyon.starfruit.module.ModuleInfo.Category;
import software.tachyon.starfruit.module.event.RecvPacketEvent;
import software.tachyon.starfruit.module.variable.Variable;
import net.engio.mbassy.listener.Handler;
import net.engio.mbassy.listener.Listener;
import net.engio.mbassy.listener.References;
import net.minecraft.client.network.packet.EntityVelocityUpdateS2CPacket;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.network.listener.ServerPlayPacketListener;
import net.minecraft.server.network.ServerPlayNetworkHandler;

@Listener(references = References.Strong)
public class Velocity extends StatefulModule {

  public final Variable.Dbl modifier;

  public Velocity(int keyCode) {
    super(keyCode);
    this.info = new ModuleInfo.Builder().name("Velocity").category(Category.MOVEMENT).build();
    this.modifier = new Variable.Dbl(0.00);
  }

  @Handler
  public <T extends PacketListener> void onPacketRecv(RecvPacketEvent<T> event) {
    if (event.getPacket() instanceof EntityVelocityUpdateS2CPacket) {
      final EntityVelocityUpdateS2CPacketMixin packet = (EntityVelocityUpdateS2CPacketMixin) event.getPacket();
      if (packet.getId() == StarfruitMod.minecraft.player.getEntityId()) {
        packet.setVelocityX((int) Math.floor(packet.getVelocityX() * this.modifier.get()));
        packet.setVelocityY((int) Math.floor(packet.getVelocityX() * this.modifier.get()));
        packet.setVelocityZ((int) Math.floor(packet.getVelocityX() * this.modifier.get()));
      }
    }
  }
}