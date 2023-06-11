#!/usr/bin/env bash

: ${RESOURCE_HOST=localhost}
: ${RESOURCE_PORT=8090}
: ${RESOURCE_PATH=resources}
: ${SONG_HOST=localhost}
: ${SONG_PORT=8090}
: ${SONG_PATH=songs}
: ${STORAGE_HOST=localhost}
: ${STORAGE_PORT=8090}
: ${STORAGE_PATH=storages}

function assertCurl() {
  local expectedHttpCode=$1
  local curlCmd="$2 -w \"%{http_code}\""
  local result=$(eval $curlCmd)
  local httpCode="${result:(-3)}"
  RESPONSE='' && ((${#result} > 3)) && RESPONSE="${result%???}"

  if [ "$httpCode" = "$expectedHttpCode" ]; then
    echo "Test OK (HTTP Code: $httpCode, response: $RESPONSE)"
  else
    echo "Test failed, expected HTTP Code: $expectedHttpCode, actual HTTP Code: $httpCode, will abort!"
    echo "Failing command: $curlCmd"
    echo "Response body: $RESPONSE"
    exit 1
  fi
}

function assertCurlDownload() {
  local expectedHttpCode=$1
  local curlCmd="$2 -w \"%{http_code}\""
  local result=$(eval $curlCmd)
  local httpCode="${result:(-3)}"

  if [ "$httpCode" = "$expectedHttpCode" ]; then
    echo "Test OK (HTTP Code: $httpCode)"
  else
    echo "Test failed, expected HTTP Code: $expectedHttpCode, actual HTTP Code: $httpCode, will abort!"
    echo "Failing command: $curlCmd"
    exit 1
  fi
}

function testCurl() {
  local expectedHttpCode=$1
  local curlCmd="$2 -w \"%{http_code}\""
  local result=$(eval $curlCmd)
  local httpCode="${result:(-3)}"
  RESPONSE='' && ((${#result} > 3)) && RESPONSE="${result%???}"

  if [ "$httpCode" = "$expectedHttpCode" ]; then
    echo "Test OK (HTTP Code: $httpCode, response: $RESPONSE)"
  else
    return 1
  fi
}

function assertEqual() {
  local expected=$1
  local actual=$2

  if [ "$actual" = "$expected" ]; then
    echo "Test OK (actual value: $actual)"
  else
    echo "Test failed, expected value: $expected, actual value: $actual, will abort"
    exit 1
  fi
}

function testUrl() {
  local url=$1

  if $url -ks -f -o /dev/null; then
    return 0
  else
    return 1
  fi
}

function waitForService() {
  local url=$1
  echo -n "Wait for: $url... "

  local n=0
  until testUrl $url; do
    n=$((n + 1))
    if [[ $n == 100 ]]; then
      echo " Give up"
      exit 1
    else
      sleep 5
      echo -n ", retry #$n "
    fi
  done
  echo "Done, continues..."
}

function testSongProcessed() {
  local id=$1

  if ! testCurl 200 "curl http://$SONG_HOST:$SONG_PORT/$SONG_PATH/$id -s"; then
    echo -n "Fail"
    return 1
  fi

  set +e

  assertEqual "$id" "$(echo $RESPONSE | jq .id)"
  if [ "$?" -eq "1" ]; then return 1; fi

  set -e
}

function testResourceUploadCompleted() {
  local id=$1

  if ! testCurl 200 "curl http://$RESOURCE_HOST:$RESOURCE_PORT/$RESOURCE_PATH/$id -s"; then
    echo -n "Fail"
    return 1
  fi

  set +e

  assertEqual "$id" "$(echo $RESPONSE | jq .id)"
  if [ "$?" -eq "1" ]; then return 1; fi

  assertEqual "2" "$(echo $RESPONSE | jq .storageId)"
  if [ "$?" -eq "1" ]; then return 1; fi

  set -e
}

function waitForSongProcessing() {
  local id=$1
  echo "Wait for song processing... "

  sleep 1

  local n=0
  until testSongProcessed $id; do
    n=$((n + 1))
    if [[ $n == 100 ]]; then
      echo " Give up"
      exit 1
    else
      sleep 5
      echo -n ", retry #$n "
    fi
  done
  echo "Done, continues..."
}

function waitForResourceUploadCompleting() {
  local id=$1
  echo "Wait for resource upload completing... "

  sleep 1

  local n=0
  until testResourceUploadCompleted $id; do
    n=$((n + 1))
    if [[ $n == 100 ]]; then
      echo " Give up"
      exit 1
    else
      sleep 5
      echo -n ", retry #$n "
    fi
  done
  echo "Done, continues..."
}

set -e

SECONDS=0
echo "Start tests"

echo "RESOURCE_HOST=${RESOURCE_HOST}"
echo "RESOURCE_PORT=${RESOURCE_PORT}"
echo "RESOURCE_PATH=${RESOURCE_PATH}"

echo "SONG_HOST=${SONG_HOST}"
echo "SONG_PORT=${SONG_PORT}"
echo "SONG_PATH=${SONG_PATH}"

echo "STORAGE_HOST=${STORAGE_HOST}"
echo "STORAGE_PORT=${STORAGE_PORT}"
echo "STORAGE_PATH=${STORAGE_PATH}"

if [[ $@ == *"start"* ]]; then
  echo "Starting Docker Compose environment..."
  echo "$ docker-compose down --remove-orphans"
  docker-compose down --remove-orphans
  echo "$ docker-compose up -d"
  docker-compose up -d
fi

echo
echo Check discovery service
assertCurl 200 "curl http://localhost:8761/actuator/health -s"
assertEqual '"UP"' "$(echo $RESPONSE | jq .status)"
echo
echo Check API gateway service
assertCurl 200 "curl http://localhost:8090/actuator/health -s"
assertEqual '"UP"' "$(echo $RESPONSE | jq .status)"
echo
echo Check resource service
assertCurl 200 "curl http://localhost:8081/actuator/health -s"
assertEqual '"UP"' "$(echo $RESPONSE | jq .status)"
echo
echo Check song service
assertCurl 200 "curl http://localhost:8082/actuator/health -s"
assertEqual '"UP"' "$(echo $RESPONSE | jq .status)"
echo
echo Check resource processor
assertCurl 200 "curl http://localhost:8083/actuator/health -s"
assertEqual '"UP"' "$(echo $RESPONSE | jq .status)"
echo
echo Check storage service
assertCurl 200 "curl http://localhost:8084/actuator/health -s"
assertEqual '"UP"' "$(echo $RESPONSE | jq .status)"

echo
echo Get storages
assertCurl 200 "curl -X GET http://$STORAGE_HOST:$STORAGE_PORT/$STORAGE_PATH -s"
assertEqual 2 "$(echo $RESPONSE | jq ". | length")"

echo
echo Upload resource
assertCurl 200 "curl -X POST http://$RESOURCE_HOST:$RESOURCE_PORT/$RESOURCE_PATH -H 'Content-Type: multipart/form-data' --form 'file=@audio.mp3;type=audio/mpeg' -s"
id="$(echo $RESPONSE | jq ".id")"

waitForSongProcessing $id
waitForResourceUploadCompleting $id

echo
echo Download resource
assertCurlDownload 200 "curl http://$RESOURCE_HOST:$RESOURCE_PORT/$RESOURCE_PATH/$id/download -s"

echo
echo Get song
assertCurl 200 "curl http://$SONG_HOST:$SONG_PORT/$SONG_PATH/$id -s"
assertEqual '"Impact Moderato"' "$(echo $RESPONSE | jq .name)"
assertEqual '"Kevin MacLeod"' "$(echo $RESPONSE | jq .artist)"
assertEqual '"Impact"' "$(echo $RESPONSE | jq .album)"
assertEqual '"75.67630767822266"' "$(echo $RESPONSE | jq .length)"
assertEqual '"2014-11-19T15:43:31"' "$(echo $RESPONSE | jq .released)"

echo
echo Delete song
assertCurl 200 "curl -X DELETE http://$SONG_HOST:$SONG_PORT/$SONG_PATH?ids=$id -s"
assertCurl 404 "curl http://$SONG_HOST:$SONG_PORT/$SONG_PATH/$id -s"

echo
echo Delete resource
assertCurl 200 "curl -X DELETE http://$RESOURCE_HOST:$RESOURCE_PORT/$RESOURCE_PATH?ids=$id -s"
assertCurl 404 "curl http://$RESOURCE_HOST:$RESOURCE_PORT/$RESOURCE_PATH/$id -s"

echo Get token
assertCurl 200 "curl -X POST -s 'http://keycloak:8100/realms/microservices-realm/protocol/openid-connect/token' \
-H 'Content-Type: application/x-www-form-urlencoded' \
-d 'username=testadmin' \
-d 'password=testadmin123' \
-d 'grant_type=password' \
-d 'client_id=storage-service'"

TOKEN="$(echo $RESPONSE | jq -r ".access_token")"
AUTHORIZATION="-H 'Authorization: Bearer $TOKEN'"

echo
echo Create storage
storage='{"type": "STAGING", "bucket": "resources-temporary"}'
assertCurl 401 "curl -X POST http://$STORAGE_HOST:$STORAGE_PORT/$STORAGE_PATH -H 'Content-Type: application/json' --data '$storage' -s"
assertCurl 200 "curl $AUTHORIZATION -X POST http://$STORAGE_HOST:$STORAGE_PORT/$STORAGE_PATH -H 'Content-Type: application/json' --data '$storage' -s"
storageId="$(echo $RESPONSE | jq ".id")"
echo
echo Get storages after creating
assertCurl 200 "curl -X GET http://$STORAGE_HOST:$STORAGE_PORT/$STORAGE_PATH -s"
assertEqual 3 "$(echo $RESPONSE | jq ". | length")"

echo
echo Delete storage
assertCurl 401 "curl -X DELETE http://$STORAGE_HOST:$STORAGE_PORT/$STORAGE_PATH?ids=$storageId -s"
assertCurl 200 "curl $AUTHORIZATION -X DELETE http://$STORAGE_HOST:$STORAGE_PORT/$STORAGE_PATH?ids=$storageId -s"
echo
echo Get storages after deleting
assertCurl 200 "curl -X GET http://$STORAGE_HOST:$STORAGE_PORT/$STORAGE_PATH -s"
assertEqual 2 "$(echo $RESPONSE | jq ". | length")"

if [[ $@ == *"stop"* ]]; then
  echo "Stopping Docker Compose environment..."
  echo "$ docker-compose down"
  docker-compose down
fi

echo "Finish tests in $SECONDS second(s)"
