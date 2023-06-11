FROM openjdk:17-alpine
ARG JAR_FILE
COPY ${JAR_FILE} /storage-service.jar
ENTRYPOINT ["java","-Dspring.profiles.active=docker","-jar","/storage-service.jar"]
