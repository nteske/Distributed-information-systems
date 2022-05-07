package com.example.microservices.core.review;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import com.example.microservices.core.review.persistence.ReviewEntity;
import com.example.microservices.core.review.persistence.ReviewRepository;

import java.util.Date;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.*;
import static org.springframework.transaction.annotation.Propagation.NOT_SUPPORTED;

@RunWith(SpringRunner.class)
@DataJpaTest
@Transactional(propagation = NOT_SUPPORTED)
public class PersistenceTests {

    @Autowired
    private ReviewRepository repository;

    private ReviewEntity savedEntity;

    @Before
   	public void setupDb() {
   		repository.deleteAll();

        ReviewEntity entity = new ReviewEntity(1, 2, 3, "Great", new Date());
        savedEntity = repository.save(entity);

        assertEqualsReview(entity, savedEntity);
    }


    @Test
   	public void create() {

        ReviewEntity newEntity = new ReviewEntity(1, 3, 3, "Great", new Date());
        repository.save(newEntity);

        ReviewEntity foundEntity = repository.findById(newEntity.getId()).get();
        assertEqualsReview(newEntity, foundEntity);

        assertEquals(2, repository.count());
    }

    @Test
   	public void update() {
        savedEntity.setDescription("a2");
        repository.save(savedEntity);

        ReviewEntity foundEntity = repository.findById(savedEntity.getId()).get();
        assertEquals(1, (long)foundEntity.getVersion());
        assertEquals("a2", foundEntity.getDescription());
    }

    @Test
   	public void delete() {
        repository.delete(savedEntity);
        assertFalse(repository.existsById(savedEntity.getId()));
    }

    @Test
   	public void getByHotelId() {
        List<ReviewEntity> entityList = repository.findByHotelId(savedEntity.getHotelId());

        assertThat(entityList, hasSize(1));
        assertEqualsReview(savedEntity, entityList.get(0));
    }

    @Test(expected = DataIntegrityViolationException.class)
   	public void duplicateError() {
        ReviewEntity entity = new ReviewEntity(1, 2, 3, "Great", new Date());
        repository.save(entity);
    }

    @Test
   	public void optimisticLockError() {

        // Store the saved entity in two separate entity objects
        ReviewEntity entity1 = repository.findById(savedEntity.getId()).get();
        ReviewEntity entity2 = repository.findById(savedEntity.getId()).get();

        // Update the entity using the first entity object
        entity1.setDescription("a1");
        repository.save(entity1);

        //  Update the entity using the second entity object.
        // This should fail since the second entity now holds a old version number, i.e. a Optimistic Lock Error
        try {
            entity2.setDescription("a2");
            repository.save(entity2);

            fail("Expected an OptimisticLockingFailureException");
        } catch (OptimisticLockingFailureException e) {}

        // Get the updated entity from the database and verify its new sate
        ReviewEntity updatedEntity = repository.findById(savedEntity.getId()).get();
        assertEquals(1, (int)updatedEntity.getVersion());
        assertEquals("a1", updatedEntity.getDescription());
    }

    private void assertEqualsReview(ReviewEntity expectedEntity, ReviewEntity actualEntity) {
        assertEquals(expectedEntity.getId(),        actualEntity.getId());
        assertEquals(expectedEntity.getVersion(),   actualEntity.getVersion());
        assertEquals(expectedEntity.getHotelId(), actualEntity.getHotelId());
        assertEquals(expectedEntity.getReviewId(), actualEntity.getReviewId());
        assertEquals(expectedEntity.getRating(), actualEntity.getRating());
        assertEquals(expectedEntity.getDescription(), actualEntity.getDescription());
        assertEquals(expectedEntity.getCreatedOn(), actualEntity.getCreatedOn());
    }
}