package wendy.study.userservice.vo;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
public class RequestUser {

    @NotNull(message = "Email은 필수값 입니다.")
    @Size(min = 2, message = "Email은 2글자 이상 입력 되어야 합니다.")
    @Email
    private String email;

    @NotNull(message = "이름은 필수값 입니다.")
    @Size(min = 2, message = "이름은 2글자 이상 입력 되어야 합니다.")
    private String name;

    @NotNull(message = "비밀번호는 필수값 입니다.")
    @Size(min = 8, message = "비밀번호는 8글자 이상 입력 되어야 합니다.")
    private String password;

}
