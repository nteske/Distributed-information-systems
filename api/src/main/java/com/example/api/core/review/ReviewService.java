package com.example.api.core.review;

import org.springframework.web.bind.annotation.*;

import reactor.core.publisher.Flux;

public interface ReviewService {

    Review createReview(@RequestBody Review body);

    /**
     * Sample usage: curl $HOST:$PORT/reviews?hotelId=1
     *
     * @param hotelId
     * @return
     */
    @GetMapping(
        value    = "/review",
        produces = "application/json")
    Flux<Review> getReviews(@RequestParam(value = "hotelId", required = true) int hotelId);

    void deleteReviews(@RequestParam(value = "hotelId", required = true)  int hotelId);

}