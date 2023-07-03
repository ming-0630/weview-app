package org.weviewapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.weviewapp.entity.ReportReason;

import java.util.List;
import java.util.Optional;

public interface ReportReasonRepository extends JpaRepository<ReportReason, Long> {
    Optional<List<ReportReason>> findByNameIn(List<String> name);
}
