package software.tachyon.starfruit.module.render;

import net.engio.mbassy.listener.Handler;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import software.tachyon.starfruit.StarfruitMod;
import software.tachyon.starfruit.module.ModuleInfo;
import software.tachyon.starfruit.module.ModuleInfo.Category;
import software.tachyon.starfruit.module.StatefulModule;
import software.tachyon.starfruit.module.event.SendPacketEvent;
import software.tachyon.starfruit.module.event.TickEvent;
import software.tachyon.starfruit.module.event.TickEvent.State;
import software.tachyon.starfruit.module.variable.Variable;

public class Camera extends StatefulModule {
  public Variable.Dbl speed;
  boolean wasFlyingBefore = false;

  public Camera(Integer defaultKeyCode) {
    super(defaultKeyCode, ModuleInfo.init().name("Camera").category(Category.RENDER).build());
    this.speed = new Variable.Dbl(0.4);
  }

  @Handler
  public void onTick(TickEvent event) {
    if (event.getState() == State.POST) {
      StarfruitMod.minecraft.player.flyingSpeed = (float) (double) this.speed.get();
      StarfruitMod.minecraft.player.abilities.flying = true;
      StarfruitMod.minecraft.player.noClip = true;
    }
  }

  @Handler
  public <T extends PacketListener> void onSendPacket(SendPacketEvent<T> event) {
    if (event.getPacket() instanceof PlayerMoveC2SPacket) {
      event.setCancelled(true);
    }
  }

  @Override
  public void onDisable() {
    StarfruitMod.minecraft.player.abilities.flying = this.wasFlyingBefore;
    super.onDisable();
  }

  @Override
  public void onEnable() {
    super.onEnable();
    this.wasFlyingBefore = StarfruitMod.minecraft.player.abilities.flying;
  }
}
