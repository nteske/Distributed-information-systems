package com.example.microservices.core.location.persistence;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface LocationRepository extends CrudRepository<LocationEntity, String> {
    List<LocationEntity> findByHotelId(int hotelId);
}