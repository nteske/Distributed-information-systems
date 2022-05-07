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
: ${PORT=8080}
: ${HOT_ID_REVS_LOC_ROO=2}
: ${HOT_ID_NOT_FOUND=14}
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
    if ! assertCurl 200 "curl http://$HOST:$PORT/hotel-composite/$HOT_ID_REVS_LOC_ROO -s"
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

    assertCurl 200 "curl -X DELETE http://$HOST:$PORT/hotel-composite/${hotelId} -s"
    curl -X POST http://$HOST:$PORT/hotel-composite -H "Content-Type: application/json" --data "$composite"
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

waitForService curl http://$HOST:$PORT/actuator/health

setupTestdata

waitForMessageProcessing

# Verify that a normal request works, expect three room, three reviews and three rooms
assertCurl 200 "curl http://$HOST:$PORT/hotel-composite/$HOT_ID_REVS_LOC_ROO -s"
assertEqual "$HOT_ID_REVS_LOC_ROO" $(echo $RESPONSE | jq .hotelId)
assertEqual 3 $(echo $RESPONSE | jq ".location | length")
assertEqual 3 $(echo $RESPONSE | jq ".reviews | length")
assertEqual 3 $(echo $RESPONSE | jq ".room | length")

# Verify that a 404 (Not Found) error is returned for a non existing hotelId ($HOT_ID_NOT_FOUND)
assertCurl 404 "curl http://$HOST:$PORT/hotel-composite/$HOT_ID_NOT_FOUND -s"

# Verify that no room and no rooms are returned for hotelId $HOT_ID_NO_LOC_NO_ROO
assertCurl 200 "curl http://$HOST:$PORT/hotel-composite/$HOT_ID_NO_LOC_NO_ROO -s"
assertEqual "$HOT_ID_NO_LOC_NO_ROO" $(echo $RESPONSE | jq .hotelId)
assertEqual 0 $(echo $RESPONSE | jq ".location | length")
assertEqual 3 $(echo $RESPONSE | jq ".reviews | length")
assertEqual 0 $(echo $RESPONSE | jq ".room | length")

# Verify that no reviews and no rooms are returned for hotelId $HOT_ID_NO_REVS_NO_ROO
assertCurl 200 "curl http://$HOST:$PORT/hotel-composite/$HOT_ID_NO_REVS_NO_ROO -s"
assertEqual "$HOT_ID_NO_REVS_NO_ROO" $(echo $RESPONSE | jq .hotelId)
assertEqual 3 $(echo $RESPONSE | jq ".location | length")
assertEqual 0 $(echo $RESPONSE | jq ".reviews | length")
assertEqual 0 $(echo $RESPONSE | jq ".room | length")

# Verify that a 422 (Unprocessable Entity) error is returned for a hotelId that is out of range (-1)
assertCurl 422 "curl http://$HOST:$PORT/hotel-composite/-1 -s"
assertEqual "\"Invalid hotelId: -1\"" "$(echo $RESPONSE | jq .message)"

# Verify that a 400 (Bad Request) error error is returned for a hotelId that is not a number, i.e. invalid format
assertCurl 400 "curl http://$HOST:$PORT/hotel-composite/invalidHotelId -s"
assertEqual "\"Type mismatch.\"" "$(echo $RESPONSE | jq .message)"

echo "End, all tests OK:" `date`

if [[ $@ == *"stop"* ]]
then
    echo "Stopping the test environment..."
    echo "$ docker-compose down --remove-orphans"
    docker-compose down --remove-orphans
fi