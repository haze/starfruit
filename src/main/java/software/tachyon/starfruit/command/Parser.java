package software.tachyon.starfruit.command;

import java.lang.reflect.Field;
import java.util.Optional;
import java.util.Scanner;

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
        if (scanner.hasNextInt()) {
            int number = scanner.nextInt();
            scanner.close();
            return Optional.of(new SetVariable(moduleName, valueName, literal, number));
        } else if (scanner.hasNextDouble()) {
            double number = scanner.nextDouble();
            scanner.close();
            return Optional.of(new SetVariable(moduleName, valueName, literal, number));
        } else if (scanner.hasNextBoolean()) {
            double flag = scanner.nextDouble();
            scanner.close();
            return Optional.of(new SetVariable(moduleName, valueName, literal, flag));
        } else if (scanner.hasNext()) {
            double text = scanner.nextDouble();
            scanner.close();
            return Optional.of(new SetVariable(moduleName, valueName, literal, text));
        } else {
            scanner.close();
            return Optional.empty();
        }
    }

}
