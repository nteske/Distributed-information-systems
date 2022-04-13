package microservices.core.review;

import org.junit.Test;
import org.mapstruct.factory.Mappers;
import com.example.api.core.review.Review;
import com.example.microservices.core.review.persistence.ReviewEntity;
import com.example.microservices.core.review.services.ReviewMapper;

import java.sql.Date;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

public class MapperTests {

    private ReviewMapper mapper = Mappers.getMapper(ReviewMapper.class);


    @Test
    public void mapperTests() {
        assertNotNull(mapper);

        Review api = new Review(1, 2, 3, "Great", Date.valueOf("2021-08-12"), "adr");

        ReviewEntity entity = mapper.apiToEntity(api);

        assertEquals(api.getHotelId(), entity.getHotelId());
        assertEquals(api.getReviewId(), entity.getReviewId());
        assertEquals(api.getRating(), entity.getRating());
        assertEquals(api.getDescription(), entity.getDescription());
        assertEquals(api.getCreatedOn(), entity.getCreatedOn());

        Review api2 = mapper.entityToApi(entity);
        
        assertEquals(api.getHotelId(), api2.getHotelId());
        assertEquals(api.getReviewId(), api2.getReviewId());
        assertEquals(api.getRating(), api2.getRating());
        assertEquals(api.getDescription(), api2.getDescription());
        assertEquals(api.getCreatedOn(), api2.getCreatedOn());
        assertNull(api2.getServiceAddress());
    }

    @Test
    public void mapperListTests() {

        assertNotNull(mapper);

        Review api = new Review(1, 2, 3, "Great", Date.valueOf("2021-08-12"), "adr");
        List<Review> apiList = Collections.singletonList(api);

        List<ReviewEntity> entityList = mapper.apiListToEntityList(apiList);
        assertEquals(apiList.size(), entityList.size());

        ReviewEntity entity = entityList.get(0);
        
        assertEquals(api.getHotelId(), entity.getHotelId());
        assertEquals(api.getReviewId(), entity.getReviewId());
        assertEquals(api.getRating(), entity.getRating());
        assertEquals(api.getDescription(), entity.getDescription());
        assertEquals(api.getCreatedOn(), entity.getCreatedOn());

        List<Review> api2List = mapper.entityListToApiList(entityList);
        assertEquals(apiList.size(), api2List.size());

        Review api2 = api2List.get(0);

        assertEquals(api.getHotelId(), api2.getHotelId());
        assertEquals(api.getReviewId(), api2.getReviewId());
        assertEquals(api.getRating(), api2.getRating());
        assertEquals(api.getDescription(), api2.getDescription());
        assertEquals(api.getCreatedOn(), api2.getCreatedOn());
        assertNull(api2.getServiceAddress());
    }
}