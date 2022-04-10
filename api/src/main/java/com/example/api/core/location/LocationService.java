package com.example.api.core.location;

import org.springframework.web.bind.annotation.*;

import java.util.List;

public interface LocationService {
    /**
     * Sample usage:
     *
     * curl -X POST $HOST:$PORT/location \
     *   -H "Content-Type: application/json" --data \
     *   '{"hotelId":123,"locationId":456,"country":"Serbia","town":"Belgrade","address":"Test Address"}'
     *
     * @param body
     * @return
     */
    @PostMapping(
        value    = "/location",
        consumes = "application/json",
        produces = "application/json")
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
    List<Location> getLocation(@RequestParam(value = "hotelId", required = true) int hotelId);
            /**
     * Sample usage:
     *
     * curl -X DELETE $HOST:$PORT/location?hotelId=1
     *
     * @param hotelId
     */
    @DeleteMapping(value = "/location")
    void deleteLocations(@RequestParam(value = "hotelId", required = true)  int hotelId);

}