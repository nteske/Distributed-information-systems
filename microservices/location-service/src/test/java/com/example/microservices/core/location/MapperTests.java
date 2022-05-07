package com.example.microservices.core.location;

import org.junit.Test;
import org.mapstruct.factory.Mappers;
import com.example.api.core.location.Location;
import com.example.microservices.core.location.persistence.LocationEntity;
import com.example.microservices.core.location.services.LocationMapper;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

public class MapperTests {

    private LocationMapper mapper = Mappers.getMapper(LocationMapper.class);

    @Test
    public void mapperTests() {

        assertNotNull(mapper);

        Location api = new Location(1, 2, "Country","Town","Address", "adr");

        LocationEntity entity = mapper.apiToEntity(api);

        assertEquals(api.getHotelId(), entity.getHotelId());
        assertEquals(api.getLocationId(), entity.getLocationId());
        assertEquals(api.getCountry(), entity.getCountry());
        assertEquals(api.getTown(), entity.getTown());
        assertEquals(api.getAddress(), entity.getAddress());

        Location api2 = mapper.entityToApi(entity);

        assertEquals(api.getHotelId(), api2.getHotelId());
        assertEquals(api.getLocationId(), api2.getLocationId());
        assertEquals(api.getCountry(), api2.getCountry());
        assertEquals(api.getTown(), api2.getTown());
        assertEquals(api.getAddress(), api2.getAddress());
        assertNull(api2.getServiceAddress());
    }

    @Test
    public void mapperListTests() {

        assertNotNull(mapper);

        Location api = new Location(1, 2, "Country","Town","Address", "adr");

        List<Location> apiList = Collections.singletonList(api);

        List<LocationEntity> entityList = mapper.apiListToEntityList(apiList);
        assertEquals(apiList.size(), entityList.size());

        LocationEntity entity = entityList.get(0);

        assertEquals(api.getHotelId(), entity.getHotelId());
        assertEquals(api.getLocationId(), entity.getLocationId());
        assertEquals(api.getCountry(), entity.getCountry());
        assertEquals(api.getTown(), entity.getTown());
        assertEquals(api.getAddress(), entity.getAddress());

        List<Location> api2List = mapper.entityListToApiList(entityList);
        assertEquals(apiList.size(), api2List.size());

        Location api2 = api2List.get(0);

        assertEquals(api.getHotelId(), api2.getHotelId());
        assertEquals(api.getLocationId(), api2.getLocationId());
        assertEquals(api.getCountry(), api2.getCountry());
        assertEquals(api.getTown(), api2.getTown());
        assertEquals(api.getAddress(), api2.getAddress());
        assertNull(api2.getServiceAddress());
    }
}