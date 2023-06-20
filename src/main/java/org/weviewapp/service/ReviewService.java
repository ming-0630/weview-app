package org.weviewapp.service;

import org.weviewapp.dto.ReviewDTO;
import org.weviewapp.entity.Review;

import java.util.List;
import java.util.UUID;

public interface ReviewService {
    List<ReviewDTO> mapToReviewDTO(List<Review> reviews);
    void deleteReview(UUID reviewId);
}
