package software.tachyon.starfruit.mixin.network;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import net.minecraft.network.packet.s2c.play.ContainerSlotUpdateS2CPacket;
import net.minecraft.item.ItemStack;

@Mixin(ContainerSlotUpdateS2CPacket.class)
public interface GuiSlotUpdateS2CPacketMixin {
   @Accessor
   public int getSyncId();

   @Accessor
   public int getSlot();

   @Accessor
   public ItemStack getStack();
}
