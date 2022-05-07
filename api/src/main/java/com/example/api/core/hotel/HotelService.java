package com.example.api.core.hotel;

import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

public interface HotelService {
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
     Mono<Hotel> getHotel(@PathVariable int hotelId);

    void deleteHotel(@PathVariable int hotelId);

}
