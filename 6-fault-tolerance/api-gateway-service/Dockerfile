FROM openjdk:17-alpine
ARG JAR_FILE
COPY ${JAR_FILE} /api-gateway-service.jar
ENTRYPOINT ["java","-Dspring.profiles.active=docker","-jar","/api-gateway-service.jar"]
