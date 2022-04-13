package com.example.microservices.core.room.services;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import com.example.api.core.room.Room;
import com.example.microservices.core.room.persistence.RoomEntity;

import java.util.List;

@Mapper(componentModel = "spring")
public interface RoomMapper {

    @Mappings({
        @Mapping(target = "serviceAddress", ignore = true)
    })
    Room entityToApi(RoomEntity entity);

    @Mappings({
        @Mapping(target = "id", ignore = true),
        @Mapping(target = "version", ignore = true)
    })
    RoomEntity apiToEntity(Room api);

    List<Room> entityListToApiList(List<RoomEntity> entity);
    List<RoomEntity> apiListToEntityList(List<Room> api);
}