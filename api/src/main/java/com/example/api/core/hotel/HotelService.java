package api.core.hotel;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

public interface HotelService {

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
}
