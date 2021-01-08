package software.tachyon.starfruit.module.event.player;

import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import software.tachyon.starfruit.mixin.network.GuiSlotUpdateS2CPacketMixin;
import software.tachyon.starfruit.module.event.api.Event;

public class InventoryUpdateEvent extends Event {
    final ScreenHandlerSlotUpdateS2CPacket packet;
    final int slot, id;
    final ItemStack stack;

    public InventoryUpdateEvent(ScreenHandlerSlotUpdateS2CPacket input) {
        this.packet = input;
        this.id = ((GuiSlotUpdateS2CPacketMixin) input).getSyncId();
        this.slot = ((GuiSlotUpdateS2CPacketMixin) input).getSlot();
        this.stack = ((GuiSlotUpdateS2CPacketMixin) input).getStack();
    }

    public int getSlot() {
        return this.slot;
    }

    public int getId() {
        return this.id;
    }

    public ItemStack getItemStack() {
        return this.stack;
    }

}
