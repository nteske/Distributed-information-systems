package com.example.microservices.core.hotel.persistence;

import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.Optional;

public interface HotelRepository extends PagingAndSortingRepository<HotelEntity, String> {
    Optional<HotelEntity> findByHotelId(int hotelId);
}