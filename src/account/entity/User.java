package account.entity;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Data;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Set;

@Data
@Entity
@JsonFilter("UserFilter")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @NotNull
    @NotEmpty
    private String name;
    @NotNull
    @NotEmpty
    private String lastname;
    @NotNull
    @NotEmpty
    private String email;
    @NotNull
    @NotEmpty
    private String password;
    @ElementCollection(fetch = FetchType.EAGER)
    private Set<String> roles;
    private int failedAttempts;
    private boolean lastFailed;

}
