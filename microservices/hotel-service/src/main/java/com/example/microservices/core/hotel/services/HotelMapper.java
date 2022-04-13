package com.example.microservices.core.hotel.services;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import com.example.api.core.hotel.Hotel;
import com.example.microservices.core.hotel.persistence.HotelEntity;

@Mapper(componentModel = "spring")
public interface HotelMapper {

    @Mappings({
        @Mapping(target = "serviceAddress", ignore = true)
    })
    Hotel entityToApi(HotelEntity entity);

    @Mappings({
        @Mapping(target = "id", ignore = true),
        @Mapping(target = "version", ignore = true)
    })
    HotelEntity apiToEntity(Hotel api);
}