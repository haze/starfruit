package software.tachyon.starfruit.module.network;

import net.engio.mbassy.listener.Handler;
import net.minecraft.network.Packet;
import net.minecraft.network.listener.PacketListener;
import software.tachyon.starfruit.StarfruitMod;
import software.tachyon.starfruit.module.ModuleInfo;
import software.tachyon.starfruit.module.StatefulModule;
import software.tachyon.starfruit.module.event.RecvPacketEvent;
import software.tachyon.starfruit.module.event.SendPacketEvent;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.Queue;
import java.util.StringJoiner;

import static software.tachyon.starfruit.utility.StarfruitTextFactory.file;
import static software.tachyon.starfruit.utility.StarfruitTextFactory.throwable;
import static software.tachyon.starfruit.utility.TextFactory.*;

public class PacketLogger extends StatefulModule {
    class LoggedPacket {
        final String packetName;
        final long logTime;

        class LoggedField {
            final String name, type;
            String value = null;

            LoggedField(Packet<?> packetInst, Field field) {
                this.name = field.getName();
                this.type = field.getType().getSimpleName();
                try {
                    this.value = field.get(packetInst).toString();
                } catch (IllegalAccessException ignored) {
                    ignored.printStackTrace();
                    // impossible
                }
            }
        }

        final Queue<LoggedField> fields;

        LoggedPacket(Packet<?> packet) {
            this.logTime = System.currentTimeMillis();
            final Class<?> packetClass = packet.getClass();
            this.packetName = packetClass.getSimpleName();
            this.fields = new LinkedList<>();
            for (final Field field : packetClass.getDeclaredFields()) {
                final boolean beforeAccessibleState = field.canAccess(packet);
                if (!beforeAccessibleState)
                    field.setAccessible(true);

                this.fields.add(new LoggedField(packet, field));

                field.setAccessible(beforeAccessibleState);
            }
        }

        @Override
        public String toString() {
            final StringBuilder builder =
                    new StringBuilder(this.logTime + " " + this.packetName + " ");
            for (final LoggedField field : this.fields) {
                builder.append('(');
                builder.append(field.type);
                builder.append(") ");
                builder.append(field.name);
                builder.append("=");
                builder.append(field.value);
                builder.append(' ');
            }
            return builder.toString().trim();
        }
    }

    final Queue<LoggedPacket> log;
    final SimpleDateFormat fmt;

    public PacketLogger(Integer defaultKeyCode) {
        super(defaultKeyCode, ModuleInfo.init().name("PacketLogger").build());
        this.log = new LinkedList<>();
        this.fmt = new SimpleDateFormat("yyyy-MM-dd__HH_mm_ss_SSS");
    }

    @Handler
    <T extends PacketListener> void onPacketSend(SendPacketEvent<T> event) {
        this.log.add(new LoggedPacket(event.getPacket()));
    }

    @Handler
    <T extends PacketListener> void onPacketRecv(RecvPacketEvent<T> event) {
        this.log.add(new LoggedPacket(event.getPacket()));
    }

    // flush() writes the content of 'log' to a file within the starfruit directory
    // with the time as the filename
    // NOTE: This clears the log!
    File flush() throws IOException {
        final File file = new File(StarfruitMod.FOLDER, this.fmt.format(new Date()) + ".log");
        final StringJoiner builder = new StringJoiner("\n");
        for (final LoggedPacket packet : this.log) {
            builder.add(packet.toString());
        }
        Files.writeString(file.toPath(), builder.toString());
        this.log.clear();
        return file;
    }

    @Override
    public void onDisable() {
        try {
            final int packetCount = this.log.size();
            final File savedFile = this.flush();
            StarfruitMod.info(join(
                    text("Saved " + packetCount + " packets to"),
                    file(savedFile)
            ));
        } catch (IOException e) {
            e.printStackTrace();
            StarfruitMod.info(join(red("Failed to save packet log:"), throwable(e)));
        }
        super.onDisable();
    }
}
