package software.tachyon.starfruit.module.render;

import java.util.Optional;

import net.engio.mbassy.listener.Handler;
import net.engio.mbassy.listener.Listener;
import net.engio.mbassy.listener.References;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.entity.Entity;
import software.tachyon.starfruit.StarfruitMod;
import software.tachyon.starfruit.module.ModuleInfo;
import software.tachyon.starfruit.module.StatefulModule;
import software.tachyon.starfruit.module.ModuleInfo.Category;
import software.tachyon.starfruit.module.event.gui.InGameHudDrawEvent;
import software.tachyon.starfruit.utility.ProjectionUtility;

@Listener(references = References.Strong)
public class Tracer extends StatefulModule {
    public Tracer(int keyCode) {
        super(keyCode);
        this.info = ModuleInfo.init().name("Tracers").category(Category.RENDER).build();
    }

    @Handler
    public void onScreenDraw(InGameHudDrawEvent event) {
        if (event.getState() == InGameHudDrawEvent.State.PRE) {
            for (Entity ent : StarfruitMod.minecraft.world.getEntities()) {
                if (ent == StarfruitMod.minecraft.player)
                    continue;
                final float x = (float) ent.getPos().x;
                final float y = (float) ent.getPos().y;
                final float z = (float) ent.getPos().z;
                System.out.println(ent.getName().getString());
                try {
                    final Optional<Vector3f> screenPos = ProjectionUtility.project(x, y, z, false);
                    screenPos.ifPresent(pos -> {
                        System.out.printf("%.3f, %.3f, %.3f\n", pos.getX(), pos.getY(), pos.getZ());
                        final TextRenderer rend = StarfruitMod.minecraft.textRenderer;
                        rend.drawWithShadow(ent.getName().getString(), pos.getX(), pos.getY(),
                                StarfruitMod.getGlobalIridescence().getRGB());
                    });
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
        }
    }
}
