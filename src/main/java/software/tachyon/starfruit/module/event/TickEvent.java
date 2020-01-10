package software.tachyon.starfruit.module.event;

import software.tachyon.starfruit.module.event.api.Event;

public class TickEvent extends Event {
    public static enum State {
        PRE, POST
    }

    final State state;

    public State getState() {
        return this.state;
    }

    public TickEvent(State state) {
        this.state = state;
    }
}
