package account.security;

import account.constant.Constant;
import account.entity.Event;
import account.entity.User;
import account.enums.Action;
import account.enums.Role;
import account.service.EventRepositoryService;
import account.service.UserRepositoryService;
import org.apache.tomcat.util.codec.binary.Base64;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;

import static account.utils.HttpRequestDispatcher.getNameFromRequest;

@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Autowired
    EventRepositoryService eventRepositoryService;

    @Autowired
    UserRepositoryService userRepositoryService;

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        String upd = request.getHeader("authorization");
        String message = "";
        if (upd != null && upd.matches("Basic .*"))
            message = logEvents(request);
        proceedException(request, response, message);
    }

    private void proceedException(HttpServletRequest request,
                                  HttpServletResponse response,
                                  String message) throws IOException {
        int status = HttpStatus.UNAUTHORIZED.value();
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(status);
        response.getWriter().write(new JSONObject()
                .put("timestamp", LocalDateTime.now())
                .put("status", status)
                .put("error", "Unauthorized")
                .put("message", message)
                .put("path", request.getRequestURI())
                .toString()
        );
    }

    private String logEvents(HttpServletRequest request) {
        increaseFailedAttempts(request);
        if (checkBruteForce(request))
            return "User account is locked";
        else
            saveLoginFailedEvent(request);
        return "";
    }

    private void saveEvent(HttpServletRequest request, String action, String object) {
        Event event = new Event();
        event.setAction(action);
        event.setSubject(getNameFromRequest(request));
        event.setObject(object);
        event.setPath(request.getRequestURI());
        eventRepositoryService.save(event);
    }

    private void saveLoginFailedEvent(HttpServletRequest request) {
        saveEvent(request, Action.LOGIN_FAILED.name(), request.getRequestURI());

    }

    private void saveBruteForceEvent(HttpServletRequest request) {
        saveEvent(request, Action.BRUTE_FORCE.name(), request.getRequestURI());

    }

    private void saveLockUserEvent(HttpServletRequest request) {
        String object = String.format("Lock user %s", getNameFromRequest(request));
        saveEvent(request, Action.LOCK_USER.name(), object);
    }

    private void increaseFailedAttempts(HttpServletRequest request) {
        User user = userRepositoryService.findUserByEmailOptional(getNameFromRequest(request)).orElse(null);
        if (user == null)
            return;
        if (hasAdminRole(user))
            return;
        user.setFailedAttempts(user.getFailedAttempts() + 1);
        user.setLastFailed(true);
            userRepositoryService.update(user);
    }

    boolean hasAdminRole(User user) {
        for (String role : user.getRoles()) {
            if (role.equals(Role.ADMINISTRATOR.getAuthority()))
                return true;
        }
        return false;
    }

    private boolean checkBruteForce(HttpServletRequest request) {
        User user = userRepositoryService.findUserByEmailOptional(getNameFromRequest(request)).orElse(null);
        if (user == null)
            return false;
        if (user.getFailedAttempts() == Constant.MAX_FAILED_ATTEMPTS) {
            saveLoginFailedEvent(request);
            saveBruteForceEvent(request);
            saveLockUserEvent(request);
        }
        return user.getFailedAttempts() >= Constant.MAX_FAILED_ATTEMPTS;
    }

}
