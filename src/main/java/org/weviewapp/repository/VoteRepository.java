package org.weviewapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.weviewapp.entity.Vote;
import org.weviewapp.enums.VoteType;

import java.util.Optional;
import java.util.UUID;

public interface VoteRepository extends JpaRepository<Vote, UUID> {
    Optional<Vote> findByReview_IdAndUserId(UUID reviewId, UUID userId);
    Optional<Vote> findByComment_IdAndUserId(UUID commentId, UUID userId);
    int countByReviewIdAndVoteType(UUID reviewId, VoteType type);
    int countByCommentIdAndVoteType(UUID commentId, VoteType type);
    int countByReview_UserIdAndVoteType(UUID userId, VoteType type);
}
