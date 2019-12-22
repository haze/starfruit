package software.tachyon.starfruit.module.event;

public class SprintChangeEvent implements Cancellable {

    private final boolean newState;
    private boolean cancelled = false;

    public SprintChangeEvent(boolean newState) {
        this.newState = newState;
    }

    public boolean getNewState() {
        return this.newState;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }
}
