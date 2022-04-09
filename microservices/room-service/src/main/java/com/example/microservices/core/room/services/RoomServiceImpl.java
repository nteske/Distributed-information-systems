package com.example.microservices.core.room.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import api.core.room.*;
import com.example.util.exceptions.InvalidInputException;
import com.example.util.http.ServiceUtil;

import java.util.ArrayList;
import java.util.List;

@RestController
public class RoomServiceImpl implements RoomService {

    private static final Logger LOG = LoggerFactory.getLogger(RoomServiceImpl.class);

    private final ServiceUtil serviceUtil;

    @Autowired
    public RoomServiceImpl(ServiceUtil serviceUtil) {
        this.serviceUtil = serviceUtil;
    }

    @Override
    public List<Room> getRoom(int hotelId) {

        if (hotelId < 1) throw new InvalidInputException("Invalid hotelId: " + hotelId);

        if (hotelId == 113) {
            LOG.debug("No location found for hotelId: {}", hotelId);
            return  new ArrayList<>();
        }

        List<Room> list = new ArrayList<>();
        list.add(new Room(hotelId, 1, 26, 3, 220, serviceUtil.getServiceAddress()));
        list.add(new Room(hotelId, 1, 27, 1, 250, serviceUtil.getServiceAddress()));
        list.add(new Room(hotelId, 1, 28, 2, 240, serviceUtil.getServiceAddress()));

        LOG.debug("/room response size: {}", list.size());

        return list;
    }
}