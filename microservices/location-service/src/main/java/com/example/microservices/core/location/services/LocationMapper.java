package com.example.microservices.core.location.services;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import com.example.api.core.location.Location;
import com.example.microservices.core.location.persistence.LocationEntity;

import java.util.List;

@Mapper(componentModel = "spring")
public interface LocationMapper {

    @Mappings({
        @Mapping(target = "serviceAddress", ignore = true)
    })
    Location entityToApi(LocationEntity entity);

    @Mappings({
        @Mapping(target = "id", ignore = true),
        @Mapping(target = "version", ignore = true)
    })
    LocationEntity apiToEntity(Location api);

    List<Location> entityListToApiList(List<LocationEntity> entity);
    List<LocationEntity> apiListToEntityList(List<Location> api);
}