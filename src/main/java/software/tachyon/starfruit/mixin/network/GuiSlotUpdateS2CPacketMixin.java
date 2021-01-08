package software.tachyon.starfruit.mixin.network;

import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import net.minecraft.item.ItemStack;

@Mixin(ScreenHandlerSlotUpdateS2CPacket.class)
public interface GuiSlotUpdateS2CPacketMixin {
   @Accessor
   public int getSyncId();

   @Accessor
   public int getSlot();

   @Accessor
   public ItemStack getStack();
}
