package wendy.study.userservice.controller;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import wendy.study.userservice.dto.UserDto;
import wendy.study.userservice.service.UserService;
import wendy.study.userservice.vo.Greeting;
import wendy.study.userservice.vo.RequestUser;
import wendy.study.userservice.vo.ResponseUser;

import javax.validation.Valid;

@RestController
@RequestMapping("/")
@RequiredArgsConstructor
public class UserController {

//    private final Environment env;
    private final Greeting greeting;
    private final UserService userService;

    @GetMapping("/health_check")
    public String status() {
        return "It's Working in User Service";
    }

    @GetMapping("/welcome")
    public String welcome() {
        return greeting.getMessage();
//        return env.getProperty("greeting.message");
    }

    //회원가입
    @PostMapping("/users")
    // @valid를 걸면 아래와 같은 메시지가 뜨는데... 해결 해야됨
    // Resolved [org.springframework.web.bind.MethodArgumentNotValidException: Validation failed for argument [0]
    // in public org.springframework.http.ResponseEntity<wendy.study.userservice.vo.ResponseUser>
    // wendy.study.userservice.controller.UserController.createUser(wendy.study.userservice.vo.RequestUser):
    // [Field error in object 'requestUser' on field 'name': rejected value [wendy];
    // codes [Email.requestUser.name,Email.name,Email.java.lang.String,Email];
    // arguments [org.springframework.context.support.DefaultMessageSourceResolvable:
    // codes [requestUser.name,name]; arguments []; default message [name],[Ljavax.validation.constraints.Pattern$Flag;@3b5bb76b,.*];
    // default message [올바른 형식의 이메일 주소여야 합니다]] ]
    public ResponseEntity<ResponseUser> createUser(@RequestBody RequestUser user) {

        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        UserDto userDto = modelMapper.map(user, UserDto.class);
        userService.createUser(userDto);

        ResponseUser responseUser = modelMapper.map(userDto, ResponseUser.class);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseUser);
    }
}
