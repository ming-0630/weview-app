package org.weviewapp.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.weviewapp.entity.Review;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReviewRepository extends JpaRepository<Review, UUID> {
    Boolean existsByUser_Id(UUID userId);
    Page<Review> findByUserId(UUID userId, Pageable pageable);
    Optional<List<Review>> findByProduct_ProductIdAndDateCreatedBeforeOrderByDateCreated(UUID product_id, LocalDateTime startDate);
    Optional<List<Review>> findByProduct_ProductIdAndDateCreatedBetweenOrderByDateCreated(UUID product_id, LocalDateTime startDate, LocalDateTime endDate);
    Optional<Review> findFirstByProduct_ProductIdOrderByDateCreatedAsc(UUID product_id);
    Boolean existsByProduct_ProductIdAndUser_Id(UUID productId, UUID id);
}
