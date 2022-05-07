package com.example.microservices.core.hotel.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import com.example.api.core.hotel.*;
import com.example.util.exceptions.InvalidInputException;
import com.example.util.exceptions.NotFoundException;
import com.example.util.http.ServiceUtil;

import com.example.microservices.core.hotel.persistence.HotelEntity;
import com.example.microservices.core.hotel.persistence.HotelRepository;

import static reactor.core.publisher.Mono.error;

@RestController
public class HotelServiceImpl implements HotelService {

    private static final Logger LOG = LoggerFactory.getLogger(HotelServiceImpl.class);

    private final ServiceUtil serviceUtil;
    
    private final HotelRepository repository;

    private final HotelMapper mapper;

    @Autowired
    public HotelServiceImpl(HotelRepository repository, HotelMapper mapper, ServiceUtil serviceUtil) {
        this.repository = repository;
        this.mapper = mapper;
        this.serviceUtil = serviceUtil;
    }
    
    @Override
    public Hotel createHotel(Hotel body) {
    	if (body.getHotelId() < 1) throw new InvalidInputException("Invalid hotelId: " + body.getHotelId());

        HotelEntity entity = mapper.apiToEntity(body);
        Mono<Hotel> newEntity = repository.save(entity)
            .log()
            .onErrorMap(
                DuplicateKeyException.class,
                ex -> new InvalidInputException("Duplicate key, Hotel Id: " + body.getHotelId()))
            .map(e -> mapper.entityToApi(e));
        return newEntity.block();
    }

    @Override
    public Mono<Hotel> getHotel(int hotelId) {
        if (hotelId < 1) throw new InvalidInputException("Invalid hotelId: " + hotelId);

        return repository.findByHotelId(hotelId)
                .switchIfEmpty(error(new NotFoundException("No hotel found for hotelId: " + hotelId)))
                .log()
                .map(e -> mapper.entityToApi(e))
                .map(e -> {e.setServiceAddress(serviceUtil.getServiceAddress()); return e;});
    }
    
    @Override
    public void deleteHotel(int hotelId) {
    	if (hotelId < 1) throw new InvalidInputException("Invalid hotelId: " + hotelId);
        LOG.debug("deleteHotel: tries to delete an entity with hotelId: {}", hotelId);
        repository.findByHotelId(hotelId).log().map(e -> repository.delete(e)).flatMap(e -> e).block();
    }
}