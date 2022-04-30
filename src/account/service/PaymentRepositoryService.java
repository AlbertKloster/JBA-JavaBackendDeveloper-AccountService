package account.service;

import account.entity.Payment;
import account.entity.User;
import account.exception.NegativeSalaryException;
import account.exception.PaymentNotExistException;
import account.exception.PeriodNotUniqueException;
import account.repository.PaymentRepository;
import account.repository.UserRepository;
import account.utils.DateParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class PaymentRepositoryService {

    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;

    @Autowired
    public PaymentRepositoryService(PaymentRepository paymentRepository, UserRepository userRepository) {
        this.paymentRepository = paymentRepository;
        this.userRepository = userRepository;
    }

    public void save(Payment payment) {
        checkSalary(payment);
        checkPeriod(payment);
        paymentRepository.save(payment);
    }

    public void update(Payment payment) {
        Payment existingPayment =
                paymentRepository.findByEmployeeAndPeriod(payment.getEmployee(), DateParser.parse(payment.getPeriod()))
                        .orElseThrow(PaymentNotExistException::new);
        checkSalary(payment);
        existingPayment.setSalary(payment.getSalary());
        paymentRepository.save(existingPayment);
    }

    private void checkSalary(Payment payment) {
        if (payment.getSalary() < 0)
            throw new NegativeSalaryException();
    }

    private void checkPeriod(Payment payment) {
        if (findByEmployeeAndPeriod(payment).isPresent())
            throw new PeriodNotUniqueException();
    }

    private Optional<Payment> findByEmployeeAndPeriod(Payment payment) {
        return paymentRepository
                .findByEmployeeAndPeriod(payment.getEmployee(), DateParser.parse(payment.getPeriod()));
    }

    public Object findByEmployeeAndPeriod(String email, LocalDate period) {
        User user = userRepository.findUserByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST));
        Payment payment = paymentRepository.findByEmployeeAndPeriod(email, period)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST));
        return createPaymentResponse(payment, user);
    }

    public Object findAllByEmployeeIgnoreCaseOrderByPeriod(String email) {
        User user = userRepository.findUserByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST));
        List<Object> response = new LinkedList<>();
        paymentRepository.findAllByEmployeeIgnoreCaseOrderByPeriodDesc(email)
                .forEach(payment -> response.add(createPaymentResponse(payment, user)));
        return response;
    }

    private Object createPaymentResponse(Payment payment, User user) {
        return Map.of(
                "name", user.getName(),
                "lastname", user.getLastname(),
                "period", DateParser.parse(payment.getPeriod(), "LLLL-yyyy"),
                "salary", parseDollar(payment.getSalary())
        );
    }

    private String parseDollar(Long salary) {
        return String.format("%d dollar(s) %d cent(s)", salary/100, salary%100);
    }

}
