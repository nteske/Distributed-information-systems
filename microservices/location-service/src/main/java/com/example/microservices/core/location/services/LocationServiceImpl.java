package com.example.microservices.core.location.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import com.example.api.core.location.*;
import com.example.util.exceptions.InvalidInputException;
import com.example.util.http.ServiceUtil;

import java.util.ArrayList;
import java.util.List;

@RestController
public class LocationServiceImpl implements LocationService {

    private static final Logger LOG = LoggerFactory.getLogger(LocationServiceImpl.class);

    private final ServiceUtil serviceUtil;

    @Autowired
    public LocationServiceImpl(ServiceUtil serviceUtil) {
        this.serviceUtil = serviceUtil;
    }

    @Override
    public List<Location> getLocation(int hotelId) {

        if (hotelId < 1) throw new InvalidInputException("Invalid hitelId: " + hotelId);

        if (hotelId == 113) {
            LOG.debug("No location found for hitelId: {}", hotelId);
            return  new ArrayList<>();
        }

        List<Location> list = new ArrayList<>();
        list.add(new Location(hotelId, 1 , "Serbia", "Belgrade", "Internacionalnih Brigada 9", serviceUtil.getServiceAddress()));
        list.add(new Location(hotelId, 2 , "Serbia", "Novi Sad", "Test BB", serviceUtil.getServiceAddress()));
        list.add(new Location(hotelId, 3 , "Serbia", "Nis", "Nova Adresa 2", serviceUtil.getServiceAddress()));

        LOG.debug("/location response size: {}", list.size());

        return list;
    }
}