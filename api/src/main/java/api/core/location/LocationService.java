package api.core.location;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

public interface LocationService {

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
}