package com.example.microservices.core.hotel.services;

import java.sql.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.RestController;
import com.example.api.core.hotel.*;
import com.example.util.exceptions.InvalidInputException;
import com.example.util.exceptions.NotFoundException;
import com.example.util.http.ServiceUtil;

import com.example.microservices.core.hotel.persistence.HotelEntity;
import com.example.microservices.core.hotel.persistence.HotelRepository;
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
        try {
            HotelEntity entity = mapper.apiToEntity(body);
            HotelEntity newEntity = repository.save(entity);

            LOG.debug("createHotel: entity created for hotelId: {}", body.getHotelId());
            return mapper.entityToApi(newEntity);

        } catch (DuplicateKeyException dke) {
            throw new InvalidInputException("Duplicate key, Hotel Id: " + body.getHotelId());
        }
    }

    @Override
    public Hotel getHotel(int hotelId) {

        if (hotelId < 1) throw new InvalidInputException("Invalid hotelId: " + hotelId);

        HotelEntity entity = repository.findByHotelId(hotelId)
        .orElseThrow(() -> new NotFoundException("No hotel found for hotelId: " + hotelId));

        Hotel response = mapper.entityToApi(entity);
        response.setServiceAddress(serviceUtil.getServiceAddress());

        LOG.debug("getHotel: found hotelId: {}", response.getHotelId());

        return response;
    }

    @Override
    public void deleteHotel(int hotelId) {
        LOG.debug("deleteHotel: tries to delete an entity with hotelId: {}", hotelId);
        repository.findByHotelId(hotelId).ifPresent(e -> repository.delete(e));
    }
}
