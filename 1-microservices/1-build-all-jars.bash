#!/usr/bin/env bash

./gradlew clean build -x test -p song-service && \
./gradlew clean build -x test -p resource-processor && \
./gradlew clean build -x test -p resource-service
