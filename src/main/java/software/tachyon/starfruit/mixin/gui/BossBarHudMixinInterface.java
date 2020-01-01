package software.tachyon.starfruit.mixin.gui;

import java.util.Map;
import java.util.UUID;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.gui.hud.BossBarHud;
import net.minecraft.client.gui.hud.ClientBossBar;

@Mixin(BossBarHud.class)
public interface BossBarHudMixinInterface {
    @Accessor
    public Map<UUID, ClientBossBar> getBossBars();
}
