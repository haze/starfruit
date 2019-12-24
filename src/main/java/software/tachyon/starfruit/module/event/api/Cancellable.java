package software.tachyon.starfruit.module.event.api;

public interface Cancellable {
    void setCancelled(boolean cancelled);

    boolean isCancelled();
}
