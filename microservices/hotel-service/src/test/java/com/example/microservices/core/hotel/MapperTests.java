package com.example.microservices.core.hotel;

import org.junit.Test;
import org.mapstruct.factory.Mappers;
import com.example.api.core.hotel.Hotel;
import com.example.microservices.core.hotel.persistence.HotelEntity;
import com.example.microservices.core.hotel.services.HotelMapper;

import static org.junit.Assert.*;

import java.util.Date;
public class MapperTests {
    private HotelMapper mapper = Mappers.getMapper(HotelMapper.class);

    @Test
    public void mapperTests() {

        assertNotNull(mapper);

        Hotel api = new Hotel(1, "Title","Description","Image", new Date(), "sa");

        HotelEntity entity = mapper.apiToEntity(api);

        assertEquals(api.getHotelId(), entity.getHotelId());
        assertEquals(api.getTitle(), entity.getTitle());
        assertEquals(api.getDescription(), entity.getDescription());
        assertEquals(api.getImage(), entity.getImage());

        Hotel api2 = mapper.entityToApi(entity);

        assertEquals(api.getHotelId(), api2.getHotelId());
        assertEquals(api.getTitle(), api2.getTitle());
        assertEquals(api.getDescription(), api2.getDescription());
        assertEquals(api.getImage(), api2.getImage());
        assertNull(api2.getServiceAddress());
    }
}
