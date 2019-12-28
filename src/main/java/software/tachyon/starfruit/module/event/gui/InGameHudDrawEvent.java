package software.tachyon.starfruit.module.event.gui;

import software.tachyon.starfruit.module.event.api.Event;

public class InGameHudDrawEvent extends Event {
    public static enum State {
        PRE, POST
    }

    private final double partialTicks;

    public double getPartialTicks() {
        return this.partialTicks;
    }

    final State state;

    public State getState() {
        return this.state;
    }

    public InGameHudDrawEvent(State state, double partialTicks) {
        this.state = state;
        this.partialTicks = partialTicks;
    }
}
