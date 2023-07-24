package org.weviewapp.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.weviewapp.dto.ReviewDTO;
import org.weviewapp.entity.Review;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReviewService {
    List<ReviewDTO> mapToReviewDTO(List<Review> reviews);
    void deleteReview(UUID reviewId);
//    List<Review> getAllReviewsByUserId(UUID userId, Pageable pageable);
    List<Review> getAllReviewsByProductId(UUID productId);
    Page<Review> getReviewsByProductId(UUID productId, Pageable pageable);
    Page<Review> getReviewsByProductIdSortByVotes(UUID productId, String sortDirection, Pageable pageable);
    Page<Review> getReviewsByUserId(UUID userId, Pageable pageable);
    Page<Review> getReviewsByUserIdSortByVotes(UUID userId, String sortDirection, Pageable pageable);
    Optional<Review> getUnverifiedOrReportedReview(UUID userId, UUID productId);
    List<Number> getRatings(List<Review> reviews);
    LocalDateTime getLatestReviewDate(UUID productId);
    LocalDateTime getEarliestReviewDate(UUID productId);
    @Async
    void reviewVerify(List<String> imageBase64, Review review);
    Integer sentimentAPICheck(String desc) throws IOException;
    Review getRandomReview() throws IOException;
}
