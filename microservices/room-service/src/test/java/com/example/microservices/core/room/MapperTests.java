package com.example.microservices.core.room;

import org.junit.Test;
import org.mapstruct.factory.Mappers;
import com.example.api.core.room.Room;
import com.example.microservices.core.room.persistence.RoomEntity;
import com.example.microservices.core.room.services.RoomMapper;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

public class MapperTests {

    private RoomMapper mapper = Mappers.getMapper(RoomMapper.class);

    @Test
    public void mapperTests() {
        assertNotNull(mapper);

        Room api = new Room(1, 2, 2521,3,300, "adr");

        RoomEntity entity = mapper.apiToEntity(api);

        assertEquals(api.getHotelId(), entity.getHotelId());
        assertEquals(api.getRoomId(), entity.getRoomId());
        assertEquals(api.getRoomNumber(), entity.getRoomNumber());
        assertEquals(api.getBeds(), entity.getBeds());
        assertEquals(api.getPrice(), entity.getPrice(),0.0012f);

        Room api2 = mapper.entityToApi(entity);
        
        assertEquals(api.getHotelId(), api2.getHotelId());
        assertEquals(api.getRoomId(), api2.getRoomId());
        assertEquals(api.getRoomNumber(), api2.getRoomNumber());
        assertEquals(api.getBeds(), api2.getBeds());
        assertEquals(api.getPrice(), entity.getPrice(),0.0012f);
        assertNull(api2.getServiceAddress());
    }

    @Test
    public void mapperListTests() {
        assertNotNull(mapper);

        Room api = new Room(1, 2,2521,3,300, "adr");
        List<Room> apiList = Collections.singletonList(api);

        List<RoomEntity> entityList = mapper.apiListToEntityList(apiList);
        assertEquals(apiList.size(), entityList.size());

        RoomEntity entity = entityList.get(0);

        assertEquals(api.getHotelId(), entity.getHotelId());
        assertEquals(api.getRoomId(), entity.getRoomId());
        assertEquals(api.getRoomNumber(), entity.getRoomNumber());
        assertEquals(api.getBeds(), entity.getBeds());
        assertEquals(api.getPrice(), entity.getPrice(),0.0012f);

        List<Room> api2List = mapper.entityListToApiList(entityList);
        assertEquals(apiList.size(), api2List.size());

        Room api2 = api2List.get(0);

        assertEquals(api.getHotelId(), api2.getHotelId());
        assertEquals(api.getRoomId(), api2.getRoomId());
        assertEquals(api.getRoomNumber(), api2.getRoomNumber());
        assertEquals(api.getBeds(), api2.getBeds());
        assertEquals(api.getPrice(), entity.getPrice(), 0.0012f);
        assertNull(api2.getServiceAddress());
    }
}