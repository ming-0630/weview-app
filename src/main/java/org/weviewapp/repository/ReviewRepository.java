package org.weviewapp.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.weviewapp.entity.Review;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReviewRepository extends JpaRepository<Review, UUID> {
    Page<Review> findByUserId(UUID userId, Pageable pageable);
    Page<Review> findByIsVerifiedIsTrueAndReportIsNullAndProduct_ProductId(UUID productId, Pageable pageable);
    Optional<List<Review>> findAllByIsVerifiedIsTrueAndReportIsNullAndProduct_ProductId(UUID productId, Sort sort);
    Page<Review> findAllByIsVerifiedIsTrueAndReportIsNullAndUserIdOrderByDateCreatedDesc(UUID userId, Pageable pageable);
    @Query(value = "SELECT r FROM Review r LEFT JOIN r.votes v WHERE r.product.productId = :productId " +
            "AND r.isVerified = true " +
            "AND r.report.id = null " +
             "GROUP BY r.id ORDER BY (SUM(CASE WHEN v.voteType = 'UPVOTE' THEN 1 ELSE 0 END) - " +
            "SUM(CASE WHEN v.voteType = 'DOWNVOTE' THEN 1 ELSE 0 END)) *" +
            "(CASE WHEN :sortDirection = 'ASC' THEN 1 ELSE -1 END) DESC",
            countQuery = "SELECT COUNT(DISTINCT r) FROM Review r LEFT JOIN r.votes v WHERE r.product.productId = :productId ORDER BY :sortDirection")
    Page<Review> findByProductIdSortedByVoteDifference(UUID productId,
                                               String sortDirection,
                                               Pageable pageable);
    @Query(value = "SELECT r FROM Review r LEFT JOIN r.votes v WHERE r.user.id = :userId " +
            "AND r.isVerified = true " +
            "AND r.report.id = null " +
            "GROUP BY r.id ORDER BY (SUM(CASE WHEN v.voteType = 'UPVOTE' THEN 1 ELSE 0 END) - " +
            "SUM(CASE WHEN v.voteType = 'DOWNVOTE' THEN 1 ELSE 0 END)) *" +
            "(CASE WHEN :sortDirection = 'ASC' THEN 1 ELSE -1 END) DESC",
            countQuery = "SELECT COUNT(DISTINCT r) FROM Review r LEFT JOIN r.votes v WHERE r.user.id = :userId ORDER BY :sortDirection")
    Page<Review> findByUserIdSortedByVoteDifference(UUID userId,
                                                       String sortDirection,
                                                       Pageable pageable);
    Optional<List<Review>> findByIsVerifiedIsTrueAndReportIsNullAndDateCreatedBeforeAndProduct_ProductIdOrderByDateCreated(LocalDateTime startDate, UUID product_id);
    Optional<List<Review>> findByIsVerifiedIsTrueAndReportIsNullAndDateCreatedBetweenAndProduct_ProductIdOrderByDateCreated(LocalDateTime startDate, LocalDateTime endDate, UUID product_id);
    Optional<Review> findFirstByIsVerifiedIsTrueAndReportIsNullAndProduct_ProductIdOrderByDateCreatedAsc(UUID product_id);
    Optional<Review> findFirstByIsVerifiedIsTrueAndReportIsNullAndProduct_ProductIdOrderByDateCreatedDesc(UUID product_id);
    @Query("SELECT r From Review r " +
            "WHERE (r.isVerified = false OR r.report.id IS NOT NULL) " +
            "AND r.user.id = :userId " +
            "AND r.product.productId = :productId ")
    Optional<Review> findFirstByIsVerifiedIsFalseOrReportIsNotNullAndUser_IdAndProduct_ProductId(UUID userId, UUID productId);
    Boolean existsByProduct_ProductIdAndUser_Id(UUID productId, UUID id);
}
