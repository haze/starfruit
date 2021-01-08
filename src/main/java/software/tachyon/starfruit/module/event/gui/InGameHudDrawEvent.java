package software.tachyon.starfruit.module.event.gui;

import net.minecraft.client.util.math.MatrixStack;
import software.tachyon.starfruit.module.event.api.Event;

public class InGameHudDrawEvent extends Event {
    public static enum State {
        PRE, POST
    }

    private MatrixStack matrices;

    public MatrixStack getMatrices() { return this.matrices; }
    private final double partialTicks;

    public double getPartialTicks() {
        return this.partialTicks;
    }

    final State state;

    public State getState() {
        return this.state;
    }

    public InGameHudDrawEvent(State state, MatrixStack matrices, double partialTicks) {
        this.state = state;
        this.matrices = matrices;
        this.partialTicks = partialTicks;
    }
}
