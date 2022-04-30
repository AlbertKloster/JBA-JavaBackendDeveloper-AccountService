package account.utils;

import account.entity.BreachedPassword;
import account.entity.User;
import account.exception.BreachedPasswordException;
import account.service.BreachedPasswordRepositoryService;

import java.util.List;

public class BreachPasswordChecker {

    public static void check(User user, BreachedPasswordRepositoryService breachedPasswordRepositoryService) {
        String password = user.getPassword();
        List<BreachedPassword> breachedPasswords = breachedPasswordRepositoryService.findAll();
        for (BreachedPassword breachedPassword : breachedPasswords) {
            if (breachedPassword.getPassword().equals(password))
                throw new BreachedPasswordException();
        }
    }
}
