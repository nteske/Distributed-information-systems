package com.example.microservices.core.location.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.messaging.Sink;
import com.example.api.core.location.Location;
import com.example.api.core.location.LocationService;
import com.example.api.event.Event;
import com.example.util.exceptions.EventProcessingException;

@EnableBinding(Sink.class)
public class MessageProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(MessageProcessor.class);

    private final LocationService locationService;

    @Autowired
    public MessageProcessor(LocationService locationService) {
        this.locationService = locationService;
    }

    @StreamListener(target = Sink.INPUT)
    public void process(Event<Integer, Location> event) {

        LOG.info("Process message created at {}...", event.getEventCreatedAt());

        switch (event.getEventType()) {

        case CREATE:
        	Location location = event.getData();
            LOG.info("Create location with ID: {}/{}", location.getHotelId(), location.getLocationId());
            locationService.createLocation(location);
            break;

        case DELETE:
            int hotelId = event.getKey();
            LOG.info("Delete location with HotelId: {}", hotelId);
            locationService.deleteLocations(hotelId);
            break;

        default:
            String errorMessage = "Incorrect event type: " + event.getEventType() + ", expected a CREATE or DELETE event";
            LOG.warn(errorMessage);
            throw new EventProcessingException(errorMessage);
        }

        LOG.info("Message processing done!");
    }
}