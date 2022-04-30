package account.controller;

import account.constant.Entrypoint;
import account.entity.Event;
import account.entity.User;
import account.enums.Action;
import account.enums.Role;
import account.exception.PasswordLengthException;
import account.exception.PasswordMustBeDifferentException;
import account.exception.WrongEmailException;
import account.json.JsonUser;
import account.security.LockAccountHandler;
import account.service.BreachedPasswordRepositoryService;
import account.service.EventRepositoryService;
import account.service.UserRepositoryService;
import account.utils.BreachPasswordChecker;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Map;

@RestController
public class AuthController {

    private final UserRepositoryService userRepositoryService;
    private final BreachedPasswordRepositoryService breachedPasswordRepositoryService;
    private final EventRepositoryService eventRepositoryService;
    private final PasswordEncoder encoder;

    @Autowired
    public AuthController(UserRepositoryService userRepositoryService,
                          BreachedPasswordRepositoryService breachedPasswordRepositoryService,
                          EventRepositoryService eventRepositoryService,
                          PasswordEncoder encoder) {
        this.userRepositoryService = userRepositoryService;
        this.breachedPasswordRepositoryService = breachedPasswordRepositoryService;
        this.eventRepositoryService = eventRepositoryService;
        this.encoder = encoder;
    }

    @PostMapping(Entrypoint.AUTH_SIGNUP)
    public Object postUser(@Valid @RequestBody User user) {
        BreachPasswordChecker.check(user, breachedPasswordRepositoryService);
        checkPasswordLength(user.getPassword());
        checkEmail(user.getEmail());
        JsonUser response = new JsonUser(userRepositoryService.save(user));
        saveCreateUserEvent(user.getEmail());
        return response;
    }

    private void saveEvent(String email, String action, String subject, String path) {
        User user = userRepositoryService.findUserByEmail(email);
        Event event = new Event();
        event.setAction(action);
        event.setSubject(subject);
        event.setObject(user.getEmail());
        event.setPath(path);
        eventRepositoryService.save(event);
    }


    private void saveCreateUserEvent(String email) {
        saveEvent(email, Action.CREATE_USER.name(), Role.ANONYMOUS.getName(), Entrypoint.AUTH_SIGNUP);
    }

    @PostMapping(Entrypoint.AUTH_CHANGEPASS)
    public Object postChangePass(@Valid @RequestBody NewPassword newPassword,
                                 @AuthenticationPrincipal UserDetails userDetails) {
        LockAccountHandler.check(userRepositoryService, userDetails);
        checkNewPassword(userDetails, newPassword);
        setNewPassword(userDetails, newPassword);
        Object response = createChangepassResponseBody(userDetails);
        saveChangepassEvent(userDetails.getUsername());
        return response;
    }

    private void saveChangepassEvent(String email) {
        saveEvent(email, Action.CHANGE_PASSWORD.name(), email, Entrypoint.AUTH_CHANGEPASS);
    }

    private Object createChangepassResponseBody(UserDetails userDetails) {
        return Map.of(
                "email", userDetails.getUsername(),
                "status", "The password has been updated successfully"
        );
    }

    private User getUser(UserDetails userDetails) {
        return userRepositoryService.findUserByEmail(userDetails.getUsername());
    }

    private void checkEmail(String email) {
        if (!email.matches(".+@acme.com"))
            throw new WrongEmailException();
    }

    private void setNewPassword(UserDetails userDetails, NewPassword newPassword) {
        User user = getUser(userDetails);
        user.setPassword(newPassword.new_password);
        BreachPasswordChecker.check(user, breachedPasswordRepositoryService); // validate new password
        user.setPassword(encoder.encode(user.getPassword())); // encode new password
        userRepositoryService.update(user);
    }

    private void checkNewPassword(UserDetails userDetails, NewPassword newPassword) {
        User user = getUser(userDetails);
        if (isSamePassword(newPassword, user))
            throw new PasswordMustBeDifferentException();
        checkPasswordLength(newPassword.getNew_password());
    }

    private void checkPasswordLength(String password) {
        int MIN_PASSWORD_LENGTH = 12;
        if (password.length() < MIN_PASSWORD_LENGTH)
            throw new PasswordLengthException();
    }

    private boolean isSamePassword(NewPassword newPassword, User user) {
        return encoder.matches(newPassword.getNew_password().trim(), user.getPassword());
    }

    @Data
    static class NewPassword {
        @NotNull
        @NotEmpty
        private String new_password;
    }

}
