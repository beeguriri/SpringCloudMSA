package wendy.study.userservice.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import wendy.study.userservice.dto.UserDto;
import wendy.study.userservice.service.UserService;
import wendy.study.userservice.vo.RequestLogin;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

@Slf4j
@RequiredArgsConstructor
public class AuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private final Environment env;
    private final UserService userService;

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException {
        try {
            //post 형태로 전달 되는 것은 parameter 형태로 받을 수 없기 때문에
            //input stream을 java class type으로 변경 시켜주고자 함.
            RequestLogin creds = new ObjectMapper().readValue(request.getInputStream(), RequestLogin.class);

            //인증정보 만들기
            //사용자가 입력한 email, id를 시큐리티에 사용하기 위해 token 형태로 변환해서
            //인증처리를 해주는 매니저에 넘김
            return getAuthenticationManager().authenticate(
                    new UsernamePasswordAuthenticationToken(
                            creds.getEmail(), creds.getPassword(),
                            new ArrayList<>()
                    )
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response,
                                            FilterChain chain, Authentication authResult)
            throws IOException, ServletException {
        //로그인 성공 했을 때 어떤 처리를 해 줄것 인지에 대한 로직 작성
        log.info("로그인 성공 한 user = {}", ((User)authResult.getPrincipal()).getUsername());

        String email = ((User)authResult.getPrincipal()).getUsername();
        UserDto userDto = userService.getUserByEmail(email);

        //Email로 userDto 가져온 후 token 생성
        String token = Jwts.builder()
                .setSubject(userDto.getUserId())
                .setExpiration(new Date(System.currentTimeMillis() + Long.parseLong(env.getProperty("token.expriration_time"))))
                .signWith(SignatureAlgorithm.HS512, env.getProperty("token.secret"))
                .compact();

        //header에 심어서 반환
        log.info("token = {}", token);
        response.addHeader("token", token);
        response.addHeader("userId", userDto.getUserId());

    }
}
