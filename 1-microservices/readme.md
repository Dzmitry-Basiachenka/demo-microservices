Build jars
```
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
curl -s localhost:8082/actuator/health | jq -r .status
curl -s localhost:8083/actuator/health | jq -r .status
curl -s localhost:8081/actuator/health | jq -r .status
```
