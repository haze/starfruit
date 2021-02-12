package software.tachyon.starfruit.mixin.network;

import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.InventoryS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(InventoryS2CPacket.class)
public interface InventoryS2CPacketMixin {
    @Accessor
    public int getSyncId();

    @Accessor
    public List<ItemStack> getContents();
}
