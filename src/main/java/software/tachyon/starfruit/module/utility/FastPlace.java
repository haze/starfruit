package software.tachyon.starfruit.module.utility;

import net.engio.mbassy.listener.Handler;
import software.tachyon.starfruit.StarfruitMod;
import software.tachyon.starfruit.mixin.client.MinecraftClientInterfaceMixin;
import software.tachyon.starfruit.module.ModuleInfo;
import software.tachyon.starfruit.module.ModuleInfo.Category;
import software.tachyon.starfruit.module.StatefulModule;
import software.tachyon.starfruit.module.event.TickEvent;
import software.tachyon.starfruit.module.event.TickEvent.State;

public class FastPlace extends StatefulModule {
  public FastPlace(Integer defaultKeyCode) {
    super(defaultKeyCode, ModuleInfo.init().name("FastPlace").category(Category.UTILITY).build());
  }

  @Handler
  void onTick(TickEvent event) {
    if (event.getState() == State.POST) {
      ((MinecraftClientInterfaceMixin) StarfruitMod.minecraft).setItemUseCooldown(0);
    }
  }
}
