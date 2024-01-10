package wendy.study.userservice.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL) //null값은 버린다!
public class ResponseUser {
    private String email;
    private String name;
    private String userId;
}
