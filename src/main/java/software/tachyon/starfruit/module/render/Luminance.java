package software.tachyon.starfruit.module.render;

import software.tachyon.starfruit.StarfruitMod;
import software.tachyon.starfruit.mixin.MinecraftClientMixin;
import software.tachyon.starfruit.mixin.world.DimensionMixin;
import software.tachyon.starfruit.module.ModuleInfo;
import software.tachyon.starfruit.module.StatefulModule;
import software.tachyon.starfruit.module.ModuleInfo.Category;

import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.Future;

import net.engio.mbassy.listener.Listener;
import net.engio.mbassy.listener.References;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.dimension.Dimension;

@Listener(references = References.Strong)
public class Luminance extends StatefulModule {

    class AdjustLuminanceTask implements Runnable {
        private final float desiredLightmapValue;
        private float[] initialLightmap;
        private long startMS;
        private final MinecraftClient client;

        AdjustLuminanceTask(float desiredLightmapValue, MinecraftClient client) {
            this.desiredLightmapValue = desiredLightmapValue;
            this.client = client;
        }

        boolean isFinished() {
            return Thread.interrupted() || System.currentTimeMillis() - this.startMS > 1500;
        }

        private float bounceOut(float x) {
            float n1 = 7.5625F;
            float d1 = 2.75F;
        
            if (x < 1 / d1) {
                return n1 * x * x;
            } else if (x < 2 / d1) {
                return n1 * (x -= 1.5F / d1) * x + 0.75F;
            } else if (x < 2.5 / d1) {
                return n1 * (x -= 2.25F / d1) * x + 0.9375F;
            } else {
                return n1 * (x -= 2.625F / d1) * x + 0.984375F;
            }
        }

        private float easeInOutBounce(float x) {
            return x < 0.5F ? (1F - bounceOut(1F - 2F * x)) / 2F : (1F + bounceOut(2F * x - 1F)) / 2F;
        }
    

        public void run() {
            DimensionMixin dim = ((DimensionMixin)StarfruitMod.minecraft.world.dimension);
            this.startMS = System.currentTimeMillis();
            while (!this.isFinished()) {
                final long timeDiff = System.currentTimeMillis() - this.startMS;
                final float eased = bounceOut(timeDiff / 1500F);
                for(int i = 0; i < dim.getLightLevelToBrightness().length; i++) {
                    dim.getLightLevelToBrightness()[i] = MathHelper.lerp(eased, this.initialLightmap[i], Math.max(this.desiredLightmapValue, this.initialLightmap[i]));
                }
                // curMS = System.currentTimeMillis();
            }
        }

        public void setInitialLightmap(float[] lightmap) {
            this.initialLightmap = lightmap;
        }
    }

    private AdjustLuminanceTask adjustLuminanceTask = null;
    private Optional<Future<Void>> runningAdjustmentTask = Optional.empty();
    private double savedGamma = 0;

    private float[] initialLightmap = null;

    public Luminance(int keyCode) {
        super(keyCode);
        this.info = new ModuleInfo.Builder().name("Luminance").color(StarfruitMod.Colors.moduleColor(0.18F))
                .category(Category.MOVEMENT).build();
        this.adjustLuminanceTask = new AdjustLuminanceTask(0.2F, StarfruitMod.minecraft);
    }

    @Override
    @SuppressWarnings("unchecked") // TODO(haze): is this really needed?
    public void onEnable() {
        this.initialLightmap = ((DimensionMixin)StarfruitMod.minecraft.world.dimension).getLightLevelToBrightness().clone();
        this.savedGamma = StarfruitMod.minecraft.options.gamma;
        this.adjustLuminanceTask.setInitialLightmap(this.initialLightmap);
        this.runningAdjustmentTask = Optional
                .of((Future<Void>) StarfruitMod.getModuleManager().getThreadPool().submit(this.adjustLuminanceTask));
        super.onEnable();
    }

    @Override
    public void onDisable() {
        this.runningAdjustmentTask.ifPresent(task -> task.cancel(true)); // cancel running task if there is any
        this.runningAdjustmentTask = Optional.empty();
        super.onDisable();
        StarfruitMod.minecraft.options.gamma = this.savedGamma;
        this.savedGamma = 0;
    }
}
