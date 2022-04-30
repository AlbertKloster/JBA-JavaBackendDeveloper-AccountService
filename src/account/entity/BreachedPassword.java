package account.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;

@Data
@Entity
public class BreachedPassword {
    @Id
    private String password;
}
