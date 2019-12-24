package software.tachyon.starfruit.module.event;

import software.tachyon.starfruit.module.event.api.CancellableEvent;

public class SprintChangeEvent extends CancellableEvent {

    private final boolean newState;

    public SprintChangeEvent(boolean newState) {
        this.newState = newState;
    }

    public boolean getNewState() {
        return this.newState;
    }
}
