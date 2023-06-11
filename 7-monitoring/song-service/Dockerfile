FROM openjdk:17-alpine
ARG JAR_FILE
COPY ${JAR_FILE} /song-service.jar
COPY build/otel/opentelemetry-javaagent.jar /opentelemetry-javaagent.jar
ENTRYPOINT ["java","-Dspring.profiles.active=docker","-jar","-javaagent:/opentelemetry-javaagent.jar","/song-service.jar"]
