package wendy.study.userservice.config;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JCircuitBreakerFactory;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JConfigBuilder;
import org.springframework.cloud.client.circuitbreaker.Customizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class Resilience4JConfiguration {

    @Bean
    public Customizer<Resilience4JCircuitBreakerFactory> globalCustomConfig() {

        CircuitBreakerConfig circuitBreakerConfig = CircuitBreakerConfig.custom()
                .failureRateThreshold(4) //default 50, 100번에 4번 실패했을 때 circuit braeker open
                .waitDurationInOpenState(Duration.ofMillis(1000)) //circuit breaker open 상태 유지하는 지속시간
                .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED) //circuitbreaker 닫힐 때 통화결과 기록
                .slidingWindowSize(2) //circuitbreaker 닫힐 때 통화결과 기록 시 창의 크기 (횟수 또는 시간정보)
                .build();

        TimeLimiterConfig timeLimiterConfig = TimeLimiterConfig.custom()
                .timeoutDuration(Duration.ofSeconds(4))
                .build();

        return factory -> factory.configureDefault(
                id -> new Resilience4JConfigBuilder(id)
                        .timeLimiterConfig(timeLimiterConfig)
                        .circuitBreakerConfig(circuitBreakerConfig)
                        .build()
        );
    }
}
