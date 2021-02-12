package software.tachyon.starfruit.module.movement;

import net.engio.mbassy.listener.Handler;
import net.engio.mbassy.listener.Listener;
import net.engio.mbassy.listener.References;
import software.tachyon.starfruit.StarfruitMod;
import software.tachyon.starfruit.module.ModuleInfo;
import software.tachyon.starfruit.module.ModuleInfo.Category;
import software.tachyon.starfruit.module.StatefulModule;
import software.tachyon.starfruit.module.event.SprintChangeEvent;

@Listener(references = References.Strong)
public class Sprint extends StatefulModule {

  public Sprint(int keyCode) {
    super(keyCode, ModuleInfo.init().name("Sprint").category(Category.MOVEMENT).build());
  }

  @Override
  public void onEnable() {
    StarfruitMod.minecraft.player.setSprinting(true);
    super.onEnable();
  }

  @Override
  public void onDisable() {
    super.onDisable();
    StarfruitMod.minecraft.player.setSprinting(false);
  }

  @Handler
  public void onSprintStateChanged(SprintChangeEvent event) {
    event.setCancelled(true);
  }

}
