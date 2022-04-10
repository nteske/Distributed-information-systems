package com.example.microservices.core.hotel.services;

import java.sql.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import com.example.api.core.hotel.*;
import com.example.util.exceptions.InvalidInputException;
import com.example.util.exceptions.NotFoundException;
import com.example.util.http.ServiceUtil;

@RestController
public class HotelServiceImpl implements HotelService {
    
    private static final Logger LOG = LoggerFactory.getLogger(HotelServiceImpl.class);

    private final ServiceUtil serviceUtil;

    @Autowired
    public HotelServiceImpl(ServiceUtil serviceUtil) {
        this.serviceUtil = serviceUtil;
    }

    @Override
    public Hotel getHotel(int hotelId) {
        LOG.debug("/hotel returns the found hotel for hotelId={}", hotelId);

        if (hotelId < 1) throw new InvalidInputException("Invalid hotelId: " + hotelId);

        if (hotelId == 13) throw new NotFoundException("No hotel found for hotelId: " + hotelId);
        
        return Hotel(hotelId, "Crystal Hotel", "Place where comfort meets luxury.", "https://placekitten.com/200/300", Date.valueOf("2021-08-13"), serviceUtil.getServiceAddress());
    }
}
