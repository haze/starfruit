package software.tachyon.starfruit.mixin.network;

import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ScreenHandlerSlotUpdateS2CPacket.class)
public interface GuiSlotUpdateS2CPacketMixin {
   @Accessor
   int getSyncId();

   @Accessor
   int getSlot();

   @Accessor
   ItemStack getStack();
}
