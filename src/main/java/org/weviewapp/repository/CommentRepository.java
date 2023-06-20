package org.weviewapp.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.weviewapp.entity.Comment;

import java.util.UUID;

public interface CommentRepository extends JpaRepository<Comment, UUID> {
    Page<Comment> findByReviewIdOrderByDateCreatedDesc(UUID reviewId, Pageable pageable);
    Page<Comment> findByUserId(UUID userId, Pageable pageable);
    Integer countByReviewId(UUID reviewId);
}
