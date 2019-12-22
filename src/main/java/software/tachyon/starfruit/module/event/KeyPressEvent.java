package software.tachyon.starfruit.module.event;

public class KeyPressEvent extends Event {

    private final int keyPressed;

    public KeyPressEvent(int keyPressed) {
        this.keyPressed = keyPressed;
    }

    public int getKeyPressed() {
        return this.keyPressed;
    }
}
