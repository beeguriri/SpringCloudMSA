# ✨ SpringCloud로 개발하는 MSA
- 관련 강의 : [Spring Cloud로 개발하는 마이크로서비스 애플리케이션(MSA)](https://inf.run/GHeRm)

## ⭐ 목차
- [⭐ 개발 환경](#-개발-환경)
- [⭐ 실행 순서](#-실행-순서)
- [⭐ MicroService Architechture](#-microservice-architechture)
- [⭐ Service Discovery](#-service-discovery)
- [⭐ API GateWay](#-api-gateway)
- [⭐ Spring Security & JWT with Spring Cloud](#-spring-security--jwt-with-spring-cloud)
- [⭐ Spring Cloud Config Server](#-spring-cloud-config-server)
- [⭐ Spring Cloud Bus](#-spring-cloud-bus)
- [⭐ Config 정보의 암호화 처리](#-config-정보의-암호화-처리)
- [⭐ 데이터 동기화를 위한 Apache Kafka 활용](#-데이터-동기화를-위한-apache-kafka-활용)
- [⭐ 장애 처리와 분산 추적](#-장애-처리와-분산-추적)

## ⭐ 개발 환경
- SpringBoot version `2.7.18`
- SpringCloud version `2021.0.9 aka jubilee`
- Java version `17`
- Type : Maven Project
- Apache Kafka `2.13-3.6.1`
- Apache Kafka Connect `7.5.3`
- JDBC Connector `10.7.4`
- Maria DB `2.7.11` 
- 해당 project test를 하고자 할 경우 "C://Documents(내문서)"에 "etc폴더 내 2개의 폴더 옮겨 놓기"

## ⭐ 실행 순서
```shell
# zookeeper-server 기동
kafka_2.13-3.6.1$ .\bin\windows\zookeeper-server-start.bat .\config\zookeeper.properties

# kafka server 기동
kafka_2.13-3.6.1$ .\bin\windows\kafka-server-start.bat .\config\server.properties

# eureka server 기동
SpringCloudMSA\discovery-service$ mvn spring-boot:run

# config service 실행
SpringCloudMSA\config-service$ mvn spring-boot:run

# api gateway service 실행
SpringCloudMSA\gateway-service$ mvn spring-boot:run

# kafka connector 실행
confluent-7.5.3$ .\bin\windows\connect-distributed.bat .\etc\kafka\connect-distributed.properties

# kafka-console-consumer로 확인
kafka_2.13-3.6.1$ .\bin\windows\kafka-console-consumer.bat --bootstrap-server localhost:9092 --topic orders --from-beginning
```
- 필요 시 db의 table 만들기 (Maria DB CMD)
```shell
# mariadb 실행 
$ mysql -uroot -p
# password 입력

> show databases;
> create database mydb;
> use mydb;

> create table orders (
    -> id int auto_increment primary key,
    -> user_id varchar(50) not null,
    -> product_id varchar(20) not null,
    -> order_id varchar(50) not null,
    -> qty int default 0,
    -> unit_price int default 0,
    -> total_price int default 0,
    -> created_at datetime default now()
    -> );
```
- 필요 시 sink-connector 만들기 (POST localhost:8083/connectors/)
```json
{
  "name" : "my-order-sink-connect",
  "config" : {
    "connector.class" : "io.confluent.connect.jdbc.JdbcSinkConnector",
    "connection.url" : "jdbc:mysql://localhost:3306/mydb",
    "connection.user" : "root",
    "connection.password" : "test1357",
    "auto.create" : "true",
    "auto.evolve" : "true",
    "delete.enabled" : "false",
    "tasks.max" : "1",
    "topics" : "orders"
  }
}
```
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

## ⭐ Spring Cloud Config Server
- 각 서비스를 다시 빌드하지 않고 바로 적용하기 위해
- 구성에 필요한 설정 정보를 외부시스템에서 관리
### ✨ config-service
```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-config-server</artifactId>
</dependency>
```
```yaml
spring:
  cloud:
    config:
      server:
        git:
          uri: https://github.com/beeguriri/SpringCloudMSA
```
```java
@SpringBootApplication
@EnableConfigServer
public class ConfigServiceApplication {
  ...
}
```
### ✨ 각 service
```xml
<!-- cloud config 사용을 위한 dependency 추가 -->
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-config</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-bootstrap</artifactId>
</dependency>
<!-- actuator 사용을 위한 dependency 추가 -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```
```yaml
  # config 정보 입력
spring:
  cloud:
    config:
      name: ecommerce
  config:
    import: optional:configserver:http://127.0.0.1:8888
```

## ⭐ Spring Cloud Bus
- 분산 시스템의 노드를 경량 메시지 브로커와 연결
- 상태 및 구성에 대한 변경 사항을 연결 된 노드에 전달 (broadcast)
### ✨ AMQP
- 메시지 브로커
- 초당 20+ 메시지를 컨슈머에게 전달
- `메시지 전달 보장`, 시스템 간 메시지 전달
- 브로커, 컨슈머 중심

### ✨ Kafka
- `초당 100k+ 이상`의 이벤트 처리
- pub/sub, topic에 메시지 전달
- ack를 기다리지 않고 전달 가능
- 프로듀서 중심

### ✨ AMQP 적용하기
- [Erlang 설치](https://www.erlang.org/downloads) `26.2.1`, 환경변수 추가
- [RabbitMq 설치](https://rabbitmq.com/install-windows.html) `3.12.12`, 환경변수 추가
- management plugin 설치
```shell
$ rabbitmq-server -detached
$ rabbitmq-plugins enable rabbitmq_management

# 실행 안 될 경우
$ rabbitmq-service.bat remove
$ rabbitmq-service.bat install
$ rabbitmq-server -detached
$ rabbitmq-plugins enable rabbitmq_management
```
- http://localhost:15672/ 접속 되는지 확인
  - id : guest / pw: guest
- dependency 추가 및 yml 파일 설정
```xml
<!-- rabbitmq 사용을 위한 dependency 추가 -->
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-bus-amqp</artifactId>
</dependency>
```
```yaml
spring:
  #rabbitmq 설정
  rabbitmq:
    host: 127.0.0.1
    port: 5672
    username: guest
    password: guest
```
### ✨ config 파일 실행 테스트
- rabbitmq server 실행
- config-service 실행
- discovery-service 실행
- gateway-service 실행
- user-service 실행
- POST http://localhost:8000/user-service/actuator/busrefresh 로 서비스 재기동 및 config 변경 정보 반영 되는 것 확인

### ✨ (참고) busrefresh api 관련 에러 해결
- 서비스들이 재기동 되지만 config 내용 변경이 반영되지 않을 경우
- 별도의 bootstrap.yml 파일 작성, 
- application.yml에서 bootstrap 파일 불러오기
```yaml
# config-service의 application.yml
spring:
  profiles:
    active: native
  cloud:
    config:
      server:
        native:
          # http://127.0.0.1:8888/user-service/native 등으로 경로 반영 잘 됐는 지 확인
          # 실제 경로는 "file:/C:/Users/pooh1/Documents/native-file-repo/ecommerce.yml"
          search-locations: file:///${user.home}\Documents\native-file-repo

# gateway-service/user-service bootstrap.yml
spring:
  cloud:
    config:
      uri: http://127.0.0.1:8888
      name: ecommerce # config-service에서 참조하는 위치에 있는 yml 파일의 이름 작성

# gateway-service/user-service application.yml
spring:
  config:
    import:
      - classpath:/bootstrap.yml
```

## ⭐ Config 정보의 암호화 처리
### ✨ 대칭키를 이용한 암호화
- bootstrap.yml 에 encrypt key값 작성 하여 적용
- POST 127.0.0.1:8888/encrypt, decrypt 에서 test
  ![](/images/encrypt.png)
  ![](/images/decrypt.png)
- db의 접속정보를 별도(외부)의 config파일에 작성 및 암호화 후 test
  ```yaml
  # config-service bootstrap.yml에 encrypt key 작성
  encrypt:
    key: hello1234spring
  
  # user-service에서 외부에서 불러올 config 파일 설정
  spring:
    cloud:
      config:
        uri: http://127.0.0.1:8888
        name: user-service
        
  # 외부의 user-service.yml 파일에 database 접속정보 설정
  spring:
    datasource:
      url: jdbc:h2:mem:testdb
      username: sa
      password: '{cipher}ce8f7745d1387a521c0044fae541ceb35d507bc7797095e24fbbed2224cf50c1'
  ```
  ![](/images/encrypt_db_source.png)
### ✨ 비대칭키를 이용한 암호화
- JDK 11버전 이상 사용 권장
- RSA 알고리즘 사용
  ```shell
  # private key 생성
  $ keytool -genkeypair -alias apiEncryptionKey -keyalg RSA 
    -dname "CN=wendy, OU=API Development, O=https://github.com/beeguriri, L=Seoul, C=KR" 
    -keypass "test1234" -keystore apiEncryptionKey.jks
  
  Generating 2,048 bit RSA key pair and self-signed certificate (SHA256withRSA) with a validity of 90 days
  for: CN=wendy, OU=API Development, O=https://github.com/beeguriri, L=Seoul, C=KR
  ```
  ![](/images/gen_private_key.png)
  ```shell
  # 인증서 생성
  $ keytool -export -alias apiEncryptionKey -keystore apiEncryptionKey.jks -rfc -file trustServer.cer
  
  # public key 생성
  $ keytool -import -alias trustServer -file trustServer.cer -keystore publicKey.jks
  ```
  ![](/images/noncymetrickey.png)
- 키 적용 확인
  ![](/images/rsa_encrypt.png)
  ![](/images/rsa_decrypt.png)

## ⭐ 데이터 동기화를 위한 Apache Kafka 활용
### ✨ [Apache Kafka 다운로드 및 테스트](ApacheKafka사용하기.md)
### ✨ Apache Kafka
- kafka를 통해 각 다른 DB와 각 다른 서비스 간 데이터(메시지) 전달
- Producer / Consumer 분리
- Zookeeper : 메타데이터(Broker ID, Controller ID 등) 저장, Controller 정보 저장
- order-service에 요청 된 주문의 수량 정보를 catalog-service에 반영
  - order service -> kafka topic으로 produce
  - catalog service -> kafka topic consume
```xml
<!-- kafka 사용을 위한 dependency 추가 -->
<dependency>
    <groupId>org.springframework.kafka</groupId>
    <artifactId>spring-kafka</artifactId>
</dependency>
```
```java
//configuration에 annotation 추가
@EnableKafka
@Configuration
public class KafkaProducerConfig {
  //...
}
```
### ✨ Apache Kafka Connector
- order-service의 인스턴스를 여러개 실행 할 경우 데이터 동기화 문제가 발생
  - 유레카 서버에서 RR 방식으로 인스턴스를 호출하기 때문에!
- order-service로 들어 온 주문 정보를 kafka topic으로 전송하여
- sink-connector가 db에 저장할 수 있도록 함
- 이때 message 양식 맞춰서 전송하기!!!
  - schema, payload에 각각 필요한 정보 실어서 json string 만들어주기

## ⭐ 장애 처리와 분산 추적
### ✨ 장애 처리 
- CircuitBreaker
  - 장애가 발생하는 서비스에 반복적인 호출이 되지 못하게 차단
  - 특정 서비스가 정상적으로 동작하지 않을 경우 다른 기능으로 대체 수행 -> 장애 회피
  - Hystrix는 스프링부트 2.4.x 이후 버전부터는 사용 하지 않음
- Resilience4J
  - CircuitBreaker 지원
  - ```xml
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-circuitbreaker-resilience4j</artifactId>
        </dependency>
     ```
    ```java
        CircuitBreaker circuitbreaker = circuitBreakerFactory.create("circuitbreaker");
        List<ResponseOrder> orderList = circuitbreaker.run(() ->
                            orderServiceClient.getOrders(userId),
                           throwable -> new ArrayList<>()
        );
    ```
  - 별도 configration 파일 만들어서 CircuitBreakerFactory 설정 내용 변경할 수 있음
### ✨ 분산 추적
- Spring Cloud Sleuth : Trace ID, Span ID 부여, Zipkin과 연동
  - `span` : 하나의 요청에 사용 되는 작업의 단위
  - `trace` : Trace ID, 사용자가 요청 한 span이 모여서 하나의 trace 구성
- 설치
```shell
curl -sSL https://zipkin.io/quickstart.sh | bash -s
java -jar zipkin.jar
```
- 아... 진짜... 윈도우... 파워쉘.... 아.......
- curl 명령어 아.... 진짜......

