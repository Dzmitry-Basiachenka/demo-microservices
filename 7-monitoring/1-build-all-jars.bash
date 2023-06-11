#!/usr/bin/env bash

./gradlew clean build -p discovery-service && \
./gradlew clean build -p api-gateway-service && \
./gradlew clean build -x test -p song-service && \
./gradlew clean build -x test -p storage-service && \
./gradlew clean build -x test -p resource-processor && \
./gradlew clean build -x test -p resource-service
