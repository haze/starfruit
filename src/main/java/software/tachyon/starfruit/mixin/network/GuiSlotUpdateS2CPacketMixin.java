package software.tachyon.starfruit.mixin.network;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import net.minecraft.client.network.packet.GuiSlotUpdateS2CPacket;
import net.minecraft.item.ItemStack;

@Mixin(GuiSlotUpdateS2CPacket.class)
public interface GuiSlotUpdateS2CPacketMixin {
   @Accessor
   public int getId();

   @Accessor
   public int getSlot();

   @Accessor
   public ItemStack getStack();
}
