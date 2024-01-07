# ✨ SpringCloud로 개발하는 MSA
- 관련 강의 : [Spring Cloud로 개발하는 마이크로서비스 애플리케이션(MSA)](https://inf.run/GHeRm)

## 개발 환경
- SpringBoot version `2.7.18`
- SpringCloud version `2021.0.9 aka jubilee`
- Java version `17`
- Type : Maven Project

## MicroService Architechture
![](/images/msa_architecture.png)
- 독립적으로 배포, 확장 될 수 있는 서비스를 조합해서 하나의 큰 어플리케이션을 구성하는 패턴
- API를 통하여 MSA 서비스 간 통신
- 다양한 언어 및 프레임워크로 서비스 구성 할 수 있음
- Kafka 등의 메시징 처리를 통해 각 API 간 데이터 동기화
- 장애 격리 : 특정 서비스에 오류가 발생해도 다른 서비스에 영향 주지 않음

## Spring Cloud
- Spring Cloud Starter
- Spring Cloud Config : 환경설정 관련정보 -> 다른 저장소에 모아둠
- Spring Cloud Netflix : 유레카(네이밍 서버)
- Spring Cloud Gateway : 요청 정보 분산 (로드밸런싱)
- Spring Cloud OpenFeign : 각 서비스간 통신 (API)
- Spring Cloud Security

## Service Discovery
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
```
$ mvn spring-boot:run '-Dspring-boot.run.jvmArguments=-Dserver.port=9002'
$ mvn spring-boot:run '-Dspring-boot.run.jvmArguments=-Dserver.port=9003'
```
![](/images/eureka_test.png)