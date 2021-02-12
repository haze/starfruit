package software.tachyon.starfruit.mixin.gui;

import net.minecraft.client.gui.hud.BossBarHud;
import net.minecraft.client.gui.hud.ClientBossBar;
import net.minecraft.network.packet.s2c.play.BossBarS2CPacket;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import software.tachyon.starfruit.utility.DrawUtility;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Mixin(BossBarHud.class)
public abstract class BossBarHudMixin {

    @Final
    @Shadow
    private Map<UUID, ClientBossBar> bossBars;

    @Inject(method = "handlePacket", at = @At("RETURN"))
    public void handlePacket(BossBarS2CPacket packet, CallbackInfo ci) {
        final List<Map.Entry<UUID, ClientBossBar>> list = new ArrayList<>(this.bossBars.entrySet());
        list.sort((a, b) -> Integer.compare(DrawUtility.asString(b.getValue().getName().asOrderedText()).length(),
                DrawUtility.asString(a.getValue().getName().asOrderedText()).length()));
        this.bossBars.clear();
        for (final Map.Entry<UUID, ClientBossBar> entry : list) {
            this.bossBars.put(entry.getKey(), entry.getValue());
        }
    }
}
