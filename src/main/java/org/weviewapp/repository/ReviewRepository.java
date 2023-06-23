package org.weviewapp.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.weviewapp.entity.Review;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReviewRepository extends JpaRepository<Review, UUID> {
    Boolean existsByUser_Id(UUID userId);
    Page<Review> findByUserId(UUID userId, Pageable pageable);
    Page<Review> findByProduct_ProductId(UUID productId, Pageable pageable);
    @Query(value = "SELECT r FROM Review r LEFT JOIN r.votes v WHERE r.product.productId = :productId " +
            "GROUP BY r.id ORDER BY (SUM(CASE WHEN v.voteType = 'UPVOTE' THEN 1 ELSE 0 END) - " +
            "SUM(CASE WHEN v.voteType = 'DOWNVOTE' THEN 1 ELSE 0 END)) *" +
            "(CASE WHEN :sortDirection = 'ASC' THEN 1 ELSE -1 END) DESC",
            countQuery = "SELECT COUNT(DISTINCT r) FROM Review r LEFT JOIN r.votes v WHERE r.product.productId = :productId ORDER BY :sortDirection")
    Page<Review> findByProductIdSortedByVoteDifference(UUID productId,
                                               String sortDirection,
                                               Pageable pageable);
    @Query(value = "SELECT r FROM Review r LEFT JOIN r.votes v WHERE r.user.id = :userId " +
            "GROUP BY r.id ORDER BY (SUM(CASE WHEN v.voteType = 'UPVOTE' THEN 1 ELSE 0 END) - " +
            "SUM(CASE WHEN v.voteType = 'DOWNVOTE' THEN 1 ELSE 0 END)) *" +
            "(CASE WHEN :sortDirection = 'ASC' THEN 1 ELSE -1 END) DESC",
            countQuery = "SELECT COUNT(DISTINCT r) FROM Review r LEFT JOIN r.votes v WHERE r.user.id = :userId ORDER BY :sortDirection")
    Page<Review> findByUserIdSortedByVoteDifference(UUID userId,
                                                       String sortDirection,
                                                       Pageable pageable);
    Optional<List<Review>> findByProduct_ProductIdAndDateCreatedBeforeOrderByDateCreated(UUID product_id, LocalDateTime startDate);
    Optional<List<Review>> findByProduct_ProductIdAndDateCreatedBetweenOrderByDateCreated(UUID product_id, LocalDateTime startDate, LocalDateTime endDate);
    Optional<Review> findFirstByProduct_ProductIdOrderByDateCreatedAsc(UUID product_id);
    Boolean existsByProduct_ProductIdAndUser_Id(UUID productId, UUID id);
}
