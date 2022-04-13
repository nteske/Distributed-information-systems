package microservices.core.location;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.test.context.junit4.SpringRunner;
import com.example.microservices.core.location.persistence.*;

import java.sql.Date;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@DataMongoTest
public class PersistenceTests {

    @Autowired
    private LocationRepository repository;

    private LocationEntity savedEntity;

    @Before
   	public void setupDb() {
   		repository.deleteAll();

        LocationEntity entity = new LocationEntity(1, 2, "Country","Town","Address");

        savedEntity = repository.save(entity);

        assertEqualsLocation(entity, savedEntity);
    }


    @Test
   	public void create() {

    	LocationEntity newEntity = new LocationEntity(1, 2, "Country","Town","Address");
        repository.save(newEntity);

        LocationEntity foundEntity = repository.findById(newEntity.getId()).get();
        assertEqualsLocation(newEntity, foundEntity);

        assertEquals(2, repository.count());
    }

    @Test
   	public void update() {
        savedEntity.setCountry("a2");
        repository.save(savedEntity);

        LocationEntity foundEntity = repository.findById(savedEntity.getId()).get();
        assertEquals(1, (long)foundEntity.getVersion());
        assertEquals("a2", foundEntity.getCountry());
    }

    @Test
   	public void delete() {
        repository.delete(savedEntity);
        assertFalse(repository.existsById(savedEntity.getId()));
    }

    @Test
   	public void getByHotelId() {
        List<LocationEntity> entityList = repository.findByHotelId(savedEntity.getHotelId());

        assertThat(entityList, hasSize(1));
        assertEqualsLocation(savedEntity, entityList.get(0));
    }

    @Test(expected = DuplicateKeyException.class)
   	public void duplicateError() {
    	LocationEntity entity = new LocationEntity(1, 2, "Country","Town","Address");
        repository.save(entity);
    }

    @Test
   	public void optimisticLockError() {

        // Store the saved entity in two separate entity objects
    	LocationEntity entity1 = repository.findById(savedEntity.getId()).get();
    	LocationEntity entity2 = repository.findById(savedEntity.getId()).get();

        // Update the entity using the first entity object
        entity1.setCountry("a1");
        repository.save(entity1);

        //  Update the entity using the second entity object.
        // This should fail since the second entity now holds a old version number, i.e. a Optimistic Lock Error
        try {
            entity2.setCountry("a2");
            repository.save(entity2);

            fail("Expected an OptimisticLockingFailureException");
        } catch (OptimisticLockingFailureException e) {}

        // Get the updated entity from the database and verify its new sate
        LocationEntity updatedEntity = repository.findById(savedEntity.getId()).get();
        assertEquals(1, (int)updatedEntity.getVersion());
        assertEquals("a1", updatedEntity.getCountry());
    }

    private void assertEqualsLocation(LocationEntity expectedEntity, LocationEntity actualEntity) {
        assertEquals(expectedEntity.getId(),               actualEntity.getId());
        assertEquals(expectedEntity.getVersion(),          actualEntity.getVersion());
        assertEquals(expectedEntity.getHotelId(),        actualEntity.getHotelId());
        assertEquals(expectedEntity.getLocationId(), actualEntity.getLocationId());
        assertEquals(expectedEntity.getCountry(),           actualEntity.getCountry());
        assertEquals(expectedEntity.getTown(),           actualEntity.getTown());
        assertEquals(expectedEntity.getAddress(),          actualEntity.getAddress());
    }
}