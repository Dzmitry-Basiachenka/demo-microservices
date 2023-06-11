FROM openjdk:17-alpine
ARG JAR_FILE
COPY ${JAR_FILE} /resource-processor.jar
ENTRYPOINT ["java","-Dspring.profiles.active=docker","-jar","/resource-processor.jar"]
