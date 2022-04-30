package account.enums;

import account.exception.WrongOperationException;

import java.util.Locale;

public enum Access {
    LOCK, UNLOCK;

    public static Access getAccess(String access) {
        for (Access a : Access.values()) {
            if (a.name().equals(access.toUpperCase(Locale.ROOT)))
                return a;
        }
        throw new WrongOperationException();
    }
}
