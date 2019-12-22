package software.tachyon.starfruit.module.render;

import software.tachyon.starfruit.StarfruitMod;
import software.tachyon.starfruit.mixin.MinecraftClientMixin;
import software.tachyon.starfruit.module.ModuleInfo;
import software.tachyon.starfruit.module.StatefulModule;
import software.tachyon.starfruit.module.ModuleInfo.Category;

import java.util.Optional;
import java.util.concurrent.Future;

import net.engio.mbassy.listener.Listener;
import net.engio.mbassy.listener.References;

@Listener(references = References.Strong)
public class Luminance extends StatefulModule {

    class AdjustLuminanceTask implements Runnable {
        private final int desiredGamma;
        private final MinecraftClientMixin client;

        AdjustLuminanceTask(int gamma, MinecraftClientMixin client) {
            this.desiredGamma = gamma;
            this.client = client;
        }

        boolean isIncreasing() {
            return this.desiredGamma > this.client.getOptions().gamma;
        }

        boolean isFinished() {
            final double curGamma = this.client.getOptions().gamma;
            final boolean finishedSlidingLight = this.isIncreasing() ? curGamma >= this.desiredGamma
                    : curGamma < this.desiredGamma;
            return Thread.interrupted() || finishedSlidingLight;
        }

        public void run() {
            double curGamma = this.client.getOptions().gamma;
            final int diff = this.desiredGamma > curGamma ? 1 : -1;
            long curMS = System.currentTimeMillis();
            while (!this.isFinished()) {
                final long timeDiff = System.currentTimeMillis() - curMS;
                // System.out.println(timeDiff);
                if (timeDiff >= 10) {
                    // System.out.printf("des=%f, cur=%f\n", this.desiredGamma, curGamma);
                    this.client.getOptions().gamma += (diff * 0.1);
                    curGamma = this.client.getOptions().gamma;
                    curMS = System.currentTimeMillis();
                }
            }
        }

        // TODO(haze) figure out new lighting, dont use gamma
        void updateBrightnessTable(double newValue) {
        }
    }

    AdjustLuminanceTask adjustLuminanceTask = null;
    Optional<Future<Void>> runningAdjustmentTask = Optional.empty();
    double savedGamma = 0;

    public Luminance(int keyCode) {
        super(keyCode);
        this.info = new ModuleInfo.Builder().name("Luminance").color(StarfruitMod.Colors.moduleColor(0.18F))
                .category(Category.MOVEMENT).build();
        this.adjustLuminanceTask = new AdjustLuminanceTask(10000, StarfruitMod.minecraft);
    }

    @Override
    @SuppressWarnings("unchecked") // TODO(haze): is this really needed?
    public void onEnable() {
        this.savedGamma = StarfruitMod.minecraft.getOptions().gamma;
        this.runningAdjustmentTask = Optional
                .of((Future<Void>) StarfruitMod.getModuleManager().getThreadPool().submit(this.adjustLuminanceTask));
        super.onEnable();
    }

    @Override
    public void onDisable() {
        this.runningAdjustmentTask.ifPresent(task -> task.cancel(true)); // cancel running task if there is any
        super.onDisable();
        StarfruitMod.minecraft.getOptions().gamma = this.savedGamma;
        this.savedGamma = 0;
    }
}
