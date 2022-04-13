package com.example.microservices.core.room.persistence;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface RoomRepository extends CrudRepository<RoomEntity, String> {
    List<RoomEntity> findByHotelId(int hotelId);
}