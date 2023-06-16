package org.weviewapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.weviewapp.entity.Vote;
import org.weviewapp.enums.VoteType;

import java.util.Optional;
import java.util.UUID;

public interface VoteRepository extends JpaRepository<Vote, UUID> {
    public Optional<Vote> findByReview_IdAndUserId(UUID reviewId, UUID userId);
    public Optional<Vote> findByComment_IdAndUserId(UUID commentId, UUID userId);
    public int countByReviewIdAndVoteType(UUID reviewId, VoteType type);
    public int countByCommentIdAndVoteType(UUID commentId, VoteType type);
}
