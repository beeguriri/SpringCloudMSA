FROM openjdk:17-ea-slim
VOLUME /tmp
COPY target/discovery-service-1.0.jar DiscoveryService.jar
ENTRYPOINT ["java","-jar","DiscoveryService.jar"]