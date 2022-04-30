package account.json;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class JsonAccess {

    @NotNull
    @NotBlank
    private String user;
    @NotNull
    @NotBlank
    private String operation;

}
