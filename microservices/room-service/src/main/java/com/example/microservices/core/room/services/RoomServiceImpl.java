package com.example.microservices.core.room.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.RestController;
import com.example.microservices.core.room.persistence.RoomEntity;
import com.example.microservices.core.room.persistence.RoomRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import com.example.api.core.room.*;
import com.example.util.exceptions.*;
import com.example.util.http.*;

import java.util.List;

@RestController
public class RoomServiceImpl implements RoomService {

    private static final Logger LOG = LoggerFactory.getLogger(RoomServiceImpl.class);

    private final ServiceUtil serviceUtil;
    
    private final RoomRepository repository;

    private final RoomMapper mapper;

    @Autowired
    public RoomServiceImpl(RoomRepository repository, RoomMapper mapper, ServiceUtil serviceUtil) {
        this.repository = repository;
        this.mapper = mapper;
        this.serviceUtil = serviceUtil;
    }

    @Override
    public Room createRoom(Room body) {
    	if (body.getHotelId() < 1) throw new InvalidInputException("Invalid hotelId: " + body.getHotelId());

        RoomEntity entity = mapper.apiToEntity(body);
        Mono<Room> newEntity = repository.save(entity)
            .log()
            .onErrorMap(
                DuplicateKeyException.class,
                ex -> new InvalidInputException("Duplicate key, Hotel Id: " + body.getHotelId() + ", room Id:" + body.getRoomId()))
            .map(e -> mapper.entityToApi(e));
        return newEntity.block();
    }

    @Override
    public Flux<Room> getRoom(int hotelId) {

        if (hotelId < 1) throw new InvalidInputException("Invalid hotelId: " + hotelId);

        return repository.findByHotelId(hotelId)
                .log()
                .map(e -> mapper.entityToApi(e))
                .map(e -> {e.setServiceAddress(serviceUtil.getServiceAddress()); return e;});
    }

    @Override
    public void deleteRooms(int hotelId) {
        if (hotelId < 1) throw new InvalidInputException("Invalid hotelId: " + hotelId);
        LOG.debug("deleteRooms: tries to delete room for the hotel with hotelId: {}", hotelId);
        repository.deleteAll(repository.findByHotelId(hotelId)).block();
    }
}