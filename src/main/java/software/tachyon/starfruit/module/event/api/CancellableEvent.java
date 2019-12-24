package software.tachyon.starfruit.module.event.api;

public class CancellableEvent extends Event implements Cancellable {
    
    protected boolean cancelled = false;

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }
}
