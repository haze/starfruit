package software.tachyon.starfruit.utility;

import java.awt.*;
import java.util.Optional;

public class HexShift {
    public static final char CATALYST_CHAR = '\u0666';
    public static final char IRIDESCENCE_CHAR = '\u0420';

    public static final int SHIFT_DISTANCE = 7;
    public final float r, g, b;

    HexShift(float r, float g, float b) {
        this.r = r;
        this.g = g;
        this.b = b;
    }

    public static String colorize(String input, Color color) {
        return colorizeLiteral(input, Integer.toHexString(color.getRGB() & 0xFFFFFF));
    }

    public static String colorizeLiteral(String input, String color) {
        return input;
//        return CATALYST_CHAR + color + input + StarfruitMod.COLOR_SEPARATOR + "r";
    }

    // Transforms "\u0666XXXXXX" to a shifted color
    public static Optional<HexShift> parseHex(int offset, String text) {
        if (offset + 7 > text.length())
            return Optional.empty();
        final String hex = text.substring(offset + 1, offset + SHIFT_DISTANCE);
        try {
            final int color = Integer.parseInt(hex, 16);
            final float r = (float) (color >> 16 & 255) / 255.0F;
            final float g = (float) (color >> 8 & 255) / 255.0F;
            final float b = (float) (color & 255) / 255.0F;
            return Optional.of(new HexShift(r, g, b));
        } catch (NumberFormatException ignored) {
        }
        return Optional.empty();
    }
}
