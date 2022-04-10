package com.example.api.core.review;

import org.springframework.web.bind.annotation.*;

import java.util.List;

public interface ReviewService {

    /**
     * Sample usage:
     *
     * curl -X POST $HOST:$PORT/review \
     *   -H "Content-Type: application/json" --data \
     *   '{"hotelId":123,"reviewId":456,"rating":3,"description":"Description","createdOn":"2021-08-12"}'
     *
     * @param body
     * @return
     */
    @PostMapping(
        value    = "/review",
        consumes = "application/json",
        produces = "application/json")
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
    List<Review> getReviews(@RequestParam(value = "hotelId", required = true) int hotelId);

        /**
     * Sample usage:
     *
     * curl -X DELETE $HOST:$PORT/reviews?hotelId=1
     *
     * @param hotelId
     */
    @DeleteMapping(value = "/review")
    void deleteReviews(@RequestParam(value = "hotelId", required = true)  int hotelId);

}