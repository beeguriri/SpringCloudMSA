package wendy.study.userservice.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.ObjectPostProcessor;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import wendy.study.userservice.service.UserService;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserService userService;
    private final ObjectPostProcessor<Object> objectPostProcessor;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final Environment env;

    //권한 작업
    @Bean
    protected SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http.csrf().disable();
//        http.authorizeHttpRequests().antMatchers("/users/**").permitAll(); //모든 요청에 대해 허가
        http.authorizeRequests().antMatchers("/**")
                .hasIpAddress("127.0.0.1")
                .and()
                .addFilter(getAuthenticationFilter());

        return http.build();
    }

    private AuthenticationFilter getAuthenticationFilter() throws Exception{

        AuthenticationFilter authenticationFilter = new AuthenticationFilter(env, userService);
        AuthenticationManagerBuilder builder = new AuthenticationManagerBuilder(objectPostProcessor);
        authenticationFilter.setAuthenticationManager(authenticationManager(builder));
        return authenticationFilter;
    }

    //db에 password는 encrypt 되어있음
    //로그인 할때 password도 encrypt 해줄 것
    public AuthenticationManager authenticationManager(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userService).passwordEncoder(bCryptPasswordEncoder);
        return auth.build();
    }

    //h2 web-console 사용하기 위한 설정
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        //인증 거치지 않고 접근 가능 하도록 허용
        return (web) -> web.ignoring()
                .requestMatchers(new AntPathRequestMatcher("/h2-console/**"));
    }

    /**
     * service에서도 해당 빈 참조하고
     * securigyConfig에서도 해당 빈 참조하니까
     * 서로 순환참조가 발생
     * --> 메인 클래스로 해당 빈 옮겨줘서 해결 함 (공통으로 사용 할 빈이므로...)
     */
    //비밀번호 암호화
//    @Bean
//    BCryptPasswordEncoder passwordEncoder() {
//        return new BCryptPasswordEncoder();
//    }
}
