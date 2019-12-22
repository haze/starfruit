package software.tachyon.starfruit.mixin.world;

import net.minecraft.world.dimension.Dimension;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

// Thanks Jordin
@Mixin(Dimension.class)
public interface DimensionMixin {
    @Accessor
    float[] getLightLevelToBrightness();
}