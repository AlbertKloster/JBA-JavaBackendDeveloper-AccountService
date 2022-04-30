package account.utils;

import account.entity.BreachedPassword;
import account.repository.BreachedPasswordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class BreachedPasswordsLoader implements ApplicationRunner {

    private final List<String> list = List.of(
            "PasswordForJanuary",
            "PasswordForFebruary",
            "PasswordForMarch",
            "PasswordForApril",
            "PasswordForMay",
            "PasswordForJune",
            "PasswordForJuly",
            "PasswordForAugust",
            "PasswordForSeptember",
            "PasswordForOctober",
            "PasswordForNovember",
            "PasswordForDecember"
    );

    private final BreachedPasswordRepository repository;

    @Autowired
    public BreachedPasswordsLoader(BreachedPasswordRepository repository) {
        this.repository = repository;
    }

    @Override
    public void run(ApplicationArguments args) {
        repository.deleteAll();
        list.forEach(password -> {
            BreachedPassword breachedPassword = new BreachedPassword();
            breachedPassword.setPassword(password);
            repository.save(breachedPassword);
        });
    }

}
