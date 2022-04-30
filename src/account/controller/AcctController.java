package account.controller;

import account.constant.Entrypoint;
import account.entity.Payment;
import account.entity.User;
import account.exception.WrongDateException;
import account.security.LockAccountHandler;
import account.service.PaymentRepositoryService;
import account.service.UserRepositoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
public class AcctController {

    private final PaymentRepositoryService paymentRepositoryService;
    private final UserRepositoryService userRepositoryService;


    @Autowired
    public AcctController(PaymentRepositoryService paymentRepositoryService,
                          UserRepositoryService userRepositoryService) {
        this.paymentRepositoryService = paymentRepositoryService;
        this.userRepositoryService = userRepositoryService;
    }

    @PostMapping(Entrypoint.ACCT_PAYMENTS)
    public Object postPayments(@RequestBody List<Payment> payments,
                               @AuthenticationPrincipal UserDetails userDetails) {
        LockAccountHandler.check(userRepositoryService, userDetails);
        checkPayments(payments);
        payments.forEach(paymentRepositoryService::save);
        return Map.of("status", "Added successfully!");
    }

    @PutMapping(Entrypoint.ACCT_PAYMENTS)
    public Object putPayments(@RequestBody Payment payment,
                              @AuthenticationPrincipal UserDetails userDetails) {
        LockAccountHandler.check(userRepositoryService, userDetails);
        if (isWrongDate(payment))
            throw new WrongDateException();
        paymentRepositoryService.update(payment);
        return Map.of("status", "Updated successfully!");
    }

    private void checkPayments(List<Payment> payments) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < payments.size(); i++) {
            checkSalary(builder, payments.get(i), i);
            checkDate(builder, payments.get(i), i);
        }
        String message = builder.toString();
        if (!message.isEmpty())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }

    private void checkSalary(StringBuilder builder, Payment payment, int i) {
        if (payment.getSalary() < 0) {
            if (builder.length() > 0)
                builder.append(", ");
            builder.append(String.format("payments[%d].salary: Salary must be non negative!", i));
        }
    }

    private boolean isWrongDate(Payment payment) {
        return payment.getPeriod().equals("01-0001");
    }

    private void checkDate(StringBuilder builder, Payment payment, int i) {
        if (isWrongDate(payment)) {
            if (builder.length() > 0)
                builder.append(", ");
            builder.append(String.format("payments[%d].period: Wrong date!", i));
        }
    }

}
