package software.tachyon.starfruit.mixin.world;

import net.minecraft.world.dimension.DimensionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

// Thanks Jordin
@Mixin(DimensionType.class)
public interface DimensionMixin {
    @Accessor
    float[] getField_24767();
}