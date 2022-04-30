package account.enums;

import account.exception.RoleNotFoundException;

import java.util.Locale;

public enum Role {
    ANONYMOUS, USER, ACCOUNTANT, ADMINISTRATOR, AUDITOR;

    public String getRole() {
        return this.name();
    }

    public String getAuthority() {
        return "ROLE_" + getRole();
    }

    public static String getAuthority(String role) {
        for (Role r : Role.values()) {
            if (r.name().equals(role.toUpperCase(Locale.ROOT)))
                return r.getAuthority();
        }
        throw new RoleNotFoundException();
    }

    public static String getRole(String role) {
        for (Role r : Role.values()) {
            if (r.name().equals(role.toUpperCase(Locale.ROOT)))
                return r.getRole();
        }
        throw new RoleNotFoundException();
    }

    public String getName() {
        return getRole().charAt(0) + getRole().substring(1).toLowerCase();
    }

}
