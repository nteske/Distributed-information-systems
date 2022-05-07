package com.example.microservices.core.room;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.test.context.junit4.SpringRunner;
import com.example.microservices.core.room.persistence.RoomEntity;
import com.example.microservices.core.room.persistence.RoomRepository;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@DataMongoTest
public class PersistenceTests {

    @Autowired
    private RoomRepository repository;

    private RoomEntity savedEntity;

    @Before
   	public void setupDb() {
   		repository.deleteAll().block();

   		RoomEntity entity = new RoomEntity(1, 2, 315,32,233);
        savedEntity = repository.save(entity).block();

        assertEqualsRecommendation(entity, savedEntity);
    }


    @Test
   	public void create() {

    	RoomEntity newEntity = new RoomEntity(1, 3, 315,32,233);
        repository.save(newEntity).block();

        RoomEntity foundEntity = repository.findById(newEntity.getId()).block();
        assertEqualsRecommendation(newEntity, foundEntity);

        assertEquals(2, (long)repository.count().block());
    }

    @Test
   	public void update() {
        savedEntity.setBeds(5);
        repository.save(savedEntity).block();

        RoomEntity foundEntity = repository.findById(savedEntity.getId()).block();
        assertEquals(1, (long)foundEntity.getVersion());
        assertEquals(5, foundEntity.getBeds());
    }

    @Test
   	public void delete() {
        repository.delete(savedEntity).block();
        assertFalse(repository.existsById(savedEntity.getId()).block());
    }

    @Test
   	public void getByHotelId() {
        List<RoomEntity> entityList = repository.findByHotelId(savedEntity.getHotelId()).collectList().block();

        assertThat(entityList, hasSize(1));
        assertEqualsRecommendation(savedEntity, entityList.get(0));
    }

    @Test(expected = DuplicateKeyException.class)
   	public void duplicateError() {
    	RoomEntity entity = new RoomEntity(1, 2, 315,32,233);
        repository.save(entity).block();
    }

    @Test
   	public void optimisticLockError() {

        // Store the saved entity in two separate entity objects
    	RoomEntity entity1 = repository.findById(savedEntity.getId()).block();
    	RoomEntity entity2 = repository.findById(savedEntity.getId()).block();

        // Update the entity using the first entity object
        entity1.setBeds(19);
        repository.save(entity1).block();

        //  Update the entity using the second entity object.
        // This should fail since the second entity now holds a old version number, i.e. a Optimistic Lock Error
        try {
            entity2.setBeds(33);
            repository.save(entity2).block();

            fail("Expected an OptimisticLockingFailureException");
        } catch (OptimisticLockingFailureException e) {}

        // Get the updated entity from the database and verify its new sate
        RoomEntity updatedEntity = repository.findById(savedEntity.getId()).block();
        assertEquals(1, (int)updatedEntity.getVersion());
        assertEquals(19, updatedEntity.getBeds());
    }

    private void assertEqualsRecommendation(RoomEntity expectedEntity, RoomEntity actualEntity) {
        assertEquals(expectedEntity.getId(),               actualEntity.getId());
        assertEquals(expectedEntity.getVersion(),          actualEntity.getVersion());
        assertEquals(expectedEntity.getHotelId(),        actualEntity.getHotelId());
        assertEquals(expectedEntity.getRoomId(), actualEntity.getRoomId());
        assertEquals(expectedEntity.getRoomNumber(),          actualEntity.getRoomNumber());
        assertEquals(expectedEntity.getBeds(),           actualEntity.getBeds());
        assertEquals(expectedEntity.getPrice(),           actualEntity.getPrice(), 0.0012f);
    }
}