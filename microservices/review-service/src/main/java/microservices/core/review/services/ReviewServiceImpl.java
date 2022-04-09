package microservices.core.review.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import api.core.review.*;
import util.exceptions.InvalidInputException;
import util.http.ServiceUtil;

import java.util.ArrayList;
import java.util.List;

@RestController
public class ReviewServiceImpl implements ReviewService {

    private static final Logger LOG = LoggerFactory.getLogger(ReviewServiceImpl.class);

    private final ServiceUtil serviceUtil;

    @Autowired
    public ReviewServiceImpl(ServiceUtil serviceUtil) {
        this.serviceUtil = serviceUtil;
    }

    @Override
    public List<Review> getReview(int hotelId) {

        if (hotelId < 1) throw new InvalidInputException("Invalid hitelId: " + hotelId);

        if (hotelId == 113) {
            LOG.debug("No review found for hitelId: {}", hotelId);
            return  new ArrayList<>();
        }

        List<Review> list = new ArrayList<>();
        list.add(new Review(hotelId, 1 ,5 , "Great!", Date.valueOf("2021-10-13"), serviceUtil.getServiceAddress()));
        list.add(new Review(hotelId, 1 ,4 , "good", Date.valueOf("2021-11-13"), serviceUtil.getServiceAddress()));

        LOG.debug("/review response size: {}", list.size());

        return list;
    }
}