FROM openjdk:17-alpine
ARG JAR_FILE
COPY ${JAR_FILE} /discovery-service.jar
ENTRYPOINT ["java","-Dspring.profiles.active=docker","-jar","/discovery-service.jar"]
