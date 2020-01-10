package software.tachyon.starfruit.module;

import software.tachyon.starfruit.StarfruitMod;

public abstract class StatefulModule extends Module {

    private Integer catalystKeyCode = null;
    private boolean state;

    protected ModuleInfo info;

    public StatefulModule(Integer defaultKeyCode, ModuleInfo info) {
        this.catalystKeyCode = defaultKeyCode;
        this.info = info;
    }

    protected void onEnable() {
        StarfruitMod.getModuleManager().getBus().subscribe(this);
    }

    protected void onDisable() {
        StarfruitMod.getModuleManager().getBus().unsubscribe(this);
    }

    void setKeyCode(Integer newKeyCode) {
        this.catalystKeyCode = newKeyCode;
    }

    Integer getKeyCode() {
        return this.catalystKeyCode;
    }

    String getKeyCodeString() {
        return this.getKeyCode() == null ? "null" : this.getKeyCode().toString();
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

    public void disable() {
        StarfruitMod.getModuleManager().setModuleState(this, false, true);
    }
}
