package account.json;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Locale;

@Data
public class JsonRole {
    @NotNull
    @NotBlank
    private String user;
    @NotNull
    @NotBlank
    private String role;
    @NotNull
    @NotBlank
    private String operation;

    public String getUser() {
        return user.toLowerCase(Locale.ROOT);
    }
}
