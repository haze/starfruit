package software.tachyon.starfruit.module;

public abstract class StatefulModule extends Module {

    private Integer catalystKeyCode = null;
    private boolean state;

    protected ModuleInfo info;
    
    public StatefulModule(int keyCode) {
        this.catalystKeyCode = keyCode;
    }

    protected void onEnable() {
        ModuleManager.getModuleManager().getBus().subscribe(this);
    }

    protected void onDisable() {
        ModuleManager.getModuleManager().getBus().unsubscribe(this);
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