package software.tachyon.starfruit.module.event;

public class KeyPressEvent {

    private final int keyPressed;

    public KeyPressEvent(int keyPressed) {
        this.keyPressed = keyPressed;
    }

    public int getKeyPressed() {
        return this.keyPressed;
    }
}
