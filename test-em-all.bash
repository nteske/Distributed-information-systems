#!/usr/bin/env bash
#
# ./gradlew clean build
# docker-compose build
# docker-compose up -d
#
# Sample usage:
#
#   HOST=localhost PORT=7000 ./test-em-all.bash
#
: ${HOST=localhost}
: ${PORT=8443}
: ${HOT_ID_REVS_LOC_ROO=2}
: ${HOT_ID_NOT_FOUND=13}
: ${HOT_ID_NO_LOC_NO_ROO=114}
: ${HOT_ID_NO_REVS_NO_ROO=214}

function assertCurl() {

    local expectedHttpCode=$1
    local curlCmd="$2 -w \"%{http_code}\""
    local result=$(eval $curlCmd)
    local httpCode="${result:(-3)}"
    RESPONSE='' && (( ${#result} > 3 )) && RESPONSE="${result%???}"

	if [ "$httpCode" = "$expectedHttpCode" ]
    then
        if [ "$httpCode" = "200" ]
        then
            echo "Test OK (HTTP Code: $httpCode)"
        else
            echo "Test OK (HTTP Code: $httpCode, $RESPONSE)"
        fi
        return 0
    else
        echo  "Test FAILED, EXPECTED HTTP Code: $expectedHttpCode, GOT: $httpCode, WILL ABORT!"
        echo  "- Failing command: $curlCmd"
        echo  "- Response Body: $RESPONSE"
        return 1
    fi
}

function assertEqual() {

    local expected=$1
    local actual=$2

	if [ "$actual" = "$expected" ]
    then
        echo "Test OK (actual value: $actual)"
        return 0
    else
        echo "Test FAILED, EXPECTED VALUE: $expected, ACTUAL VALUE: $actual, WILL ABORT"
        return 1
    fi
}


function testUrl() {
    url=$@
	if $url -ks -f
    then
          return 0
    else
          return 1
    fi;
}

function waitForService() {
    url=$@
    echo -n "Wait for: $url... "
    n=0
    until testUrl $url
    do
        n=$((n + 1))
        if [[ $n == 100 ]]
        then
            echo " Give up"
            exit 1
        else
            sleep 3
            echo -n ", retry #$n "
        fi
    done
    echo "DONE, continues..."
}

function testCompositeCreated() {

    # Expect that the Hotel Composite for hotelId $HOT_ID_REVS_LOC_ROO has been created with three reviews, three location and three rooms
    if ! assertCurl 200 "curl $AUTH -k https://$HOST:$PORT/hotel-composite/$HOT_ID_REVS_LOC_ROO -s"
    then
        echo -n "FAIL"
        return 1
    fi

    set +e
    assertEqual "$HOT_ID_REVS_LOC_ROO" $(echo $RESPONSE | jq .hotelId)
    if [ "$?" -eq "1" ] ; then return 1; fi

    assertEqual 3 $(echo $RESPONSE | jq ".location | length")
    if [ "$?" -eq "1" ] ; then return 1; fi

    assertEqual 3 $(echo $RESPONSE | jq ".reviews | length")
    if [ "$?" -eq "1" ] ; then return 1; fi

	assertEqual 3 $(echo $RESPONSE | jq ".rooms | length")
    if [ "$?" -eq "1" ] ; then return 1; fi
    set -e
}

function waitForMessageProcessing() {
    echo "Wait for messages to be processed... "

    # Give background processing some time to complete...
    sleep 5

    n=0
    until testCompositeCreated
    do
        n=$((n + 1))
        if [[ $n == 40 ]]
        then
            echo " Give up"
            exit 1
        else
            sleep 3
            echo -n ", retry #$n "
        fi
    done
    echo "All messages are now processed!"
}

function recreateComposite() {
    local hotelId=$1
    local composite=$2

    assertCurl 200 "curl $AUTH -X DELETE -k https://$HOST:$PORT/hotel-composite/${hotelId} -s"
    curl -X POST -k https://$HOST:$PORT/hotel-composite -H "Content-Type: application/json" -H "Authorization: Bearer $ACCESS_TOKEN" --data "$composite"
}

function setupTestdata() {
 body="{\"hotelId\":$HOT_ID_NO_LOC_NO_ROO"
    body+=\
',"title":"Hotel 114","description":"Description","image":"Image","createdOn":"2021-08-12", "reviews":[
	{"reviewId":1,"rating":0,"description":"Description 1","createdOn":"2021-08-12"},
    {"reviewId":2,"rating":0,"description":"Description 2","createdOn":"2021-08-12"},
    {"reviewId":3,"rating":0,"description":"Description 3","createdOn":"2021-08-12"}
]}'
    recreateComposite "$HOT_ID_NO_LOC_NO_ROO" "$body"

body="{\"hotelId\":$HOT_ID_NO_REVS_NO_ROO"
    body+=\
',"title":"Hotel 214","description":"Description 2","image":"Image 3","createdOn":"2021-08-12", "room":[
		{"roomId":1,324,3,3333},
        {"roomId":2,325,3,3333},
        {"roomId":3,326,3,3333}
	]
}'
    recreateComposite "$HOT_ID_NO_REVS_NO_ROO" "$body"
	
body="{\"hotelId\":$HOT_ID_REVS_LOC_ROO"
    body+=\
',"title":"Hotel 214","description":"Description 2","image":"Image 3","createdOn":"2021-08-12", "location":[
		{"locationId":1,"country":"Serbia","town":"Belgrade","address":"Test"},
        {"locationId":2,"country":"Croatia","town":"Zagreb","address":"None"},
        {"locationId":3,"country":"Slovenia","town":"Ljubljana","address":"no address"}
	], "reviews":[
	    {"reviewId":1,"rating":0,"description":"Description 1","createdOn":"2021-08-12"},
        {"reviewId":2,"rating":0,"description":"Description 2","createdOn":"2021-08-12"},
        {"reviewId":3,"rating":0,"description":"Description 3","createdOn":"2021-08-12"}
    ], "room":[
		{"roomId":1,324,3,3333},
        {"roomId":2,325,3,3333},
        {"roomId":3,326,3,3333}
    ]}'
    recreateComposite "$HOT_ID_REVS_LOC_ROO" "$body"

}

function testCircuitBreaker() {

    echo "Start Circuit Breaker tests!"

    EXEC="docker run --rm -it --network=my-network alpine"

    # First, use the health - endpoint to verify that the circuit breaker is closed
    assertEqual "CLOSED" "$($EXEC wget hotel-composite:8080/actuator/health -qO - | jq -r .components.circuitBreakers.details.hotel.details.state)"

    # Open the circuit breaker by running three slow calls in a row, i.e. that cause a timeout exception
    # Also, verify that we get 500 back and a timeout related error message
    for ((n=0; n<3; n++))
    do
        assertCurl 500 "curl -k https://$HOST:$PORT/hotel-composite/$HOT_ID_REVS_LOC_ROO?delay=3 $AUTH -s"
        message=$(echo $RESPONSE | jq -r .message)
        assertEqual "Did not observe any item or terminal signal within 2000ms" "${message:0:57}"
    done

    # Verify that the circuit breaker now is open by running the slow call again, verify it gets 200 back, i.e. fail fast works, and a response from the fallback method.
    assertCurl 200 "curl -k https://$HOST:$PORT/hotel-composite/$HOT_ID_REVS_LOC_ROO?delay=3 $AUTH -s"
    assertEqual "Fallback hotel2" "$(echo "$RESPONSE" | jq -r .title)"

    # Also, verify that the circuit breaker is open by running a normal call, verify it also gets 200 back and a response from the fallback method.
    assertCurl 200 "curl -k https://$HOST:$PORT/hotel-composite/$HOT_ID_REVS_LOC_ROO $AUTH -s"
    assertEqual "Fallback hotel2" "$(echo "$RESPONSE" | jq -r .title)"

    # Verify that a 404 (Not Found) error is returned for a non existing hotelId ($HOT_ID_NOT_FOUND) from the fallback method.
    assertCurl 404 "curl -k https://$HOST:$PORT/hotel-composite/$HOT_ID_NOT_FOUND $AUTH -s"
    assertEqual "Hotel Id: $HOT_ID_NOT_FOUND not found in fallback cache!" "$(echo $RESPONSE | jq -r .message)"

    # Wait for the circuit breaker to transition to the half open state (i.e. max 10 sec)
    echo "Will sleep for 10 sec waiting for the CB to go Half Open..."
    sleep 10

    # Verify that the circuit breaker is in half open state
    assertEqual "HALF_OPEN" "$($EXEC wget hotel-composite:8080/actuator/health -qO - | jq -r .components.circuitBreakers.details.hotel.details.state)"

    # Close the circuit breaker by running three normal calls in a row
    # Also, verify that we get 200 back and a response based on information in the hotel database
    for ((n=0; n<3; n++))
    do
        assertCurl 200 "curl -k https://$HOST:$PORT/hotel-composite/$HOT_ID_REVS_LOC_ROO $AUTH -s"
        assertEqual "Hotel 214" "$(echo "$RESPONSE" | jq -r .title)"
    done

    # Verify that the circuit breaker is in closed state again
    assertEqual "CLOSED" "$($EXEC wget hotel-composite:8080/actuator/health -qO - | jq -r .components.circuitBreakers.details.hotel.details.state)"

    # Verify that the expected state transitions happened in the circuit breaker
    assertEqual "CLOSED_TO_OPEN"      "$($EXEC wget hotel-composite:8080/actuator/circuitbreakerevents/hotel/STATE_TRANSITION -qO - | jq -r .circuitBreakerEvents[-3].stateTransition)"
    assertEqual "OPEN_TO_HALF_OPEN"   "$($EXEC wget hotel-composite:8080/actuator/circuitbreakerevents/hotel/STATE_TRANSITION -qO - | jq -r .circuitBreakerEvents[-2].stateTransition)"
    assertEqual "HALF_OPEN_TO_CLOSED" "$($EXEC wget hotel-composite:8080/actuator/circuitbreakerevents/hotel/STATE_TRANSITION -qO - | jq -r .circuitBreakerEvents[-1].stateTransition)"
}

set -e

echo "Start Tests:" `date`

echo "HOST=${HOST}"
echo "PORT=${PORT}"

if [[ $@ == *"start"* ]]
then
    echo "Restarting the test environment..."
    echo "$ docker-compose down --remove-orphans"
    docker-compose down --remove-orphans
    echo "$ docker-compose up -d"
    docker-compose up -d
fi

waitForService curl -k https://$HOST:$PORT/actuator/health

ACCESS_TOKEN=$(curl -k https://writer:secret@$HOST:$PORT/oauth/token -d grant_type=password -d username=magnus -d password=password -s | jq .access_token -r)
AUTH="-H \"Authorization: Bearer $ACCESS_TOKEN\""

setupTestdata

waitForMessageProcessing

# Verify that a normal request works, expect three room, three reviews and three rooms
assertCurl 200 "curl -k https://$HOST:$PORT/hotel-composite/$HOT_ID_REVS_LOC_ROO $AUTH -s"
assertEqual "$HOT_ID_REVS_LOC_ROO" $(echo $RESPONSE | jq .hotelId)
assertEqual 3 $(echo $RESPONSE | jq ".location | length")
assertEqual 3 $(echo $RESPONSE | jq ".reviews | length")
assertEqual 3 $(echo $RESPONSE | jq ".room | length")

# Verify that a 404 (Not Found) error is returned for a non existing hotelId ($HOT_ID_NOT_FOUND)
assertCurl 404 "curl -k https://$HOST:$PORT/hotel-composite/$HOT_ID_NOT_FOUND $AUTH -s"

# Verify that no room and no rooms are returned for hotelId $HOT_ID_NO_LOC_NO_ROO
assertCurl 200 "curl -k https://$HOST:$PORT/hotel-composite/$HOT_ID_NO_LOC_NO_ROO $AUTH -s"
assertEqual "$HOT_ID_NO_LOC_NO_ROO" $(echo $RESPONSE | jq .hotelId)
assertEqual 0 $(echo $RESPONSE | jq ".location | length")
assertEqual 3 $(echo $RESPONSE | jq ".reviews | length")
assertEqual 0 $(echo $RESPONSE | jq ".room | length")

# Verify that no reviews and no rooms are returned for hotelId $HOT_ID_NO_REVS_NO_ROO
assertCurl 200 "curl -k https://$HOST:$PORT/hotel-composite/$HOT_ID_NO_REVS_NO_ROO $AUTH -s"
assertEqual "$HOT_ID_NO_REVS_NO_ROO" $(echo $RESPONSE | jq .hotelId)
assertEqual 3 $(echo $RESPONSE | jq ".location | length")
assertEqual 0 $(echo $RESPONSE | jq ".reviews | length")
assertEqual 0 $(echo $RESPONSE | jq ".room | length")

# Verify that a 422 (Unprocessable Entity) error is returned for a hotelId that is out of range (-1)
assertCurl 422 "curl -k https://$HOST:$PORT/hotel-composite/-1 $AUTH -s"
assertEqual "\"Invalid hotelId: -1\"" "$(echo $RESPONSE | jq .message)"

# Verify that a 400 (Bad Request) error error is returned for a hotelId that is not a number, i.e. invalid format
assertCurl 400 "curl -k https://$HOST:$PORT/hotel-composite/invalidHotelId $AUTH -s"
assertEqual "\"Type mismatch.\"" "$(echo $RESPONSE | jq .message)"

# Verify that a request without access token fails on 401, Unauthorized
assertCurl 401 "curl -k https://$HOST:$PORT/hotel-composite/$HOT_ID_REVS_LOC_ROO -s"

# Verify that the reader - client with only read scope can call the read API but not delete API.
READER_ACCESS_TOKEN=$(curl -k https://reader:secret@$HOST:$PORT/oauth/token -d grant_type=password -d username=magnus -d password=password -s | jq .access_token -r)
READER_AUTH="-H \"Authorization: Bearer $READER_ACCESS_TOKEN\""

assertCurl 200 "curl -k https://$HOST:$PORT/hotel-composite/$HOT_ID_REVS_LOC_ROO $READER_AUTH -s"
assertCurl 403 "curl -k https://$HOST:$PORT/hotel-composite/$HOT_ID_REVS_LOC_ROO $READER_AUTH -X DELETE -s"

testCircuitBreaker

echo "End, all tests OK:" `date`

if [[ $@ == *"stop"* ]]
then
    echo "Stopping the test environment..."
    echo "$ docker-compose down --remove-orphans"
    docker-compose down --remove-orphans
fi