package account.repository;

import account.entity.BreachedPassword;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BreachedPasswordRepository extends JpaRepository<BreachedPassword, String> {
}
