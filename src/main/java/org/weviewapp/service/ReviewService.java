package org.weviewapp.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.weviewapp.dto.ReviewDTO;
import org.weviewapp.entity.Review;

import java.util.List;
import java.util.UUID;

public interface ReviewService {
    public List<ReviewDTO> mapToReviewDTO(List<Review> reviews);
    public void deleteReview(UUID reviewId);
    public Page<Review> getReviewsByProductId(UUID productId, Pageable pageable);
    public Page<Review> getReviewsByProductIdSortByVotes(UUID productId, String sortDirection, Pageable pageable);
    public Page<Review> getReviewsByUserId(UUID userId, Pageable pageable);
    public Page<Review> getReviewsByUserIdSortByVotes(UUID userId, String sortDirection, Pageable pageable);
}
