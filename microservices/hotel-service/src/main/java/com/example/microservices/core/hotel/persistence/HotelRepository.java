package com.example.microservices.core.hotel.persistence;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface HotelRepository extends ReactiveCrudRepository<HotelEntity, String> {
    Mono<HotelEntity> findByHotelId(int hotelId);
}