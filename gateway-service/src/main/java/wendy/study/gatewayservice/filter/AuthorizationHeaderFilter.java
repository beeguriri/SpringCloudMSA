package wendy.study.gatewayservice.filter;

import io.jsonwebtoken.Jwts;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class AuthorizationHeaderFilter extends AbstractGatewayFilterFactory<AuthorizationHeaderFilter.Config> {

    private final Environment env;

    public AuthorizationHeaderFilter(Environment env) {
        super(Config.class);
        this.env = env;
    }

    @Override
    public GatewayFilter apply(AuthorizationHeaderFilter.Config config) {
        return (exchange, chain) -> {
            //사용자 요청으로부터 받은 토큰(헤더)을 가져오기
            ServerHttpRequest request = exchange.getRequest();

            if(!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION))
                return onError(exchange, "no authorization header", HttpStatus.UNAUTHORIZED);

            String authorizationHeader = request.getHeaders().get(HttpHeaders.AUTHORIZATION).get(0);
            String token = authorizationHeader.replace("Bearer", "");

            //토큰 검증
            if (!isJwtValid(token))
                return onError(exchange, "JWT token is not valid", HttpStatus.UNAUTHORIZED);

            //Post-Filter 적용
            return chain.filter(exchange);
        };
    }

    private boolean isJwtValid(String token) {
        boolean returnValue = true;

        //token 복호화 하면 payload의 "subject"에 id return 되어옴
        String subject = null;
        try {
            log.info("token secret : {}", env.getProperty("token.secret"));
            subject = Jwts.parser().setSigningKey(env.getProperty("token.secret"))
                    .parseClaimsJws(token).getBody()
                    .getSubject();
        } catch (Exception e) {
            returnValue = false;
        }

        if (subject == null || subject.isEmpty())
            returnValue = false;

        //id값이 같은지 비교
        log.info("decoded token = {}", subject);

        return returnValue;
    }

    //Mono, Flux -> Spring WebFlux
    private Mono<Void> onError(ServerWebExchange exchange, String message, HttpStatus httpStatus) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(httpStatus);

        log.error(message);

        return response.setComplete();
    }

    @Data
    public static class Config {
        private String baseMessage;
        private boolean preLogger;
        private boolean postLogger;
    }
}
