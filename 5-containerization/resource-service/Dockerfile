FROM openjdk:17-alpine
ARG JAR_FILE
COPY ${JAR_FILE} /resource-service.jar
ENTRYPOINT ["java","-Dspring.profiles.active=docker","-jar","/resource-service.jar"]
