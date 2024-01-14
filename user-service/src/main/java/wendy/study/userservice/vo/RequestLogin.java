package wendy.study.userservice.vo;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
public class RequestLogin {

    @NotNull(message = "E-mail은 필수값 입니다.")
    @Size(min = 2, message = "E-mail은 2글자 이상 입니다.")
    @Email
    private String email;

    @NotNull(message = "비밀번호는 필수값 입니다.")
    @Size(min = 8, message = "비밀번호는 8글자 이상 16글자 이하 입니다.")
    private String password;
}
