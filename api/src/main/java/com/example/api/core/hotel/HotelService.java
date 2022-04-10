package com.example.api.core.hotel;

import org.springframework.web.bind.annotation.*;

public interface HotelService {
    /**
     * Sample usage:
     *
     * curl -X POST $HOST:$PORT/hotel \
     *   -H "Content-Type: application/json" --data \
     *   '{"hotelId":123,"title":"Title 123","description":"Description","image":"http://www.image.com","createdOn":"2021-08-12"}'
     *
     * @param body
     * @return
     */
    @PostMapping(
        value    = "/hotel",
        consumes = "application/json",
        produces = "application/json")
    Hotel createHotel(@RequestBody Hotel body);
	
    /**
     * Sample usage: curl $HOST:$PORT/hotel/1
     *
     * @param hotelId
     * @return the hotel, if found, else null
     */
    @GetMapping(
        value    = "/hotel/{hotelId}",
        produces = "application/json")
     Hotel getHotel(@PathVariable int hotelId);
         
    /**
     * Sample usage:
     *
     * curl -X DELETE $HOST:$PORT/hotel/1
     *
     * @param hotelId
     */
    @DeleteMapping(value = "/hotel/{hotelId}")
    void deleteHotel(@PathVariable int hotelId);

}
