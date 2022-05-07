package com.example.microservices.core.hotel.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.messaging.Sink;
import com.example.api.core.hotel.Hotel;
import com.example.api.core.hotel.HotelService;
import com.example.api.event.Event;
import com.example.util.exceptions.EventProcessingException;

@EnableBinding(Sink.class)
public class MessageProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(MessageProcessor.class);

    private final HotelService hotelService;

    @Autowired
    public MessageProcessor(HotelService hotelService) {
        this.hotelService = hotelService;
    }

    @StreamListener(target = Sink.INPUT)
    public void process(Event<Integer, Hotel> event) {

        LOG.info("Process message created at {}...", event.getEventCreatedAt());

        switch (event.getEventType()) {

        case CREATE:
            Hotel hotel = event.getData();
            LOG.info("Create hotel with ID: {}", hotel.getHotelId());
            hotelService.createHotel(hotel);
            break;

        case DELETE:
            int hotelId = event.getKey();
            LOG.info("Delete location with HotelID: {}", hotelId);
            hotelService.deleteHotel(hotelId);
            break;

        default:
            String errorMessage = "Incorrect event type: " + event.getEventType() + ", expected a CREATE or DELETE event";
            LOG.warn(errorMessage);
            throw new EventProcessingException(errorMessage);
        }

        LOG.info("Message processing done!");
    }
}