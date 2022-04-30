package account.controller;

import account.constant.Entrypoint;
import account.security.LockAccountHandler;
import account.service.PaymentRepositoryService;
import account.service.UserRepositoryService;
import account.utils.DateParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class EmplController {

    private final PaymentRepositoryService paymentRepositoryService;
    private final UserRepositoryService userRepositoryService;

    @Autowired
    public EmplController(PaymentRepositoryService paymentRepositoryService,
                          UserRepositoryService userRepositoryService) {
        this.paymentRepositoryService = paymentRepositoryService;
        this.userRepositoryService = userRepositoryService;
    }

    @RequestMapping(name = Entrypoint.EMPL_PAYMENT, params = "period")
    public Object getPayment(@RequestParam String period,
                             @AuthenticationPrincipal UserDetails userDetails) {
        LockAccountHandler.check(userRepositoryService, userDetails);
        return paymentRepositoryService.findByEmployeeAndPeriod(userDetails.getUsername(), DateParser.parse(period));
    }

    @RequestMapping(Entrypoint.EMPL_PAYMENT)
    public Object getPayments(@AuthenticationPrincipal UserDetails userDetails) {
        LockAccountHandler.check(userRepositoryService, userDetails);
        return paymentRepositoryService.findAllByEmployeeIgnoreCaseOrderByPeriod(userDetails.getUsername());
    }
}
