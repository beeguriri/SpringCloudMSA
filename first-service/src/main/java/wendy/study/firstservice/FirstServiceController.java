package wendy.study.firstservice;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/first-service")
public class FirstServiceController {

    @GetMapping("/hello")
    public String hello() {
        return "Hello First Service!";
    }
}
