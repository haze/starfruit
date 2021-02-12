package software.tachyon.starfruit.module.movement;

import net.engio.mbassy.listener.Handler;
import net.engio.mbassy.listener.Listener;
import net.engio.mbassy.listener.References;
import software.tachyon.starfruit.StarfruitMod;
import software.tachyon.starfruit.module.ModuleInfo;
import software.tachyon.starfruit.module.ModuleInfo.Category;
import software.tachyon.starfruit.module.StatefulModule;
import software.tachyon.starfruit.module.event.TickEvent;
import software.tachyon.starfruit.module.event.TickEvent.State;
import software.tachyon.starfruit.module.variable.Variable;

@Listener(references = References.Strong)
public class Flight extends StatefulModule {

  public final Variable.Dbl speed;


  public Flight(int keyCode) {
    super(keyCode, ModuleInfo.init().name("Flight").category(Category.MOVEMENT).build());
    this.speed = new Variable.Dbl(0.1);
  }

  @Override
  public void onDisable() {
    super.onDisable();
    if (StarfruitMod.minecraft.player.abilities.flying) {
      StarfruitMod.minecraft.player.abilities.flying = false;
    }
  }


  @Handler
  public void onScreenDraw(TickEvent event) {
    if (event.getState() == State.POST) {
      StarfruitMod.minecraft.player.abilities.flying = true;
      StarfruitMod.minecraft.player.abilities.setFlySpeed((float) (double) this.speed.get());
    }
  }

}
