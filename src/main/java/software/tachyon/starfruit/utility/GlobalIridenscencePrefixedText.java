package software.tachyon.starfruit.utility;

import net.minecraft.client.font.TextVisitFactory;
import net.minecraft.text.*;
import org.apache.commons.lang3.mutable.MutableInt;
import software.tachyon.starfruit.StarfruitMod;

import java.util.List;
import java.util.Optional;

public final class GlobalIridenscencePrefixedText implements Text {
    private final String prefixStr;
    private final Text underlying;
    private final OrderedText prefix;

    public GlobalIridenscencePrefixedText(final String prefixStr, final Text underlying) {
        this.prefixStr = prefixStr;
        this.underlying = underlying;
        this.prefix = (visitor -> TextVisitFactory.visitFormatted(this.prefixStr, Style.EMPTY.withColor(TextColor.fromRgb(StarfruitMod.getGlobalIridescence())), visitor));
    }

    @Override
    public Style getStyle() {
        return this.underlying.getStyle();
    }

    @Override
    public String asString() {
        return this.underlying.asString();
    }

    @Override
    public List<Text> getSiblings() {
        return this.underlying.getSiblings();
    }

    @Override
    public MutableText copy() {
        return this.underlying.copy();
    }

    @Override
    public MutableText shallowCopy() {
        return this.underlying.shallowCopy();
    }

    private static OrderedText mergeConcat(final OrderedText start, final int startLength, final OrderedText end) {
        return visitor -> {
            final MutableInt absoluteIndex = new MutableInt();

            return start.accept(visitor) && end.accept((index, style, codePoint) -> {
                // System.out.println(absoluteIndex.getValue() + " " + index + " " + Arrays.toString(Character.toChars(codePoint)));

                if (absoluteIndex.getAndIncrement() < startLength) {
                    return true;
                }

                return visitor.accept(index, style, codePoint);
            });
        };
    }

    public void mutate(final List<OrderedText> input) {
        // would this ever occur?
        if (input.isEmpty() || input.get(0) == OrderedText.EMPTY) {
            input.clear();
            input.add(this.prefix);
        } else {
            final OrderedText original = input.remove(0);
            input.add(0, mergeConcat(this.prefix, this.prefixStr.length(), original));
        }
    }

    // FIXME: are these required?
    @Override
    public OrderedText asOrderedText() {
        return OrderedText.concat(this.prefix, this.underlying.asOrderedText());
    }

    @Override
    public <T> Optional<T> visit(final StringVisitable.Visitor<T> visitor) {
        return visitor.accept(this.prefixStr).or(() -> this.underlying.visit(visitor));
    }

    @Override
    public <T> Optional<T> visit(final StringVisitable.StyledVisitor<T> styledVisitor, final Style style) {
        return styledVisitor.accept(Style.EMPTY, this.prefixStr).or(() -> this.underlying.visit(styledVisitor, style));
    }
}