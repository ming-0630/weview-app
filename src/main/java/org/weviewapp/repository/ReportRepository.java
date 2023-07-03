package org.weviewapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.weviewapp.entity.Report;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReportRepository extends JpaRepository<Report, UUID> {
    Optional<Report> findByReporterIdAndReviewId(UUID userId, UUID reviewId);
    Optional<List<Report>> findAllByReviewId(UUID reviewId);

}
