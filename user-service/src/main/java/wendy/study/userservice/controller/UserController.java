package wendy.study.userservice.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
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
@Slf4j
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
    public ResponseEntity<ResponseUser> createUser(@Valid @RequestBody RequestUser user, BindingResult result) {

        //TODO: 나중에 예외처리 좀 해줘야 할 듯
        if(result.hasErrors())
            log.error("error: {}", result.getFieldError());

        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        UserDto userDto = modelMapper.map(user, UserDto.class);

        userService.createUser(userDto);

        ResponseUser responseUser = modelMapper.map(userDto, ResponseUser.class);

        return ResponseEntity.status(HttpStatus.CREATED).body(responseUser);
    }
}
