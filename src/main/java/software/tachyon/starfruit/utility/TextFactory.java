package software.tachyon.starfruit.utility;


import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import org.apache.commons.lang3.StringUtils;
import org.intellij.lang.annotations.MagicConstant;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;

@SuppressWarnings("all")
// @Generated
public class TextFactory {
    public static final MutableText EMPTY_TEXT = (MutableText) LiteralText.EMPTY;

    // style bit masks
    public static final int PLAIN = Font.PLAIN;       // 0b00000
    public static final int BOLD = Font.BOLD;         // 0b00001
    public static final int ITALIC = Font.ITALIC;     // 0b00010
    public static final int UNDERLINE = (1 << 2);     // 0b00100
    public static final int STRIKETHROUGH = (1 << 3); // 0b01000
    public static final int OBFUSCATED = (1 << 4);    // 0b10000

    public static final int ALL_STYLES = OBFUSCATED | STRIKETHROUGH | UNDERLINE | ITALIC | BOLD; // 0b11111

    // @Generated
    private static final Style[] STYLES = new Style[]{
            Style.EMPTY,
            Style.EMPTY.withBold(true),
            Style.EMPTY.withItalic(true),
            Style.EMPTY.withBold(true).withItalic(true),
            Style.EMPTY.withFormatting(Formatting.UNDERLINE),
            Style.EMPTY.withBold(true).withFormatting(Formatting.UNDERLINE),
            Style.EMPTY.withItalic(true).withFormatting(Formatting.UNDERLINE),
            Style.EMPTY.withBold(true).withItalic(true).withFormatting(Formatting.UNDERLINE),
            Style.EMPTY.withFormatting(Formatting.STRIKETHROUGH),
            Style.EMPTY.withBold(true).withFormatting(Formatting.STRIKETHROUGH),
            Style.EMPTY.withItalic(true).withFormatting(Formatting.STRIKETHROUGH),
            Style.EMPTY.withBold(true).withItalic(true).withFormatting(Formatting.STRIKETHROUGH),
            Style.EMPTY.withFormatting(Formatting.UNDERLINE).withFormatting(Formatting.STRIKETHROUGH),
            Style.EMPTY.withBold(true).withFormatting(Formatting.UNDERLINE).withFormatting(Formatting.STRIKETHROUGH),
            Style.EMPTY.withItalic(true).withFormatting(Formatting.UNDERLINE).withFormatting(Formatting.STRIKETHROUGH),
            Style.EMPTY.withBold(true).withItalic(true).withFormatting(Formatting.UNDERLINE).withFormatting(Formatting.STRIKETHROUGH),
            Style.EMPTY.withFormatting(Formatting.OBFUSCATED),
            Style.EMPTY.withBold(true).withFormatting(Formatting.OBFUSCATED),
            Style.EMPTY.withItalic(true).withFormatting(Formatting.OBFUSCATED),
            Style.EMPTY.withBold(true).withItalic(true).withFormatting(Formatting.OBFUSCATED),
            Style.EMPTY.withFormatting(Formatting.UNDERLINE).withFormatting(Formatting.OBFUSCATED),
            Style.EMPTY.withBold(true).withFormatting(Formatting.UNDERLINE).withFormatting(Formatting.OBFUSCATED),
            Style.EMPTY.withItalic(true).withFormatting(Formatting.UNDERLINE).withFormatting(Formatting.OBFUSCATED),
            Style.EMPTY.withBold(true).withItalic(true).withFormatting(Formatting.UNDERLINE).withFormatting(Formatting.OBFUSCATED),
            Style.EMPTY.withFormatting(Formatting.STRIKETHROUGH).withFormatting(Formatting.OBFUSCATED),
            Style.EMPTY.withBold(true).withFormatting(Formatting.STRIKETHROUGH).withFormatting(Formatting.OBFUSCATED),
            Style.EMPTY.withItalic(true).withFormatting(Formatting.STRIKETHROUGH).withFormatting(Formatting.OBFUSCATED),
            Style.EMPTY.withBold(true).withItalic(true).withFormatting(Formatting.STRIKETHROUGH).withFormatting(Formatting.OBFUSCATED),
            Style.EMPTY.withFormatting(Formatting.UNDERLINE).withFormatting(Formatting.STRIKETHROUGH).withFormatting(Formatting.OBFUSCATED),
            Style.EMPTY.withBold(true).withFormatting(Formatting.UNDERLINE).withFormatting(Formatting.STRIKETHROUGH).withFormatting(Formatting.OBFUSCATED),
            Style.EMPTY.withItalic(true).withFormatting(Formatting.UNDERLINE).withFormatting(Formatting.STRIKETHROUGH).withFormatting(Formatting.OBFUSCATED),
            Style.EMPTY.withBold(true).withItalic(true).withFormatting(Formatting.UNDERLINE).withFormatting(Formatting.STRIKETHROUGH).withFormatting(Formatting.OBFUSCATED)
    };

    public static Style getStyleFromMask(final @TextStyle int styleMask) {
        return STYLES[styleMask];
    }

    public static int getStyle(final @NotNull Style style) {
        return (style.isObfuscated() ? OBFUSCATED : PLAIN) |
                (style.isStrikethrough() ? STRIKETHROUGH : PLAIN) |
                (style.isUnderlined() ? UNDERLINE : PLAIN) |
                (style.isItalic() ? ITALIC : PLAIN) |
                (style.isBold() ? BOLD : PLAIN);
    }

    public static MutableText concat(final @NotNull MutableText... texts) {
        final MutableText message = new LiteralText(StringUtils.EMPTY);

        for (MutableText text : texts) {
            if (text != null) {
                message.append(text);
            }
        }

        return message;
    }

    public static MutableText concat(final @NotNull Iterable<@NotNull MutableText> texts) {
        final MutableText message = new LiteralText(StringUtils.EMPTY);

        for (MutableText text : texts) {
            if (text != null) {
                message.append(text);
            }
        }

        return message;
    }

    public static MutableText concat(final @NotNull String joinerStr, final @Nullable MutableText @NotNull ... texts) {
        final MutableText message = new LiteralText(StringUtils.EMPTY);
        final MutableText joiner = new LiteralText(joinerStr);

        for (final @Nullable MutableText text : texts) {
            if (text != null) {
                message.append(text);
                message.append(joiner);
            }
        }

        // remove last joiner string
        if (!message.getSiblings().isEmpty()) {
            message.getSiblings().remove(message.getSiblings().size() - 1);
        }

        return message;
    }

    public static MutableText concat(final @NotNull String joinerStr, final @NotNull Iterable<@NotNull MutableText> texts) {
        return concat(new LiteralText(joinerStr), texts);
    }

    public static MutableText concat(final @NotNull MutableText joiner, final @NotNull Iterable<@NotNull MutableText> texts) {
        final MutableText message = new LiteralText(StringUtils.EMPTY);

        for (final MutableText text : texts) {
            if (text != null) {
                message.append(text);
                message.append(joiner);
            }
        }

        // remove last joiner string
        if (!message.getSiblings().isEmpty()) {
            message.getSiblings().remove(message.getSiblings().size() - 1);
        }

        return message;
    }

    public static MutableText commafy(final @NotNull Iterable<@NotNull MutableText> texts, final @NotNull Formatting formatting) {
        final MutableText message = concat(text(", ", formatting), texts);
        message.append(text(".", formatting));
        return message;
    }

    public static MutableText join(final @NotNull MutableText... texts) {
        return concat(" ", texts);
    }

    public static MutableText join(final @NotNull Iterable<@NotNull MutableText> texts) {
        return concat(" ", texts);
    }

    public static MutableText format(final @NotNull MutableText text, final @NotNull Formatting format) {
        return text.formatted(format);
    }

    public static MutableText text(final @NotNull String msg) {
        return new LiteralText(msg);
    }

    public static MutableText text(final @NotNull String msg, final @TextStyle int styleMask) {
        return new LiteralText(msg).setStyle(STYLES[styleMask]);
    }

    public static MutableText text(final @NotNull String msg, final @TextStyle int styleMask, final int colour) {
        return new LiteralText(msg).setStyle(STYLES[styleMask].withColor(TextColor.fromRgb(colour)));
    }

    public static MutableText text(final @NotNull MutableText msg, final @NotNull Formatting format) {
        return msg.formatted(format);
    }

    public static MutableText text(final @NotNull String msg, final @NotNull Formatting format) {
        return text(msg, PLAIN).formatted(format);
    }

    public static MutableText text(final @NotNull String msg, final @TextStyle int styleMask, final @NotNull Formatting format) {
        return text(msg, styleMask).formatted(format);
    }

    public static MutableText setTextColour(final @NotNull MutableText text, final int colour) {
        text.setStyle(text.getStyle().withColor(TextColor.fromRgb(colour)));
        return text;
    }

    public static TranslatableText translatableText(final @NotNull String key) {
        return new TranslatableText(key);
    }

    public static MutableText click(final @NotNull MutableText base, final @NotNull ClickEvent clickEvent) {
        return base.setStyle(base.getStyle().withClickEvent(clickEvent));
    }

    public static MutableText click(final @NotNull MutableText base, final @NotNull ClickEvent.Action action, final @NotNull String value) {
        return click(base, new ClickEvent(action, value));
    }

    public static MutableText hover(final @NotNull MutableText base, final @NotNull HoverEvent hoverEvent) {
        return base.setStyle(base.getStyle().withHoverEvent(hoverEvent));
    }

    public static <T> MutableText hover(final @NotNull MutableText base, final @NotNull HoverEvent.Action<T> action, final @NotNull T object) {
        return hover(base, new HoverEvent(action, object));
    }

    // @Generated

    public static MutableText black(final @NotNull MutableText msg) {
        return msg.formatted(Formatting.BLACK);
    }

    public static MutableText black(final @NotNull String msg) {
        return black(msg, PLAIN);
    }

    public static MutableText black(final @NotNull String msg, final @TextStyle int styleMask) {
        return black(text(msg, styleMask));
    }

    public static MutableText dark_blue(final @NotNull MutableText msg) {
        return msg.formatted(Formatting.DARK_BLUE);
    }

    public static MutableText dark_blue(final @NotNull String msg) {
        return dark_blue(msg, PLAIN);
    }

    public static MutableText dark_blue(final @NotNull String msg, final @TextStyle int styleMask) {
        return dark_blue(text(msg, styleMask));
    }

    public static MutableText dark_green(final @NotNull MutableText msg) {
        return msg.formatted(Formatting.DARK_GREEN);
    }

    public static MutableText dark_green(final @NotNull String msg) {
        return dark_green(msg, PLAIN);
    }

    public static MutableText dark_green(final @NotNull String msg, final @TextStyle int styleMask) {
        return dark_green(text(msg, styleMask));
    }

    public static MutableText dark_aqua(final @NotNull MutableText msg) {
        return msg.formatted(Formatting.DARK_AQUA);
    }

    public static MutableText dark_aqua(final @NotNull String msg) {
        return dark_aqua(msg, PLAIN);
    }

    public static MutableText dark_aqua(final @NotNull String msg, final @TextStyle int styleMask) {
        return dark_aqua(text(msg, styleMask));
    }

    public static MutableText dark_red(final @NotNull MutableText msg) {
        return msg.formatted(Formatting.DARK_RED);
    }

    public static MutableText dark_red(final @NotNull String msg) {
        return dark_red(msg, PLAIN);
    }

    public static MutableText dark_red(final @NotNull String msg, final @TextStyle int styleMask) {
        return dark_red(text(msg, styleMask));
    }

    public static MutableText dark_purple(final @NotNull MutableText msg) {
        return msg.formatted(Formatting.DARK_PURPLE);
    }

    public static MutableText dark_purple(final @NotNull String msg) {
        return dark_purple(msg, PLAIN);
    }

    public static MutableText dark_purple(final @NotNull String msg, final @TextStyle int styleMask) {
        return dark_purple(text(msg, styleMask));
    }

    public static MutableText gold(final @NotNull MutableText msg) {
        return msg.formatted(Formatting.GOLD);
    }

    public static MutableText gold(final @NotNull String msg) {
        return gold(msg, PLAIN);
    }

    public static MutableText gold(final @NotNull String msg, final @TextStyle int styleMask) {
        return gold(text(msg, styleMask));
    }

    public static MutableText gray(final @NotNull MutableText msg) {
        return msg.formatted(Formatting.GRAY);
    }

    public static MutableText gray(final @NotNull String msg) {
        return gray(msg, PLAIN);
    }

    public static MutableText gray(final @NotNull String msg, final @TextStyle int styleMask) {
        return gray(text(msg, styleMask));
    }

    public static MutableText dark_gray(final @NotNull MutableText msg) {
        return msg.formatted(Formatting.DARK_GRAY);
    }

    public static MutableText dark_gray(final @NotNull String msg) {
        return dark_gray(msg, PLAIN);
    }

    public static MutableText dark_gray(final @NotNull String msg, final @TextStyle int styleMask) {
        return dark_gray(text(msg, styleMask));
    }

    public static MutableText blue(final @NotNull MutableText msg) {
        return msg.formatted(Formatting.BLUE);
    }

    public static MutableText blue(final @NotNull String msg) {
        return blue(msg, PLAIN);
    }

    public static MutableText blue(final @NotNull String msg, final @TextStyle int styleMask) {
        return blue(text(msg, styleMask));
    }

    public static MutableText green(final @NotNull MutableText msg) {
        return msg.formatted(Formatting.GREEN);
    }

    public static MutableText green(final @NotNull String msg) {
        return green(msg, PLAIN);
    }

    public static MutableText green(final @NotNull String msg, final @TextStyle int styleMask) {
        return green(text(msg, styleMask));
    }

    public static MutableText aqua(final @NotNull MutableText msg) {
        return msg.formatted(Formatting.AQUA);
    }

    public static MutableText aqua(final @NotNull String msg) {
        return aqua(msg, PLAIN);
    }

    public static MutableText aqua(final @NotNull String msg, final @TextStyle int styleMask) {
        return aqua(text(msg, styleMask));
    }

    public static MutableText red(final @NotNull MutableText msg) {
        return msg.formatted(Formatting.RED);
    }

    public static MutableText red(final @NotNull String msg) {
        return red(msg, PLAIN);
    }

    public static MutableText red(final @NotNull String msg, final @TextStyle int styleMask) {
        return red(text(msg, styleMask));
    }

    public static MutableText light_purple(final @NotNull MutableText msg) {
        return msg.formatted(Formatting.LIGHT_PURPLE);
    }

    public static MutableText light_purple(final @NotNull String msg) {
        return light_purple(msg, PLAIN);
    }

    public static MutableText light_purple(final @NotNull String msg, final @TextStyle int styleMask) {
        return light_purple(text(msg, styleMask));
    }

    public static MutableText yellow(final @NotNull MutableText msg) {
        return msg.formatted(Formatting.YELLOW);
    }

    public static MutableText yellow(final @NotNull String msg) {
        return yellow(msg, PLAIN);
    }

    public static MutableText yellow(final @NotNull String msg, final @TextStyle int styleMask) {
        return yellow(text(msg, styleMask));
    }

    public static MutableText white(final @NotNull MutableText msg) {
        return msg.formatted(Formatting.WHITE);
    }

    public static MutableText white(final @NotNull String msg) {
        return white(msg, PLAIN);
    }

    public static MutableText white(final @NotNull String msg, final @TextStyle int styleMask) {
        return white(text(msg, styleMask));
    }

    public static MutableText obfuscated(final @NotNull MutableText msg) {
        return msg.formatted(Formatting.OBFUSCATED);
    }

    public static MutableText obfuscated(final @NotNull String msg) {
        return obfuscated(msg, PLAIN);
    }

    public static MutableText obfuscated(final @NotNull String msg, final @TextStyle int styleMask) {
        return obfuscated(text(msg, styleMask));
    }

    public static MutableText bold(final @NotNull MutableText msg) {
        return msg.formatted(Formatting.BOLD);
    }

    public static MutableText bold(final @NotNull String msg) {
        return bold(msg, PLAIN);
    }

    public static MutableText bold(final @NotNull String msg, final @TextStyle int styleMask) {
        return bold(text(msg, styleMask));
    }

    public static MutableText strikethrough(final @NotNull MutableText msg) {
        return msg.formatted(Formatting.STRIKETHROUGH);
    }

    public static MutableText strikethrough(final @NotNull String msg) {
        return strikethrough(msg, PLAIN);
    }

    public static MutableText strikethrough(final @NotNull String msg, final @TextStyle int styleMask) {
        return strikethrough(text(msg, styleMask));
    }

    public static MutableText underline(final @NotNull MutableText msg) {
        return msg.formatted(Formatting.UNDERLINE);
    }

    public static MutableText underline(final @NotNull String msg) {
        return underline(msg, PLAIN);
    }

    public static MutableText underline(final @NotNull String msg, final @TextStyle int styleMask) {
        return underline(text(msg, styleMask));
    }

    public static MutableText italic(final @NotNull MutableText msg) {
        return msg.formatted(Formatting.ITALIC);
    }

    public static MutableText italic(final @NotNull String msg) {
        return italic(msg, PLAIN);
    }

    public static MutableText italic(final @NotNull String msg, final @TextStyle int styleMask) {
        return italic(text(msg, styleMask));
    }

    public static MutableText reset(final @NotNull MutableText msg) {
        return msg.formatted(Formatting.RESET);
    }

    public static MutableText reset(final @NotNull String msg) {
        return reset(msg, PLAIN);
    }

    public static MutableText reset(final @NotNull String msg, final @TextStyle int styleMask) {
        return reset(text(msg, styleMask));
    }

    @MagicConstant(flags = {PLAIN, BOLD, ITALIC, UNDERLINE, STRIKETHROUGH, OBFUSCATED})
    public @interface TextStyle {
    }
}
