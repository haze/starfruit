package software.tachyon.starfruit.command;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Scanner;
import java.util.StringJoiner;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import software.tachyon.starfruit.module.ModuleManager;
import software.tachyon.starfruit.module.variable.Variable;

public class Parser {

    public class Result {
        public final String command;
        public final Optional<String> selector;
        public final Optional<Object> value;

        protected Result(String command, String selector, Object value) {
            this.command = command;
            this.selector = Optional.ofNullable(selector);
            this.value = Optional.ofNullable(value);
        }

        @Override
        public String toString() {
            return String.format("cmd=%s, sel=%s, val=%s", this.command, this.selector, this.value);
        }
    }

    public Optional<Result> parseSetCommand(String input) {
        if (!ModuleManager.isInternalCommand(input))
            return Optional.empty();
        return this.parse(input.substring(1));
    }

    Optional<Result> parse(String input) {
        final Scanner scanner = new Scanner(input);
        String command = null;
        try {
            command = scanner.next();
            final String selector = scanner.next();
            final boolean nextArgIsCustomBool = scanner.hasNext(Variable.Bool.yesNo);
            if (scanner.hasNextInt()) {
                int number = scanner.nextInt();
                scanner.close();
                return Optional.of(new Result(command, selector, number));
            } else if (scanner.hasNextDouble()) {
                double number = scanner.nextDouble();
                scanner.close();
                return Optional.of(new Result(command, selector, number));
            } else if (scanner.hasNextBoolean() || nextArgIsCustomBool) {
                final boolean flag;
                if (nextArgIsCustomBool)
                    flag = (boolean) Variable.Bool.parse(scanner.next()).get();
                else
                    flag = scanner.nextBoolean();
                scanner.close();
                return Optional.of(new Result(command, selector, flag));
            } else if (scanner.hasNext()) {
                String text = scanner.next();
                scanner.close();
                return Optional.of(new Result(command, selector, text));
            } else {
                scanner.close();
                return Optional.of(new Result(command, selector, null));
            }
        } catch (NoSuchElementException ne) {
            if (command != null) {
                return Optional.of(new Result(command, null, null));
            }
            return Optional.empty();
        }
    }

}
