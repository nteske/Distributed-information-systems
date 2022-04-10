package com.example.api.core.room;

import org.springframework.web.bind.annotation.*;

import java.util.List;

public interface RoomService {

	/**
     * Sample usage:
     *
     * curl -X POST $HOST:$PORT/room \
     *   -H "Content-Type: application/json" --data \
     *   '{"hotelId":123,"roomId":456,"roomNumber":3,"beds":1,"price":223}'
     *
     * @param body
     * @return
     */
    @PostMapping(
        value    = "/room",
        consumes = "application/json",
        produces = "application/json")
    Room createRoom(@RequestBody Room body);
	
    /**
     * Sample usage: curl $HOST:$PORT/room?hotelId=1
     *
     * @param roomId
     * @return
     */
    @GetMapping(
        value    = "/room",
        produces = "application/json")
    List<Room> getRoom(@RequestParam(value = "hotelId", required = true) int hotelId);
        
    /**
     * Sample usage:
     *
     * curl -X DELETE $HOST:$PORT/room?hotelId=1
     *
     * @param hotelId
     */
    @DeleteMapping(value = "/room")
    void deleteRooms(@RequestParam(value = "hotelId", required = true)  int hotelId);

}
