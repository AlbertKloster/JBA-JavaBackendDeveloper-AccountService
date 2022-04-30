package account.controller;

import account.constant.Entrypoint;
import account.security.LockAccountHandler;
import account.service.EventRepositoryService;
import account.service.UserRepositoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SecurityController {

    private final EventRepositoryService eventRepositoryService;
    private final UserRepositoryService userRepositoryService;

    @Autowired
    public SecurityController(EventRepositoryService eventRepositoryService,
                              UserRepositoryService userRepositoryService) {
        this.eventRepositoryService = eventRepositoryService;
        this.userRepositoryService = userRepositoryService;
    }

    @GetMapping(Entrypoint.SECURITY_EVENTS)
    public Object getEvents(@AuthenticationPrincipal UserDetails userDetails) {
        LockAccountHandler.check(userRepositoryService, userDetails);
        return eventRepositoryService.findAll();
    }
}
