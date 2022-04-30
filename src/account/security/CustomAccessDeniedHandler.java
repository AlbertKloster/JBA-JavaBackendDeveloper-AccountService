package account.security;

import account.entity.Event;
import account.enums.Action;
import account.service.EventRepositoryService;
import account.utils.HttpRequestDispatcher;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    @Autowired
    EventRepositoryService eventRepositoryService;

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException {
        saveEvent(request);
        response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied!");
    }

    private void saveEvent(HttpServletRequest request) {
        Event event = new Event();
        event.setAction(Action.ACCESS_DENIED.name());
        event.setSubject(HttpRequestDispatcher.getNameFromRequest(request));
        event.setObject(request.getRequestURI());
        event.setPath(request.getRequestURI());
        eventRepositoryService.save(event);
    }

}
