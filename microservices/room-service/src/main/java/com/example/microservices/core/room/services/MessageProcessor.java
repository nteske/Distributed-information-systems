package com.example.microservices.core.room.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.messaging.Sink;
import com.example.api.core.room.Room;
import com.example.api.core.room.RoomService;
import com.example.api.event.Event;
import com.example.util.exceptions.EventProcessingException;

@EnableBinding(Sink.class)
public class MessageProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(MessageProcessor.class);

    private final RoomService roomService;

    @Autowired
    public MessageProcessor(RoomService roomService) {
        this.roomService = roomService;
    }

    @StreamListener(target = Sink.INPUT)
    public void process(Event<Integer, Room> event) {

        LOG.info("Process message created at {}...", event.getEventCreatedAt());

        switch (event.getEventType()) {

        case CREATE:
        	Room room = event.getData();
            LOG.info("Create room with ID: {}/{}", room.getHotelId(), room.getRoomId());
            roomService.createRoom(room);
            break;

        case DELETE:
            int hotelId = event.getKey();
            LOG.info("Delete room with HotelID: {}", hotelId);
            roomService.deleteRooms(hotelId);
            break;

        default:
            String errorMessage = "Incorrect event type: " + event.getEventType() + ", expected a CREATE or DELETE event";
            LOG.warn(errorMessage);
            throw new EventProcessingException(errorMessage);
        }

        LOG.info("Message processing done!");
    }
}