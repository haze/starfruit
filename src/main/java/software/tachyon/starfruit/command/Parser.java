package software.tachyon.starfruit.command;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Optional;
import java.util.Scanner;
import java.util.StringJoiner;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import software.tachyon.starfruit.module.variable.Variable;

public class Parser {

    public class SetVariable {
        public final boolean literal;
        public final String moduleName, valueName;
        public final Object value;

        protected SetVariable(String moduleName, String valueName, boolean literal, Object value) {
            this.moduleName = moduleName;
            this.valueName = valueName;
            this.literal = literal;
            this.value = value;
        }
    }

    public Optional<SetVariable> parseSetCommand(String input) {
        final String inputLower = input.toLowerCase();
        if (inputLower.startsWith("set")) {
            return this.parse(input.substring(4), true); // "set "
        } else if (inputLower.startsWith(".")) {
            return this.parse(input.substring(1), false);
        } else {
            return Optional.empty();
        }
    }

    Optional<SetVariable> parse(String input, boolean literal) {
        final Scanner scanner = new Scanner(input);
        final String moduleName = scanner.next();
        final String valueName = scanner.next();
        final boolean nextArgIsCustomBool = scanner.hasNext(Variable.Bool.yesNo);
        System.out.println(nextArgIsCustomBool);
        System.out.println(scanner.hasNextBoolean());
        if (scanner.hasNextInt()) {
            int number = scanner.nextInt();
            scanner.close();
            return Optional.of(new SetVariable(moduleName, valueName, literal, number));
        } else if (scanner.hasNextDouble()) {
            double number = scanner.nextDouble();
            scanner.close();
            return Optional.of(new SetVariable(moduleName, valueName, literal, number));
        } else if (scanner.hasNextBoolean() || nextArgIsCustomBool) {
            final boolean flag;
            if (nextArgIsCustomBool)
                flag = (boolean) Variable.Bool.parse(scanner.next()).get();
            else
                flag = scanner.nextBoolean();
            scanner.close();
            return Optional.of(new SetVariable(moduleName, valueName, literal, flag));
        } else if (scanner.hasNext()) {
            String text = scanner.next();
            scanner.close();
            return Optional.of(new SetVariable(moduleName, valueName, literal, text));
        } else {
            scanner.close();
            return Optional.empty();
        }
    }

}
