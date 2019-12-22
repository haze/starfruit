package software.tachyon.starfruit.module;

import net.engio.mbassy.bus.MBassador;
import net.engio.mbassy.listener.Handler;
import net.engio.mbassy.listener.Listener;
import net.engio.mbassy.listener.References;

import software.tachyon.starfruit.module.event.Event;
import software.tachyon.starfruit.module.event.KeyPressEvent;
import software.tachyon.starfruit.module.movement.Sprint;
import software.tachyon.starfruit.module.render.Luminance;
import static org.lwjgl.glfw.GLFW.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Listener(references = References.Strong)
public class ModuleManager {

    private MBassador<Event> bus = null;
    private static ModuleManager INSTANCE = null;

    private Map<Integer, StatefulModule> modules = null;
    private SortedSet<StatefulModule> display = null;

    private ExecutorService threadPool = null;

    public ModuleManager() {
        this.bus = new MBassador<>();

        this.modules = new HashMap<>();
        this.display = new TreeSet<>(
                (StatefulModule m1, StatefulModule m2) -> m1.getInfo().name.compareTo(m2.getInfo().name));
        this.threadPool = Executors.newCachedThreadPool();

        this.bus.subscribe(this);
        this.register(new Sprint(GLFW_KEY_C));
        this.register(new Luminance(GLFW_KEY_B));
    }

    public void register(StatefulModule mod) {
        this.modules.put(mod.getKeyCode(), mod);
    }

    @Handler
    public void onKeyPress(KeyPressEvent event) {
        // System.out.println("I got a key press " + event.getKeyPressed());
        Optional.ofNullable(this.modules.get(event.getKeyPressed())).ifPresent((module) -> {
            final boolean newState = !module.getState();
            module.setState(newState);

            if (newState) {
                module.onEnable();
                this.display.add(module);
            } else {
                this.display.remove(module);
                module.onDisable();
            }
        });
    }

    public MBassador<Event> getBus() {
        return this.bus;
    }

    public SortedSet<StatefulModule> getDisplay() {
        return this.display;
    }

    public ExecutorService getThreadPool() {
        return this.threadPool;
    }

    public static ModuleManager getInstance() {
        if (INSTANCE == null)
            INSTANCE = new ModuleManager();

        return INSTANCE;
    }
}
