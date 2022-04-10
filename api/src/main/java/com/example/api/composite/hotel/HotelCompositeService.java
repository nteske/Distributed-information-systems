package com.example.api.composite.hotel;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;


@Api(description = "REST API for composite hotel information.")

public interface HotelCompositeService {

    /**
     * Sample usage: curl $HOST:$PORT/hotel-composite/1
     *
     * @param hotelId
     * @return the composite hotel info, if found, else null
     */
    @ApiOperation(
        value = "${api.hotel-composite.get-composite-hotel.title}",
        notes = "${api.hotel-composite.get-composite-hotel.description}")
    @ApiResponses(value = {
        @ApiResponse(code = 400, message = "Bad Request, invalid format of the request. See response message for more information."),
        @ApiResponse(code = 404, message = "Not found, the specified id does not exist."),
        @ApiResponse(code = 422, message = "Unprocessable entity, input parameters caused the processing to fails. See response message for more information.")
    })
    @GetMapping(
        value    = "/hotel-composite/{hotelId}",
        produces = "application/json")
    HotelAggregate getHotel(@PathVariable int hotelId);
}
