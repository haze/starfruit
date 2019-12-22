package software.tachyon.starfruit.module.movement;

import software.tachyon.starfruit.StarfruitMod;
import software.tachyon.starfruit.module.ModuleInfo;
import software.tachyon.starfruit.module.StatefulModule;
import software.tachyon.starfruit.module.ModuleInfo.Category;
import software.tachyon.starfruit.module.event.SprintChangeEvent;

import java.awt.Color;

import net.engio.mbassy.listener.Handler;
import net.engio.mbassy.listener.Listener;
import net.engio.mbassy.listener.References;

@Listener(references = References.Strong)
public class Sprint extends StatefulModule {

    public Sprint(int keyCode) {
        super(keyCode);
        this.info = new ModuleInfo.Builder()
          .name("Sprint")
          .color(Color.magenta)
          .category(Category.MOVEMENT)
          .build();
    }

  @Override
  public void onEnable() {
    StarfruitMod.minecraft.getPlayer().setSprinting(true);
    super.onEnable();
  }

  @Override
  public void onDisable() {
    super.onDisable();
    StarfruitMod.minecraft.getPlayer().setSprinting(false);
  }

  @Handler
  public void onSprintStateChanged(SprintChangeEvent event) {
    event.setCancelled(true);
  }

    
}
