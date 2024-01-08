package wendy.study.gatewayservice.filter;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.OrderedGatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class LoggingFilter extends AbstractGatewayFilterFactory<LoggingFilter.Config> {

    public LoggingFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {

        //filter의 우선순위 지정하기
        return new OrderedGatewayFilter((exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            ServerHttpResponse response = exchange.getResponse();

            log.info("Logging Filter Base Message : {}", config.getBaseMessage());

            //Pre-Filter 적용
            if(config.isPreLogger())
                log.info("Logging Pre Filter  : request uri -> {}", request.getURI());

            //Post-Filter 적용
            return chain.filter(exchange).then(Mono.fromRunnable(() -> {
                if(config.isPostLogger())
                    log.info("Logging Post Filter : response code -> {}", response.getStatusCode());
            }));
        }, Ordered.HIGHEST_PRECEDENCE);
    }

    @Data
    public static class Config {
        private String baseMessage;
        private boolean preLogger;
        private boolean postLogger;
    }
}
