package com.example.microservices.core.room.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.RestController;
import com.example.microservices.core.room.persistence.RoomEntity;
import com.example.microservices.core.room.persistence.RoomRepository;

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
        try {
        	RoomEntity entity = mapper.apiToEntity(body);
        	RoomEntity newEntity = repository.save(entity);

            LOG.debug("createRoom: created a room entity: {}/{}", body.getHotelId(), body.getRoomId());
            return mapper.entityToApi(newEntity);

        } catch (DuplicateKeyException dke) {
            throw new InvalidInputException("Duplicate key, Hotel Id: " + body.getHotelId() + ", Room Id:" + body.getRoomId());
        }
    }

    @Override
    public List<Room> getRoom(int hotelId) {

        if (hotelId < 1) throw new InvalidInputException("Invalid hotelId: " + hotelId);

        List<RoomEntity> entityList = repository.findByHotelId(hotelId);
        List<Room> list = mapper.entityListToApiList(entityList);
        list.forEach(e -> e.setServiceAddress(serviceUtil.getServiceAddress()));

        LOG.debug("getRooms: response size: {}", list.size());

        return list;
    }

    @Override
    public void deleteRooms(int hotelId) {
        LOG.debug("deleteRooms: tries to delete room for the hotel with hotelId: {}", hotelId);
        repository.deleteAll(repository.findByHotelId(hotelId));
    }
}