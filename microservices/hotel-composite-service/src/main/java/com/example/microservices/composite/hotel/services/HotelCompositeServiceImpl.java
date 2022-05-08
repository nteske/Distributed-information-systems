package com.example.microservices.composite.hotel.services;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import com.example.api.composite.hotel.*;
import com.example.api.core.room.Room;
import com.example.api.core.hotel.Hotel;
import com.example.api.core.review.Review;
import com.example.api.core.location.Location;
import com.example.util.exceptions.NotFoundException;
import com.example.util.http.ServiceUtil;

import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class HotelCompositeServiceImpl implements HotelCompositeService {

    private static final Logger LOG = LoggerFactory.getLogger(HotelCompositeServiceImpl.class);
	
    private final SecurityContext nullSC = new SecurityContextImpl();

	private final ServiceUtil serviceUtil;
    private final HotelCompositeIntegration integration;

    @Autowired
    public HotelCompositeServiceImpl(ServiceUtil serviceUtil, HotelCompositeIntegration integration) {
        this.serviceUtil = serviceUtil;
        this.integration = integration;
    }
    
    @Override
    public Mono<Void> createCompositeHotel(HotelAggregate body) {
        return ReactiveSecurityContextHolder.getContext().doOnSuccess(sc -> internalCreateCompositeHotel(sc, body)).then();
    }

    public void internalCreateCompositeHotel(SecurityContext sc, HotelAggregate body) {
        try {
            logAuthorizationInfo(sc);

            LOG.debug("createCompositeHotel: creates a new composite entity for hotelId: {}", body.getHotelId());

            Hotel hotel = new Hotel(body.getHotelId(), body.getTitle(), body.getDescription(), body.getImage(),
            						body.getCreatedOn(), null);
            integration.createHotel(hotel);

            if (body.getLocation() != null) {
                body.getLocation().forEach(r -> {
                    Location location = new Location(body.getHotelId(), r.getLocationId(), r.getCountry(), r.getTown(), r.getAddress(), null);
                    integration.createLocation(location);
                });
            }

            if (body.getReviews() != null) {
                body.getReviews().forEach(r -> {
                    Review review = new Review(body.getHotelId(), r.getReviewId(), r.getRating(), r.getDescription(),
                    						   r.getCreatedOn(), null);
                    integration.createReview(review);
                });
            }
            
            if (body.getRoom() != null) {
                body.getRoom().forEach(r -> {
                    Room room = new Room(body.getHotelId(), r.getRoomId(), r.getRoomNumber(), r.getBeds(), r.getPrice(), null);
                    integration.createRoom(room);
                });
            }

            LOG.debug("createCompositeHotel: composite entities created for hotelId: {}", body.getHotelId());

        } catch (RuntimeException re) {
            LOG.warn("createCompositeHotel failed: {}", re.toString());
            throw re;
        }
    }
    
    @Override
    public Mono<HotelAggregate> getCompositeHotel(int hotelId, int delay, int faultPercent) {
        return Mono.zip(
        		values -> createHotelAggregate((SecurityContext) values[0], (Hotel) values[1], (List<Location>) values[2], (List<Review>) values[3], (List<Room>) values[4], serviceUtil.getServiceAddress()),
                ReactiveSecurityContextHolder.getContext().defaultIfEmpty(nullSC),
                integration.getHotel(hotelId, delay, faultPercent)
                	.onErrorReturn(CallNotPermittedException.class, getHotelFallbackValue(hotelId)),
                integration.getLocation(hotelId).collectList(),
                integration.getReviews(hotelId).collectList(),
                integration.getRoom(hotelId).collectList())
            .doOnError(ex -> LOG.warn("getCompositeHotel failed: {}", ex.toString()))
            .log();
    }
    
    @Override
    public Mono<Void> deleteCompositeHotel(int hotelId) {
        return ReactiveSecurityContextHolder.getContext().doOnSuccess(sc -> internalDeleteCompositeHotel(sc, hotelId)).then();
    }

    private void internalDeleteCompositeHotel(SecurityContext sc, int hotelId) {
    	try {
            logAuthorizationInfo(sc);
            LOG.debug("deleteCompositeHotel: Deletes a hotel aggregate for hotelId: {}", hotelId);

            integration.deleteHotel(hotelId);
            integration.deleteLocations(hotelId);
            integration.deleteReviews(hotelId);
            integration.deleteRooms(hotelId);

            LOG.debug("deleteCompositeHotel: aggregate entities deleted for hotelId: {}", hotelId);

        } catch (RuntimeException re) {
            LOG.warn("deleteCompositeHotel failed: {}", re.toString());
            throw re;
        }
    }
	
    private Hotel getHotelFallbackValue(int hotelId) {

        LOG.warn("Creating a fallback hotel for hotelId = {}", hotelId);

        if (hotelId == 13) {
            String errMsg = "Hotel Id: " + hotelId + " not found in fallback cache!";
            LOG.warn(errMsg);
            throw new NotFoundException(errMsg);
            }
        return new Hotel(hotelId, "Fallback hotel" + hotelId, "Description","image",new Date(), serviceUtil.getServiceAddress());
    }
    
	private HotelAggregate createHotelAggregate(SecurityContext sc, Hotel hotel, List<Location> location, List<Review> reviews, List<Room> rooms, String serviceAddress) {
		logAuthorizationInfo(sc);
        // 1. Setup hotel info
        int hotelId = hotel.getHotelId();
        String title = hotel.getTitle();
        String description = hotel.getDescription();
        String image = hotel.getImage();
        Date createdOn = hotel.getCreatedOn();

        // 2. Copy summary location info, if available
        List<LocationSummary> locationSummaries = (location == null) ? null :
        	location.stream()
                .map(r -> new LocationSummary(r.getLocationId(), r.getCountry(), r.getTown(), r.getAddress()))
                .collect(Collectors.toList());

        // 3. Copy summary review info, if available
        List<ReviewSummary> reviewSummaries = (reviews == null)  ? null :
            reviews.stream()
                .map(r -> new ReviewSummary(r.getReviewId(), r.getRating(), r.getDescription(), r.getCreatedOn()))
                .collect(Collectors.toList());
        
        // 4. Copy summary rooms info, if available
        List<RoomSummary> roomSummaries = (rooms == null)  ? null :
        	rooms.stream()
                .map(r -> new RoomSummary(r.getRoomId(), r.getRoomNumber(), r.getBeds(), r.getPrice()))
                .collect(Collectors.toList());

        // 5. Create info regarding the involved microservices addresses
        String hotelAddress = hotel.getServiceAddress();
        String reviewAddress = (reviews != null && reviews.size() > 0) ? reviews.get(0).getServiceAddress() : "";
        String locationAddress = (location != null && location.size() > 0) ? location.get(0).getServiceAddress() : "";
        String roomAddress = (rooms != null && rooms.size() > 0) ? rooms.get(0).getServiceAddress() : "";
        ServiceAddresses serviceAddresses = new ServiceAddresses(serviceAddress, hotelAddress, reviewAddress, locationAddress, roomAddress);

        return new HotelAggregate(hotelId, title, description, image, createdOn,
        						  locationSummaries, reviewSummaries, roomSummaries, serviceAddresses);
    }

	private void logAuthorizationInfo(SecurityContext sc) {
        if (sc != null && sc.getAuthentication() != null && sc.getAuthentication() instanceof JwtAuthenticationToken) {
            Jwt jwtToken = ((JwtAuthenticationToken)sc.getAuthentication()).getToken();
            logAuthorizationInfo(jwtToken);
        } else {
            LOG.warn("No JWT based Authentication supplied, running tests are we?");
        }
    }

    private void logAuthorizationInfo(Jwt jwt) {
        if (jwt == null) {
            LOG.warn("No JWT supplied, running tests are we?");
        } else {
            if (LOG.isDebugEnabled()) {
                URL issuer = jwt.getIssuer();
                List<String> audience = jwt.getAudience();
                Object subject = jwt.getClaims().get("sub");
                Object scopes = jwt.getClaims().get("scope");
                Object expires = jwt.getClaims().get("exp");

                LOG.debug("Authorization info: Subject: {}, scopes: {}, expires {}: issuer: {}, audience: {}", subject, scopes, expires, issuer, audience);
            }
        }
    }
}