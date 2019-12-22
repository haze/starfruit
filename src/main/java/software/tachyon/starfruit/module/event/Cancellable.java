package software.tachyon.starfruit.module.event;

public interface Cancellable {
    void setCancelled(boolean cancelled);
    boolean isCancelled();
}