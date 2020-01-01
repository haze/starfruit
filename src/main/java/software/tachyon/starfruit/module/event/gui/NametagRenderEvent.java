package software.tachyon.starfruit.module.event.gui;

import net.minecraft.entity.Entity;
import software.tachyon.starfruit.module.event.api.CancellableEvent;

public class NametagRenderEvent<E extends Entity> extends CancellableEvent {
    final E entity;

    public NametagRenderEvent(E ent) {
        this.entity = ent;
    }

    public E getEntity() {
        return this.entity;
    }
}
