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
        public final String moduleName, valueName;
        public final Object value;

        protected SetVariable(String moduleName, String valueName, Object value) {
            this.moduleName = moduleName;
            this.valueName = valueName;
            this.value = value;
        }
    }

    public Optional<SetVariable> parseSetCommand(String input) {
        return this.parse(input.substring(1));
    }

    Optional<SetVariable> parse(String input) {
        final Scanner scanner = new Scanner(input);
        final String moduleName = scanner.next();
        final String valueName = scanner.next();
        final boolean nextArgIsCustomBool = scanner.hasNext(Variable.Bool.yesNo);
        if (scanner.hasNextInt()) {
            int number = scanner.nextInt();
            scanner.close();
            return Optional.of(new SetVariable(moduleName, valueName, number));
        } else if (scanner.hasNextDouble()) {
            double number = scanner.nextDouble();
            scanner.close();
            return Optional.of(new SetVariable(moduleName, valueName, number));
        } else if (scanner.hasNextBoolean() || nextArgIsCustomBool) {
            final boolean flag;
            if (nextArgIsCustomBool)
                flag = (boolean) Variable.Bool.parse(scanner.next()).get();
            else
                flag = scanner.nextBoolean();
            scanner.close();
            return Optional.of(new SetVariable(moduleName, valueName, flag));
        } else if (scanner.hasNext()) {
            String text = scanner.next();
            scanner.close();
            return Optional.of(new SetVariable(moduleName, valueName, text));
        } else {
            scanner.close();
            return Optional.empty();
        }
    }

}
