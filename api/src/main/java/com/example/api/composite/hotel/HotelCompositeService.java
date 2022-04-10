package com.example.api.composite.hotel;

import org.springframework.web.bind.annotation.*;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;


@Api(description = "REST API for composite hotel information.")

public interface HotelCompositeService {
    /**
     * Sample usage:
     *
     * curl -X POST $HOST:$PORT/hotel-composite \
     *   -H "Content-Type: application/json" --data \
     *   '{"hotelId":123,"title":"Some title","description":"Description","image":"http://www.image.com","createdOn":"2021-08-12"}'
     *
     * @param body
     */
    @ApiOperation(
        value = "${api.hotel-composite.create-composite-hotel.description}",
        notes = "${api.hotel-composite.create-composite-hotel.notes}")
    @ApiResponses(value = {
        @ApiResponse(code = 400, message = "Bad Request, invalid format of the request. See response message for more information."),
        @ApiResponse(code = 422, message = "Unprocessable entity, input parameters caused the processing to fail. See response message for more information.")
    })
    @PostMapping(
        value    = "/hotel-composite",
        consumes = "application/json")
    void createCompositeHotel(@RequestBody HotelAggregate body);
	
	
    /**
     * Sample usage: curl $HOST:$PORT/hotel-composite/1
     *
     * @param hotelId
     * @return the composite hotel info, if found, else null
     */
    @ApiOperation(
        value = "${api.hotel-composite.get-composite-hotel.description}",
        notes = "${api.hotel-composite.get-composite-hotel.notes}")
    @ApiResponses(value = {
        @ApiResponse(code = 400, message = "Bad Request, invalid format of the request. See response message for more information."),
        @ApiResponse(code = 404, message = "Not found, the specified id does not exist."),
        @ApiResponse(code = 422, message = "Unprocessable entity, input parameters caused the processing to fail. See response message for more information.")
    })
    @GetMapping(
        value    = "/hotel-composite/{hotelId}",
        produces = "application/json")
    HotelAggregate getCompositeHotel(@PathVariable int hotelId);


    /**
     * Sample usage:
     *
     * curl -X DELETE $HOST:$PORT/hotel-composite/1
     *
     * @param hotelId
     */
    @ApiOperation(
        value = "${api.hotel-composite.delete-composite-hotel.description}",
        notes = "${api.hotel-composite.delete-composite-hotel.notes}")
    @ApiResponses(value = {
        @ApiResponse(code = 400, message = "Bad Request, invalid format of the request. See response message for more information."),
        @ApiResponse(code = 422, message = "Unprocessable entity, input parameters caused the processing to fail. See response message for more information.")
    })
    @DeleteMapping(value = "/hotel-composite/{hotelId}")
    void deleteCompositeHotel(@PathVariable int hotelId);
}
