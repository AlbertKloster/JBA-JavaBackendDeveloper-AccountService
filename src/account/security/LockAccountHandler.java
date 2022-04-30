package account.security;

import account.constant.Constant;
import account.entity.User;
import account.service.UserRepositoryService;
import org.springframework.security.core.userdetails.UserDetails;

public class LockAccountHandler {

    private static void unlock(UserRepositoryService userRepositoryService, UserDetails userDetails) {
        User user = userRepositoryService.findUserByEmail(userDetails.getUsername());
            user.setFailedAttempts(0);
            user.setLastFailed(false);
            userRepositoryService.update(user);

    }

    public static void check(UserRepositoryService userRepositoryService, UserDetails userDetails) {
        User user = userRepositoryService.findUserByEmail(userDetails.getUsername());
        if (user.getFailedAttempts() < Constant.MAX_FAILED_ATTEMPTS)
            unlock(userRepositoryService, userDetails);
    }
}
