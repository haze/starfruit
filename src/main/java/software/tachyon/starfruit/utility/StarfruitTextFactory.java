package software.tachyon.starfruit.utility;

import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static software.tachyon.starfruit.utility.TextFactory.*;

public class StarfruitTextFactory {
    private static final int MAX_THROWABLE_STACK_TRACE_DEPTH = 4;

    public static @NotNull MutableText throwable(final @NotNull Throwable throwable) {
        final @NotNull String name = throwable.getClass().getSimpleName();
        final @Nullable String localisedMessage = throwable.getLocalizedMessage();

        final @NotNull StackTraceElement[] stackTraceElements = throwable.getStackTrace();
        final @NotNull List<MutableText> stackTrace = new ArrayList<>(Math.min(stackTraceElements.length, MAX_THROWABLE_STACK_TRACE_DEPTH));

        stackTrace.add(white(throwable.toString()));

        for (final @NotNull StackTraceElement element : stackTraceElements) {
            // this isn't a >= since stackTrace already has 1 element in it (the throwable.toString())
            if (stackTrace.size() > MAX_THROWABLE_STACK_TRACE_DEPTH) {
                break;
            }

            final String className = element.getClassName();
            final String lineNumber = element.getLineNumber() >= 0 ? "#" + element.getLineNumber() : "(Native)";

            stackTrace.add(gray(
                    className.substring(className.lastIndexOf('.') + 1) + "." + element.getMethodName() + " " + lineNumber
            ));
        }

        return hover(
                white(localisedMessage == null ? name : name + ": " + localisedMessage),
                HoverEvent.Action.SHOW_TEXT,
                concat("\n", stackTrace)
        );
    }

    public static @NotNull MutableText file(final @NotNull File path) {
        return click(hover(
                white(path.getName()),
                HoverEvent.Action.SHOW_TEXT,
                concat("\n",
                        white(path.getAbsolutePath()),
                        path.exists() ? gray("(Click to open)", ITALIC) : null
                )
        ), ClickEvent.Action.OPEN_FILE, path.getAbsolutePath());
    }
}
