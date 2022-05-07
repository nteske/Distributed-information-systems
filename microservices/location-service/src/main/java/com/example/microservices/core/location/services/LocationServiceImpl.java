package com.example.microservices.core.location.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import com.example.microservices.core.location.persistence.LocationEntity;
import com.example.microservices.core.location.persistence.LocationRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import com.example.api.core.location.*;
import com.example.util.exceptions.InvalidInputException;
import com.example.util.http.ServiceUtil;


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
    	if (body.getHotelId() < 1) throw new InvalidInputException("Invalid hotelId: " + body.getHotelId());

        LocationEntity entity = mapper.apiToEntity(body);
        Mono<Location> newEntity = repository.save(entity)
            .log()
            .onErrorMap(
                DuplicateKeyException.class,
                ex -> new InvalidInputException("Duplicate key, Hotel Id: " + body.getHotelId() + ", Location Id:" + body.getLocationId()))
            .map(e -> mapper.entityToApi(e));
        return newEntity.block();
    }
    
    @Override
    public Flux<Location> getLocation(int hotelId) {
    	if (hotelId < 1) throw new InvalidInputException("Invalid hotelId: " + hotelId);
    	
    	return repository.findByHotelId(hotelId)
                .log()
                .map(e -> mapper.entityToApi(e))
                .map(e -> {e.setServiceAddress(serviceUtil.getServiceAddress()); return e;});
    }
    
    @Override
    public void deleteLocations(int hotelId) {
    	if (hotelId < 1) throw new InvalidInputException("Invalid hotelId: " + hotelId);
        LOG.debug("deleteLocation: tries to delete location for the hotel with hotelId: {}", hotelId);
        repository.deleteAll(repository.findByHotelId(hotelId)).block();
    }

}