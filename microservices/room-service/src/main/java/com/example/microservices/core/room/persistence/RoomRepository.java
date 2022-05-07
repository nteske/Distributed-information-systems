package com.example.microservices.core.room.persistence;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface RoomRepository extends ReactiveCrudRepository<RoomEntity, String> {
    Flux<RoomEntity> findByHotelId(int hotelId);
}