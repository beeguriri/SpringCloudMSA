# ✨ SpringCloud로 개발하는 MSA
- 관련 강의 : [Spring Cloud로 개발하는 마이크로서비스 애플리케이션(MSA)](https://inf.run/GHeRm)

## ⭐ 목차
- [⭐ 개발 환경](#-개발-환경)
- [⭐ MicroService Architechture](#-microservice-architechture)
- [⭐ Service Discovery](#-service-discovery)
- [⭐ API GateWay](#-api-gateway)
- [⭐ Spring Security & JWT with Spring Cloud](#-spring-security--jwt-with-spring-cloud)

## ⭐ 개발 환경
- SpringBoot version `2.7.18`
- SpringCloud version `2021.0.9 aka jubilee`
- Java version `17`
- Type : Maven Project

## ⭐ MicroService Architechture
![](/images/msa_architecture.png)
- 독립적으로 배포, 확장 될 수 있는 서비스를 조합해서 하나의 큰 어플리케이션을 구성하는 패턴
- API를 통하여 MSA 서비스 간 통신
- 다양한 언어 및 프레임워크로 서비스 구성 할 수 있음
- Kafka 등의 메시징 처리를 통해 각 API 간 데이터 동기화
- 장애 격리 : 특정 서비스에 오류가 발생해도 다른 서비스에 영향 주지 않음

## ⭐ Spring Cloud
- Spring Cloud Starter
- Spring Cloud Config : 환경설정 관련정보 -> 다른 저장소에 모아둠
- Spring Cloud Netflix : 유레카(네이밍 서버)
- Spring Cloud Gateway : 요청 정보 분산 (로드밸런싱)
- Spring Cloud OpenFeign : 각 서비스간 통신 (API)
- Spring Cloud Security

## ⭐ Service Discovery
- 유레카 서버 등록
```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-eureka-server</artifactId>
</dependency>
```
```yaml
eureka:
  client:
    register-with-eureka: false
    fetch-registry: false
```
```java
@SpringBootApplication
@EnableEurekaServer
public class DiscoveryServiceApplication{
    //...
}
```
- 유레카 서비스 등록
```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
</dependency>
```
```yaml
eureka:
  client:
    register-with-eureka: true
    # 서버로부터 인스턴스들의 정보를 주기적으로 가져오겠다
    fetch-registry: true 
    service-url:
      #유레카 엔드포인트에 마이크로서비스 정보 등록
      defaultZone: http://127.0.0.1:8761/eureka 
```
```java
@SpringBootApplication
@EnableEurekaClient
public class UserServiceApplication {
    //...
}
```
- 서비스 인스턴스 여러개 띄울 때
  - 혹시 maven이 설치 되지 않았다면 [다운로드-maven.apache.org](https://maven.apache.org/download.cgi)
  - `3.8.8` 다운로드 및 압축해제, 환경변수-path 추가
```shell
$ mvn spring-boot:run '-Dspring-boot.run.jvmArguments=-Dserver.port=9002'
$ mvn spring-boot:run '-Dspring-boot.run.jvmArguments=-Dserver.port=9003'
```
![](/images/eureka_test.png)

## ⭐ API GateWay
- 단일 진입점을 통해 MicroService에 접근
- 인증, 권한 부여 단일 작업
- 속도 제한, 부하 분산
  - 여러 개의 인스턴스를 띄울 경우 라운드로빈 방식으로 로드발란싱
- Spring Cloud에서 MSA간 통신
  - RestTemplate : 서버의 주소, 포트 번호로 호출
  - Feign Client : 이름으로만 호출 할 수 있음
```xml
<dependency>
  <groupId>org.springframework.cloud</groupId>
  <artifactId>spring-cloud-starter-gateway</artifactId>
</dependency>
```
```yaml
spring:
  cloud:
    gateway:
      #global filter 적용
      default-filters:
        - name : GlobalFilter
          args:
            baseMessage: Spring Cloud Gateway Global Filter
            preLogger: true
            postLogger: true
      #routing 정보
      routes:
        - id: first-service
          uri: lb://MY-FIRST-SERVICE
          predicates:
            - Path=/first-service/**
          filters:
            # custom filter 각각 적용
            - CustomFilter
        - id: second-service
          uri: lb://MY-SECOND-SERVICE
          predicates:
            - Path=/second-service/**
          filters:
            # custom filter 각각 적용
            # OrderedGatewayFilter를 구현하여 filter 순서 지정 가능
            - name: CustomFilter
            - name: LoggingFilter
              args:
                baseMessage: Apply Custom Logging Filter!
                preLogger: true
                postLogger: true
```
## ⭐ Spring Security & JWT with Spring Cloud
### ✨ 인증(Authentication)
- 사용자가 ID, Password 입력하면
  - `AuthenticationFilter`의 `attemptAuthentication(...)`에서 `UsernamePasswordAuthenticationToken` 객체를 `AuthenticationManager`에 반환
  - `UserDetailsService`를 구현한 `UserServiceImpl`의 `loadUserByUsername(..)`에서 `User`객체 반환
- 정상적으로 로그인이 되면
  -  `AuthenticationFilter`의 `successfulAuthentication(...)`에서 token 생성 후 
  - response header에 token 추가 후 반환
- Token은 JWT 라이브러리 이용하여 생성
  - ```java
    return Jwts.builder()
            .setSubject(userDto.getUserId())
            .setExpiration(new Date(System.currentTimeMillis() + Long.parseLong(env.getProperty("token.expriration_time"))))
            .signWith(SignatureAlgorithm.HS512, env.getProperty("token.secret"))
            .compact();
    ``` 
  - [jwt.io](jwt.io) 에서 생성 된 토큰 복호화 test
  - ![](/images/jwt_io.png)
- (참고) JWT 사용하는 이유?
  - 세션/쿠키로 인증 할 경우 모바일 등 이기종간의 공유가 안됨
  - 클라이언트는 서비스를 요청할 때 Token을 함께 보내게 됨 => stateless
  - Token에 사용자 인증에 필요한 정보가 들어있기 때문에 별도의 인증 저장소 필요 없음
  - 서버는 사용자에 대한 세션을 유지할 필요가 없어 서버의 자원을 절감할 수 있음
### ✨ 인가(Authorization)
- gateway에서 라우팅 할때 필터를 통해 인가 처리함
  - 인증이 필요없는 회원가입, 로그인 외에는 서비스 요청 시 토큰을 함께 전달
  - 필터에서 토큰 복호화하여 확인 후 요청 서비스로 라우팅
  - ```yaml
    - id: user-service
      uri: lb://USER-SERVICE
      predicates:
        - Path=/user-service/**
        - Method=GET
      filters:
        - AuthorizationHeaderFilter
     ```
  - ```java
      return Jwts.parser().setSigningKey(env.getProperty("token.secret"))
                    .parseClaimsJws(token).getBody()
                    .getSubject(); 
    ``` 
  - ![](/images/token_request.png)
- (참고) jwt parsing 시에 `java.lang.NoClassDefFoundError: javax/xml/bind/DatatypeConverter` 발생하면 dependency 추가
  - ```xml
    <dependency>
        <groupId>org.glassfish.jaxb</groupId>
        <artifactId>jaxb-runtime</artifactId>
    </dependency>
    ```
