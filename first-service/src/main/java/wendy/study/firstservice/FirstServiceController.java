package wendy.study.firstservice;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/first-service")
@Slf4j
@RequiredArgsConstructor
public class FirstServiceController {

    private final Environment env;

    @GetMapping("/hello")
    public String hello() {
        return "Hello First Service!";
    }

    @GetMapping("/message")
    public String message(@RequestHeader("first-request") String header) {
        log.info("header = {}", header);
        return "Hello First Service!";
    }

    @GetMapping("/check")
    public String check(HttpServletRequest request) {
        log.info("server port : {}", request.getServerPort());
        return String.format("This is a message from First Service on Port [%s]", env.getProperty("local.server.port"));
    }
}
