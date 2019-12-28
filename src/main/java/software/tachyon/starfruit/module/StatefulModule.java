package software.tachyon.starfruit.module;

import software.tachyon.starfruit.StarfruitMod;

public abstract class StatefulModule extends Module {

    private Integer catalystKeyCode = null;
    private boolean state;

    protected ModuleInfo info;

    public StatefulModule(int defaultKeyCode) {
        this.catalystKeyCode = defaultKeyCode;
    }

    protected void onEnable() {
        StarfruitMod.getModuleManager().getBus().subscribe(this);
    }

    protected void onDisable() {
        StarfruitMod.getModuleManager().getBus().unsubscribe(this);
    }

    void setKeyCode(int newKeyCode) {
        this.catalystKeyCode = newKeyCode;
    }

    Integer getKeyCode() {
        return this.catalystKeyCode;
    }

    public void setState(boolean newState) {
        this.state = newState;
    }

    public boolean getState() {
        return this.state;
    }

    public ModuleInfo getInfo() {
        return this.info;
    }
}
