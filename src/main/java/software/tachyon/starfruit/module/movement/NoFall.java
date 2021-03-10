package software.tachyon.starfruit.module.movement;

import net.engio.mbassy.listener.Handler;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket.Mode;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import software.tachyon.starfruit.StarfruitMod;
import software.tachyon.starfruit.mixin.network.PlayerMoveC2SPacketMixin;
import software.tachyon.starfruit.module.ModuleInfo;
import software.tachyon.starfruit.module.StatefulModule;
import software.tachyon.starfruit.module.event.SendPacketEvent;

public class NoFall extends StatefulModule {
  boolean mutated = false;

  public NoFall(Integer defaultKeyCode) {
    super(defaultKeyCode, ModuleInfo.init().name("Angel Toes").hidden(true).build());
  }

  @Override
  public void onDisable() {
    StarfruitMod.minecraft.player.setSneaking(this.mutated);
    super.onDisable();
  }

  @Handler
  <T extends PacketListener> void onPacketSend(SendPacketEvent<T> event) {
    if (event.getPacket() instanceof PlayerMoveC2SPacket) {
      ((PlayerMoveC2SPacketMixin) event.getPacket()).setOnGround(true);
    } else if (event.getPacket() instanceof ClientCommandC2SPacket) {
      final ClientCommandC2SPacket packet = (ClientCommandC2SPacket) event.getPacket();
      if (packet.getMode() == Mode.PRESS_SHIFT_KEY) {
        this.mutated = true;
      } else if (packet.getMode() == Mode.RELEASE_SHIFT_KEY) {
        this.mutated = false;
      }
      event.setCancelled(true);
    }
  }
}
