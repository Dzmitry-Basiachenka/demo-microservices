FROM openjdk:17-alpine
ARG JAR_FILE
COPY ${JAR_FILE} /song-service.jar
ENTRYPOINT ["java","-Dspring.profiles.active=docker","-jar","/song-service.jar"]
