package microservices.core.hotel;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit4.SpringRunner;
import com.example.microservices.core.hotel.persistence.HotelEntity;
import com.example.microservices.core.hotel.persistence.HotelRepository;

import java.sql.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.stream.IntStream.rangeClosed;
import static org.junit.Assert.*;
import static org.springframework.data.domain.Sort.Direction.ASC;

@RunWith(SpringRunner.class)
@DataMongoTest
public class PersistenceTests {

    @Autowired
    private HotelRepository repository;

    private HotelEntity savedEntity;

    @Before
   	public void setupDb() {
   		repository.deleteAll();

   		HotelEntity entity = new HotelEntity(1, "n","d","i", Date.valueOf("2021-08-12"));
        savedEntity = repository.save(entity);

        assertEqualsHotel(entity, savedEntity);
    }


    @Test
   	public void create() {
        HotelEntity newEntity = new HotelEntity(2, "n","d","i", Date.valueOf("2021-08-12"));
        repository.save(newEntity);

        HotelEntity foundEntity = repository.findById(newEntity.getId()).get();
        assertEqualsHotel(newEntity, foundEntity);

        assertEquals(2, repository.count());
    }

    @Test
   	public void update() {
        savedEntity.setTitle("n2");
        repository.save(savedEntity);

        HotelEntity foundEntity = repository.findById(savedEntity.getId()).get();
        assertEquals(1, (long)foundEntity.getVersion());
        assertEquals("n2", foundEntity.getTitle());
    }

    @Test
   	public void delete() {
        repository.delete(savedEntity);
        assertFalse(repository.existsById(savedEntity.getId()));
    }

    @Test
   	public void getByHotelId() {
        Optional<HotelEntity> entity = repository.findByHotelId(savedEntity.getHotelId());

        assertTrue(entity.isPresent());
        assertEqualsHotel(savedEntity, entity.get());
    }

    @Test(expected = DuplicateKeyException.class)
   	public void duplicateError() {
    	HotelEntity entity = new HotelEntity(savedEntity.getHotelId(),"n","d","i", Date.valueOf("2021-08-12"));
        repository.save(entity);
    }

    @Test
   	public void optimisticLockError() {
        // Store the saved entity in two separate entity objects
    	HotelEntity entity1 = repository.findById(savedEntity.getId()).get();
    	HotelEntity entity2 = repository.findById(savedEntity.getId()).get();

        // Update the entity using the first entity object
        entity1.setTitle("n1");
        repository.save(entity1);

        //  Update the entity using the second entity object.
        // This should fail since the second entity now holds a old version number, i.e. a Optimistic Lock Error
        try {
            entity2.setTitle("n2");
            repository.save(entity2);

            fail("Expected an OptimisticLockingFailureException");
        } catch (OptimisticLockingFailureException e) {}

        // Get the updated entity from the database and verify its new sate
        HotelEntity updatedEntity = repository.findById(savedEntity.getId()).get();
        assertEquals(1, (int)updatedEntity.getVersion());
        assertEquals("n1", updatedEntity.getTitle());
    }

    @Test
    public void paging() {
        repository.deleteAll();

        List<HotelEntity> newHotels = rangeClosed(1001, 1010)
            .mapToObj(i -> new HotelEntity(i, "name " + i,"d","i", Date.valueOf("2021-08-12")))
            .collect(Collectors.toList());
        repository.saveAll(newHotels);

        Pageable nextPage = PageRequest.of(0, 4, ASC, "hotelId");
        nextPage = testNextPage(nextPage, "[1001, 1002, 1003, 1004]", true);
        nextPage = testNextPage(nextPage, "[1005, 1006, 1007, 1008]", true);
        nextPage = testNextPage(nextPage, "[1009, 1010]", false);
    }

    private Pageable testNextPage(Pageable nextPage, String expectedHotelIds, boolean expectsNextPage) {
        Page<HotelEntity> hotelPage = repository.findAll(nextPage);
        assertEquals(expectedHotelIds, hotelPage.getContent().stream().map(p -> p.getHotelId()).collect(Collectors.toList()).toString());
        assertEquals(expectsNextPage, hotelPage.hasNext());
        return hotelPage.nextPageable();
    }

    private void assertEqualsHotel(HotelEntity expectedEntity, HotelEntity actualEntity) {
        assertEquals(expectedEntity.getId(), actualEntity.getId());
        assertEquals(expectedEntity.getVersion(), actualEntity.getVersion());
        assertEquals(expectedEntity.getHotelId(), actualEntity.getHotelId());
        assertEquals(expectedEntity.getTitle(), actualEntity.getTitle());
        assertEquals(expectedEntity.getDescription(), actualEntity.getDescription());
        assertEquals(expectedEntity.getImage(), actualEntity.getImage());
        assertEquals(expectedEntity.getCreatedOn(), actualEntity.getCreatedOn());
    }
}