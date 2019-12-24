package software.tachyon.starfruit.module.variable;

import java.util.Optional;

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
            return this.get() ? StarfruitMod.Colors.colorize("yes", 'a') : StarfruitMod.Colors.colorize("no", 'c');
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
}

