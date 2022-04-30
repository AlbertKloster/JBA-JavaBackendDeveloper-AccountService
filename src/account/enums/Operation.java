package account.enums;

import account.exception.WrongOperationException;

import java.util.Locale;

public enum Operation {
    GRANT, REMOVE;

    public static Operation getOperation(String operation) {
        for (Operation o : Operation.values()) {
            if (o.name().equals(operation.toUpperCase(Locale.ROOT)))
                return o;
        }
        throw new WrongOperationException();
    }
}
