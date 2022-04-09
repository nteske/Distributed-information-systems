package api.core.room;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

public interface RoomService {

    /**
     * Sample usage: curl $HOST:$PORT/room?roomId=1
     *
     * @param roomId
     * @return
     */
    @GetMapping(
        value    = "/room",
        produces = "application/json")
    List<Room> getRoom(@RequestParam(value = "roomId", required = true) int roomId);
}
