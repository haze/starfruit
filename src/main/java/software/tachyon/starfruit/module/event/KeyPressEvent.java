package software.tachyon.starfruit.module.event;

import software.tachyon.starfruit.module.event.api.CancellableEvent;

public class KeyPressEvent extends CancellableEvent {

    private final int keyPressed;

    public KeyPressEvent(int keyPressed) {
        this.keyPressed = keyPressed;
    }

    public int getKeyPressed() {
        return this.keyPressed;
    }
}
