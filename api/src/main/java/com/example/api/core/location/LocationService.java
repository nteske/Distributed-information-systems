package com.example.api.core.location;

import org.springframework.web.bind.annotation.*;

import reactor.core.publisher.Flux;

public interface LocationService {
    Location createLocation(@RequestBody Location body);

    /**
     * Sample usage: curl $HOST:$PORT/location?hotelId=1
     *
     * @param hotelId
     * @return
     */
    @GetMapping(
        value    = "/location",
        produces = "application/json")
    Flux<Location> getLocation(@RequestParam(value = "hotelId", required = true) int hotelId);

    void deleteLocations(@RequestParam(value = "hotelId", required = true)  int hotelId);

}