package software.tachyon.starfruit.module.event.gui;

import software.tachyon.starfruit.module.event.api.Event;

public class InGameHudDrawEvent extends Event {
    public static enum State {
        PRE, POST
    }

    final State state;

    public State getState() {
        return this.state;
    }

    public InGameHudDrawEvent(State state) {
        this.state = state;
    }
}
