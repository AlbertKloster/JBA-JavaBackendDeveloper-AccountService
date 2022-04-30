package account.controller;

import account.constant.Entrypoint;
import account.entity.Event;
import account.entity.User;
import account.enums.Access;
import account.enums.Action;
import account.enums.Operation;
import account.enums.Role;
import account.exception.*;
import account.json.JsonAccess;
import account.json.JsonRole;
import account.json.JsonUser;
import account.service.EventRepositoryService;
import account.service.UserRepositoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@RestController
public class AdminController {

    private final UserRepositoryService userRepositoryService;
    private final EventRepositoryService eventRepositoryService;

    @Autowired
    public AdminController(UserRepositoryService userRepositoryService,
                           EventRepositoryService eventRepositoryService) {
        this.userRepositoryService = userRepositoryService;
        this.eventRepositoryService = eventRepositoryService;
    }

    @GetMapping(Entrypoint.ADMIN_USER)
    public Object getUser() {
        List<JsonUser> userList = new ArrayList<>();
        List<User> users = userRepositoryService.findAll();
        users.forEach(user -> userList.add(new JsonUser(user)));
        return userList;
    }

    @PutMapping(Entrypoint.ADMIN_USER_ROLE)
    public Object putRole(@Valid @RequestBody JsonRole jsonRole,
                          @AuthenticationPrincipal UserDetails userDetails) {
        return new JsonUser(userRepositoryService.update(updateRole(jsonRole, userDetails)));
    }

    @PutMapping(Entrypoint.ADMIN_USER_ACCESS)
    public Object putAccess(@Valid @RequestBody JsonAccess jsonAccess,
                            @AuthenticationPrincipal UserDetails userDetails) {
        String email = jsonAccess.getUser().toLowerCase(Locale.ROOT);
        String message = "";
        switch (Access.getAccess(jsonAccess.getOperation())) {
            case LOCK: {
                message = "locked";
                saveLockUserEvent(userDetails, jsonAccess);
                break;
            }
            case UNLOCK: {
                message = "unlocked";
                saveUnlockUserEvent(userDetails, jsonAccess);
                break;
            }
        }
        return getAccessResponse(email, message);
    }

    private Object getAccessResponse(String email, String message) {
        return Map.of("status", String.format("User %s %s!", email, message));
    }

    private User updateRole(JsonRole jsonRole, UserDetails userDetails) {
        User user = userRepositoryService.findUserByEmail(jsonRole.getUser());
        checkAdministrativeAndBusinessRoles(user, jsonRole);
        String role = jsonRole.getRole();
        switch (Operation.getOperation(jsonRole.getOperation())) {
            case GRANT: {
                user.getRoles().add(Role.getAuthority(role));
                saveGrantRoleEvent(userDetails, jsonRole);
                break;
            }
            case REMOVE: {
                checkRemoveAdministratorRole(jsonRole);
                checkUserHaveRole(user, jsonRole);
                checkSingleRole(user);
                user.getRoles().remove(Role.getAuthority(role));
                saveRemoveRoleEvent(userDetails, jsonRole);
                break;
            }
        }
        return user;
    }

    private void checkSingleRole(User user) {
        if (user.getRoles().size() == 1)
            throw new RemoveSingleRoleException();
    }

    private void checkUserHaveRole(User user, JsonRole jsonRole) {
        if (!user.getRoles().contains(Role.getAuthority(jsonRole.getRole())))
            throw new UserNotHaveRoleException();
    }

    private void checkAdministrativeAndBusinessRoles(User user, JsonRole jsonRole) {
        if (isAdministrativeAndBusinessRoles(user, jsonRole) || isBusinessAndAdministratorRoles(user, jsonRole))
            throw new UserCombineAdministrativeAndBusinessRolesException();
    }

    private boolean isAdministrativeAndBusinessRoles(User user, JsonRole jsonRole) {
        return user.getRoles().contains(Role.ADMINISTRATOR.getAuthority()) &&
                !jsonRole.getRole().equals(Role.ADMINISTRATOR.getRole());
    }

    private boolean isBusinessAndAdministratorRoles(User user, JsonRole jsonRole) {
        return !user.getRoles().contains(Role.ADMINISTRATOR.getAuthority()) &&
                jsonRole.getRole().equals(Role.ADMINISTRATOR.getRole());
    }

    private void checkRemoveAdministratorRole(JsonRole jsonRole) {
        if (Role.getAuthority(jsonRole.getRole()).equals(Role.ADMINISTRATOR.getAuthority()))
            throw new RemoveAdministratorException();
    }

    @DeleteMapping(Entrypoint.ADMIN_USER + "/{email}")
    public Object deleteUser(@PathVariable(required = false) String email,
                             @AuthenticationPrincipal UserDetails userDetails) {
        if (email == null)
            throw new UserNotFoundException();
        userRepositoryService.deleteUserByEmail(email);
        saveDeleteUserEvent(userDetails, email);
        return getDeleteUserResponse(email);
    }

    private Object getDeleteUserResponse(String email) {
        return Map.of(
                "user", email,
                "status", "Deleted successfully!"
        );
    }

    private void saveEvent(UserDetails userDetails, JsonRole jsonRole, String objectMessage) {
        User user = userRepositoryService.findUserByEmail(userDetails.getUsername());
        Event event = new Event();
        event.setAction(getAction(jsonRole));
        event.setSubject(user.getEmail());
        event.setObject(getObject(jsonRole, objectMessage));
        event.setPath(Entrypoint.AUTH_CHANGEPASS);
        eventRepositoryService.save(event);
    }

    private String getAction(JsonRole jsonRole) {
        return Operation.getOperation(jsonRole.getOperation()).name() + "_ROLE";
    }

    private String getObject(JsonRole jsonRole, String objectMessage) {
        return String.format(objectMessage,
                Role.getRole(jsonRole.getRole()), jsonRole.getUser().toLowerCase());
    }

    private void saveGrantRoleEvent(UserDetails userDetails, JsonRole jsonRole) {
        saveEvent(userDetails, jsonRole, "Grant role %s to %s");
    }

    private void saveRemoveRoleEvent(UserDetails userDetails, JsonRole jsonRole) {
        saveEvent(userDetails, jsonRole, "Remove role %s from %s");
    }

    private void saveDeleteUserEvent(UserDetails userDetails, String email) {
        User user = userRepositoryService.findUserByEmail(userDetails.getUsername());
        Event event = new Event();
        event.setAction(Action.DELETE_USER.name());
        event.setSubject(user.getEmail());
        event.setObject(email);
        event.setPath(Entrypoint.ADMIN_USER);
        eventRepositoryService.save(event);
    }

    private void saveAccessEvent(UserDetails userDetails, String action, String object) {
        User user = userRepositoryService.findUserByEmail(userDetails.getUsername());
        Event event = new Event();
        event.setAction(action);
        event.setSubject(user.getEmail());
        event.setObject(object);
        event.setPath(Entrypoint.ADMIN_USER_ACCESS);
        eventRepositoryService.save(event);
    }

    private void saveLockUserEvent(UserDetails userDetails, JsonAccess jsonAccess) {
        String email = jsonAccess.getUser().toLowerCase(Locale.ROOT);
        String object = String.format("Lock user %s", email);
        saveAccessEvent(userDetails, Action.LOCK_USER.name(), object);
        userRepositoryService.lock(email);
    }

    private void saveUnlockUserEvent(UserDetails userDetails, JsonAccess jsonAccess) {
        String email = jsonAccess.getUser().toLowerCase(Locale.ROOT);
        String object = String.format("Unlock user %s", email);
        saveAccessEvent(userDetails, Action.UNLOCK_USER.name(), object);
        userRepositoryService.unlock(email);
    }

}
