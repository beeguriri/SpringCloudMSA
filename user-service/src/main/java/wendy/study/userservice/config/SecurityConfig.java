package wendy.study.userservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
public class SecurityConfig {

    //권한 작업
    @Bean
    protected SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http.csrf().disable();
        http.authorizeHttpRequests().antMatchers("/users/**").permitAll();


        return http.build();
    }

    //h2 web-console 사용하기 위한 설정
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        //인증 거치지 않고 접근 가능 하도록 허용
        return (web) -> web.ignoring()
                .requestMatchers(new AntPathRequestMatcher("/h2-console/**"));
    }

    //비밀번호 암호화
    @Bean
    BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
