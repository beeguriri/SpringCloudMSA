FROM openjdk:17-ea-slim
VOLUME /tmp
COPY target/user-service-1.1.jar UserService.jar
ENTRYPOINT ["java","-jar","UserService.jar"]