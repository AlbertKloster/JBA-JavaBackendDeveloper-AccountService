package account.json;

import account.entity.User;
import lombok.Data;

import java.util.TreeSet;

@Data
public class JsonUser {
    private Long id;
    private String name;
    private String lastname;
    private String email;
    private TreeSet<String> roles;

    public JsonUser(User user) {
        this.id = user.getId();
        this.name = user.getName();
        this.lastname = user.getLastname();
        this.email = user.getEmail();
        this.roles = new TreeSet<>(user.getRoles());
    }
}
