Run tests
```
./gradlew clean test -p discovery-service
./gradlew clean test -p api-gateway-service
./gradlew clean test -p song-service
./gradlew clean test -p resource-processor
./gradlew clean test -p resource-service
```

Run contract tests
```
./gradlew clean contractTest -p song-service
./gradlew clean build publishToMavenLocal -p song-service
./gradlew clean consumerContractTest -p resource-processor
```

Run BDD tests
```
./gradlew clean cucumber -p resource-service
```

Build jars
```
./gradlew clean build -p discovery-service
./gradlew clean build -p api-gateway-service
./gradlew clean build -x test -p song-service
./gradlew clean build -x test -p resource-processor
./gradlew clean build -x test -p resource-service
```

Run Docker Compose
```
docker-compose build
docker-compose up
docker-compose down
docker-compose rm -svf
docker system prune -a
```

Test health
```
curl -s localhost:8761/actuator/health | jq -r .status
curl -s localhost:8090/actuator/health | jq -r .status
curl -s localhost:8082/actuator/health | jq -r .status
curl -s localhost:8083/actuator/health | jq -r .status
curl -s localhost:8081/actuator/health | jq -r .status
```

Open discovery service UI
```
xdg-open http://localhost:8761
```

Get discovered services
```
curl -s localhost:8761/eureka/apps | jq -r .applications.application[].instance[].instanceId
```

