package software.tachyon.starfruit.module.movement;

import net.engio.mbassy.listener.Handler;
import net.engio.mbassy.listener.Listener;
import software.tachyon.starfruit.StarfruitMod;
import software.tachyon.starfruit.module.ModuleInfo;
import software.tachyon.starfruit.module.StatefulModule;
import software.tachyon.starfruit.module.ModuleInfo.Category;
import software.tachyon.starfruit.module.event.gui.InGameHudDrawEvent;
import net.engio.mbassy.listener.References;

@Listener(references = References.Strong)
public class Flight extends StatefulModule {

  public Flight(int keyCode) {
    super(keyCode);
    this.info = new ModuleInfo.Builder().name("Flight").category(Category.MOVEMENT).build();
  }

  @Override
  public void onEnable() {
    super.onEnable();
  }

  @Override
  public void onDisable() {
    super.onDisable();
    StarfruitMod.minecraft.player.abilities.flying = false;
  }

  @Handler
    public void onScreenDraw(InGameHudDrawEvent event) {
        StarfruitMod.minecraft.player.abilities.flying = true;
        StarfruitMod.minecraft.player.abilities.setFlySpeed(0.1F);
    }

}
