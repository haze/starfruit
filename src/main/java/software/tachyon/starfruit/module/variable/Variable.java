package software.tachyon.starfruit.module.variable;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.regex.Pattern;

import com.google.common.collect.Streams;

import software.tachyon.starfruit.StarfruitMod;

public abstract class Variable<T> {
    private final T initial;
    private T value;
    private Optional<String> givenFieldName;

    public static class Int extends Variable<Integer> {
        public String getDisplay() {
            return String.format("%d", this.get());
        }

        public Int(Integer initial) {
            super(initial);
        }
    }

    public static class Dbl extends Variable<Double> {
        public String getDisplay() {
            return String.format("%.2f", this.get());
        }

        public Dbl(Double initial) {
            super(initial);
        }
    }

    public static class Bool extends Variable<Boolean> {
        public String getDisplay() {
            return this.get() ? StarfruitMod.Colors.colorize("on", 'a') : StarfruitMod.Colors.colorize("off", 'c');
        }

        static final String[] yesPatterns = new String[] { "y", "yes", "on", };
        static final String[] noPatterns = new String[] { "n", "no", "off", "nil" };
        public static final String patternStr = Streams.concat(Arrays.stream(yesPatterns), Arrays.stream(noPatterns))
                .collect(Collectors.joining("|"));
        public static final Pattern yesNo = Pattern.compile(patternStr);

        public static Optional<Boolean> parse(String input) {
            System.out.println("input = ");
            System.out.println(input);
            for (String yes : yesPatterns)
                if (input.equalsIgnoreCase(yes))
                    return Optional.of(true);
            for (String no : noPatterns)
                if (input.equalsIgnoreCase(no))
                    return Optional.of(false);
            return Optional.empty();
        }

        public Bool(Boolean initial) {
            super(initial);
        }

    }

    public static class Str extends Variable<String> {
        public String getDisplay() {
            return "\"" + this.get() + "\"";
        }

        public Str(String initial) {
            super(initial);
        }
    }

    public Variable(T initial) {
        this.givenFieldName = Optional.empty();
        this.initial = initial;
        this.value = initial;
    }

    public abstract String getDisplay();

    public void set(T newValue) {
        this.value = newValue;
    }

    public void reset() {
        this.set(this.initial);
    }

    public T get() {
        return this.value;
    }

    // Type hacky stuff
    public void setFieldName(String name) {
        this.givenFieldName = Optional.of(name);
    }

    public Optional<String> getName() {
        return this.givenFieldName;
    }

    public Optional<Kind> getKind() {
        return Kind.forVariable(this);
    }

    public static enum Kind {
        Integer, Double, Boolean, String;

        public static Optional<Kind> forVariable(Variable<?> variable) {
            if (variable instanceof Variable.Int) {
                return Optional.of(Kind.Integer);
            } else if (variable instanceof Variable.Dbl) {
                return Optional.of(Kind.Double);
            } else if (variable instanceof Variable.Bool) {
                return Optional.of(Kind.Boolean);
            } else if (variable instanceof Variable.Str) {
                return Optional.of(Kind.String);
            } else {
                return Optional.empty();
            }
        }
    }

}

