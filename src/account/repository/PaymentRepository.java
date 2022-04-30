package account.repository;

import account.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByEmployeeAndPeriod(String employee, LocalDate period);
    List<Payment> findAllByEmployeeIgnoreCaseOrderByPeriodDesc(String employee);

}
