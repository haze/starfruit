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
        private Optional<Double> desiredLightmapValue = Optional.empty();
        private float[] initialLightmap;
        private float[] cachedLightmap;

        private long transitionTimeMS;
        private long startMS;

        AdjustLuminanceTask(long transitionTimeMS) {
            this.transitionTimeMS = transitionTimeMS;
        }

        boolean isFinished() {
            return Thread.interrupted() || System.currentTimeMillis() - this.startMS > this.transitionTimeMS;
        }

        void resetTimer() {
            this.startMS = System.currentTimeMillis();
        }

        private double easeFunc(double x) {
            return 1 - Math.sqrt(1 - Math.pow(x, 2));
        }

        public void run() {
            DimensionMixin dim = ((DimensionMixin) StarfruitMod.minecraft.world.dimension);
            this.resetTimer();
            while (!this.isFinished()) {
                final long timeDiff = System.currentTimeMillis() - this.startMS;
                // final double eased = easeFunc(timeDiff / (double) this.transitionTimeMS);
                final double eased = timeDiff / (double) this.transitionTimeMS;
                for (int i = 0; i < dim.getLightLevelToBrightness().length; i++) {
                    final double newValue = this.desiredLightmapValue.isPresent()
                            ? MathHelper.lerp(eased, this.initialLightmap[i],
                                    Math.max(this.desiredLightmapValue.get(), this.initialLightmap[i]))
                            : MathHelper.lerp(eased, this.cachedLightmap[i], this.initialLightmap[i]);
                    dim.getLightLevelToBrightness()[i] = (float) newValue;
                }
            }
        }

        public void setInitialLightmap(float[] lightmap) {
            this.initialLightmap = lightmap;
        }

        public void cacheLightmap() {
            DimensionMixin dim = ((DimensionMixin) StarfruitMod.minecraft.world.dimension);
            this.cachedLightmap = dim.getLightLevelToBrightness().clone();
        }

        public void setDesiredLightmapValue(Optional<Double> newValue) {
            this.desiredLightmapValue = newValue;
        }
    }

    private AdjustLuminanceTask adjustLuminanceTask = null;
    private Optional<Future<Void>> runningAdjustmentTask = Optional.empty();

    private float[] initialLightmap = null;

    public Luminance(int keyCode) {
        super(keyCode);
        this.info = new ModuleInfo.Builder().name("Luminance").color(StarfruitMod.Colors.moduleColor(0.18F))
                .category(Category.MOVEMENT).build();
        this.adjustLuminanceTask = new AdjustLuminanceTask(750);
    }

    @Override
    public void onEnable() {
        this.initialLightmap = ((DimensionMixin) StarfruitMod.minecraft.world.dimension).getLightLevelToBrightness()
                .clone();
        this.adjustLuminanceTask.setDesiredLightmapValue(Optional.of(0.25));
        if (!this.isTaskRunning())
            this.adjustLuminanceTask.setInitialLightmap(this.initialLightmap);
        this.launchAdjustmentTask();
        super.onEnable();
    }

    @SuppressWarnings("unchecked")
    void launchAdjustmentTask() {
        this.runningAdjustmentTask = Optional
                .of((Future<Void>) StarfruitMod.getModuleManager().getThreadPool().submit(this.adjustLuminanceTask));
    }

    boolean isTaskRunning() {
        return this.runningAdjustmentTask.isPresent() && !this.runningAdjustmentTask.get().isDone();
    }

    @Override
    public void onDisable() {
        // reuse task if available, otherwise restart
        this.adjustLuminanceTask.cacheLightmap();
        this.adjustLuminanceTask.setDesiredLightmapValue(Optional.empty());
        if (!this.isTaskRunning()) {
            this.launchAdjustmentTask();
        } else {
            this.adjustLuminanceTask.resetTimer();
        }
        super.onDisable();
    }
}
