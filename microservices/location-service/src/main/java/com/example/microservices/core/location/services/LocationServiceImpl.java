package com.example.microservices.core.location.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import com.example.microservices.core.location.persistence.LocationEntity;
import com.example.microservices.core.location.persistence.LocationRepository;

import com.example.api.core.location.*;
import com.example.util.exceptions.InvalidInputException;
import com.example.util.http.ServiceUtil;

import java.util.List;

@RestController
public class LocationServiceImpl implements LocationService {

    private static final Logger LOG = LoggerFactory.getLogger(LocationServiceImpl.class);
    
    private final LocationRepository repository;

    private final LocationMapper mapper;

    private final ServiceUtil serviceUtil;

    @Autowired
    public LocationServiceImpl(LocationRepository repository, LocationMapper mapper, ServiceUtil serviceUtil) {
        this.repository = repository;
        this.mapper = mapper;
        this.serviceUtil = serviceUtil;
    }

    @Override
    public Location createLocation(Location body) {
        try {
            LocationEntity entity = mapper.apiToEntity(body);
            LocationEntity newEntity = repository.save(entity);
            LOG.debug("createLocation: created a location entity: {}/{}", body.getHotelId(), body.getLocationId());
            return mapper.entityToApi(newEntity);
        } catch (DuplicateKeyException dke) {
            throw new InvalidInputException("Duplicate key, Hotel Id: " + body.getHotelId() + ", Location Id:" + body.getLocationId());
        }
    }
    
    @Override
    public List<Location> getLocation(int hotelId) {
    	if (hotelId < 1) throw new InvalidInputException("Invalid hotelId: " + hotelId);
    	
        List<LocationEntity> entityList = repository.findByHotelId(hotelId);
        List<Location> list = mapper.entityListToApiList(entityList);
        list.forEach(e -> e.setServiceAddress(serviceUtil.getServiceAddress()));

        LOG.debug("getLocation: response size: {}", list.size());

        return list;
    }
    
    @Override
    public void deleteLocations(int hotelId) {
        LOG.debug("deleteLocation: tries to delete location for the hotel with hotelId: {}", hotelId);
        repository.deleteAll(repository.findByHotelId(hotelId));
    }

}