package api.composite.hotel;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

public interface HotelCompositeService {

    /**
     * Sample usage: curl $HOST:$PORT/hotel-composite/1
     *
     * @param hotelId
     * @return the composite hotel info, if found, else null
     */
    @GetMapping(
        value    = "/hotel-composite/{hotelId}",
        produces = "application/json")
    HotelAggregate getHotel(@PathVariable int hotelId);
}
