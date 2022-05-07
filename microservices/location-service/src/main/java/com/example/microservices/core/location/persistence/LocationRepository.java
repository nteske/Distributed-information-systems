package com.example.microservices.core.location.persistence;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface LocationRepository extends ReactiveCrudRepository<LocationEntity, String> {
    Flux<LocationEntity> findByHotelId(int hotelId);
}