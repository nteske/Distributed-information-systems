package com.example.microservices.core.hotel;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.OptimisticLockingFailureException;
import reactor.test.StepVerifier;
import org.springframework.test.context.junit4.SpringRunner;
import com.example.microservices.core.hotel.persistence.HotelEntity;
import com.example.microservices.core.hotel.persistence.HotelRepository;

import java.util.Date;

@RunWith(SpringRunner.class)
@DataMongoTest(properties = {"spring.cloud.config.enabled=false"})
public class PersistenceTests {

    @Autowired
    private HotelRepository repository;

    private HotelEntity savedEntity;

    @Before
   	public void setupDb() {
    	StepVerifier.create(repository.deleteAll()).verifyComplete();

   		HotelEntity entity = new HotelEntity(1, "n","d","i", new Date());
		StepVerifier.create(repository.save(entity))
			.expectNextMatches(createdEntity -> {
				savedEntity = createdEntity;
				return areHotelEqual(entity, savedEntity);
			})
			.verifyComplete();
    }


    @Test
   	public void create() {
        HotelEntity newEntity = new HotelEntity(2, "n","d","i", new Date());
        StepVerifier.create(repository.save(newEntity))
        .expectNextMatches(createdEntity -> newEntity.getHotelId() == createdEntity.getHotelId())
        .verifyComplete();
        StepVerifier.create(repository.findById(newEntity.getId()))
        .expectNextMatches(foundEntity -> areHotelEqual(newEntity, foundEntity))
        .verifyComplete();

        StepVerifier.create(repository.count()).expectNext(2l).verifyComplete();
    }

    @Test
   	public void update() {
        savedEntity.setTitle("n2");
        StepVerifier.create(repository.save(savedEntity))
	        .expectNextMatches(updatedEntity -> updatedEntity.getTitle().equals("n2"))
	        .verifyComplete();

		StepVerifier.create(repository.findById(savedEntity.getId()))
		    .expectNextMatches(foundEntity ->
		        foundEntity.getVersion() == 1 &&
		        foundEntity.getTitle().equals("n2"))
		    .verifyComplete();
    }

    @Test
   	public void delete() {
    	StepVerifier.create(repository.delete(savedEntity)).verifyComplete();
        StepVerifier.create(repository.existsById(savedEntity.getId())).expectNext(false).verifyComplete();
    }

    @Test
   	public void getByHotelId() {
    	StepVerifier.create(repository.findByHotelId(savedEntity.getHotelId()))
	        .expectNextMatches(foundEntity -> areHotelEqual(savedEntity, foundEntity))
	        .verifyComplete();
    }

    @Test
   	public void duplicateError() {
    	HotelEntity entity = new HotelEntity(savedEntity.getHotelId(),"n","d","i", new Date());
    	StepVerifier.create(repository.save(entity)).expectError(DuplicateKeyException.class).verify();
    }

    @Test
   	public void optimisticLockError() {
        // Store the saved entity in two separate entity objects
    	HotelEntity entity1 = repository.findById(savedEntity.getId()).block();
    	HotelEntity entity2 = repository.findById(savedEntity.getId()).block();

        // Update the entity using the first entity object
        entity1.setTitle("n1");
        repository.save(entity1).block();

        //  Update the entity using the second entity object.
        // This should fail since the second entity now holds a old version number, i.e. a Optimistic Lock Error
        StepVerifier.create(repository.save(entity2)).expectError(OptimisticLockingFailureException.class).verify();

        // Get the updated entity from the database and verify its new sate
        StepVerifier.create(repository.findById(savedEntity.getId()))
	        .expectNextMatches(foundEntity ->
	            foundEntity.getVersion() == 1 &&
	            foundEntity.getTitle().equals("n1"))
	        .verifyComplete();
    }

    private boolean areHotelEqual(HotelEntity expectedEntity, HotelEntity actualEntity) {
        return
            (expectedEntity.getId().equals(actualEntity.getId())) &&
            (expectedEntity.getVersion() == actualEntity.getVersion()) &&
            (expectedEntity.getHotelId() == actualEntity.getHotelId()) &&
            (expectedEntity.getTitle().equals(actualEntity.getTitle())) &&
            (expectedEntity.getDescription().equals(actualEntity.getDescription())) &&
            (expectedEntity.getImage().equals(actualEntity.getImage())) &&
            (expectedEntity.getCreatedOn().equals(actualEntity.getCreatedOn()));
    }
}