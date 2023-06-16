package org.weviewapp.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.weviewapp.entity.Comment;
import org.weviewapp.entity.Review;
import org.weviewapp.entity.User;
import org.weviewapp.entity.Vote;
import org.weviewapp.enums.VoteOn;
import org.weviewapp.enums.VoteType;
import org.weviewapp.exception.WeviewAPIException;
import org.weviewapp.repository.CommentRepository;
import org.weviewapp.repository.ReviewRepository;
import org.weviewapp.repository.UserRepository;
import org.weviewapp.repository.VoteRepository;
import org.weviewapp.service.VoteService;

import java.util.Optional;
import java.util.UUID;

@Service
public class VoteServiceImpl implements VoteService {
    @Autowired
    private VoteRepository voteRepository;
    @Autowired
    private ReviewRepository reviewRepository;
    @Autowired
    private CommentRepository commentRepository;
    @Autowired
    private UserRepository userRepository;

    public Vote vote(VoteOn voteOn, UUID id, UUID userId, VoteType voteType) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new WeviewAPIException(HttpStatus.BAD_REQUEST, "User not found!"));

        if (voteOn.equals(VoteOn.COMMENT)) {
            return voteOnComment(id, user, voteType);
        } else if (voteOn.equals(VoteOn.REVIEW)) {
            return voteOnReview(id, user, voteType);
        }

        // Handle the case when voteOn is neither COMMENT nor REVIEW
        throw new WeviewAPIException(HttpStatus.BAD_REQUEST, "Invalid voteOn value!");
    }

    private Vote voteOnComment(UUID commentId, User user, VoteType voteType) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new WeviewAPIException(HttpStatus.BAD_REQUEST, "Comment not found!"));

        Optional<Vote> existingVote = voteRepository.findByComment_IdAndUserId(comment.getId(), user.getId());
        if (existingVote.isPresent()) {
            if (existingVote.get().getVoteType().equals(voteType)) {
                voteRepository.delete(existingVote.get());
                return null;
            } else {
                existingVote.get().setVoteType(voteType);
                return voteRepository.save(existingVote.get());
            }
        } else {
            Vote vote = new Vote();
            vote.setId(UUID.randomUUID());
            vote.setComment(comment);
            vote.setVoteType(voteType);
            vote.setUser(user);
            return voteRepository.save(vote);
        }
    }

    private Vote voteOnReview(UUID reviewId, User user, VoteType voteType) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new WeviewAPIException(HttpStatus.BAD_REQUEST, "Review not found!"));

        Optional<Vote> existingVote = voteRepository.findByReview_IdAndUserId(review.getId(), user.getId());
        if (existingVote.isPresent()) {
            if (existingVote.get().getVoteType().equals(voteType)) {
                voteRepository.delete(existingVote.get());
                return null;
            } else {
                existingVote.get().setVoteType(voteType);
                return voteRepository.save(existingVote.get());
            }
        } else {
            Vote vote = new Vote();
            vote.setId(UUID.randomUUID());
            vote.setReview(review);
            vote.setVoteType(voteType);
            vote.setUser(user);
            return voteRepository.save(vote);
        }
    }

    public VoteType getCurrentUserVote(VoteOn voteOn, UUID id, UUID userId) {
        Optional<Vote> vote;
        if(voteOn.equals(VoteOn.COMMENT)) {
            vote = voteRepository.findByComment_IdAndUserId(id, userId);
        } else {
            vote = voteRepository.findByReview_IdAndUserId(id, userId);
        }

        if (!vote.isEmpty()) {
            return vote.get().getVoteType();
        } else {
            return null;
        }
    }

    public int getTotalUpvotes(VoteOn voteOn, UUID id) {
        if (voteOn.equals(VoteOn.COMMENT)) {
            return voteRepository.countByCommentIdAndVoteType(id, VoteType.UPVOTE);
        }

        if (voteOn.equals(VoteOn.REVIEW)) {
            return voteRepository.countByReviewIdAndVoteType(id, VoteType.UPVOTE);
        }
        return 0;
    }

    public int getTotalDownvotes(VoteOn voteOn, UUID id){
        if (voteOn.equals(VoteOn.COMMENT)) {
            return voteRepository.countByCommentIdAndVoteType(id, VoteType.DOWNVOTE);
        }

        if (voteOn.equals(VoteOn.REVIEW)) {
            return voteRepository.countByReviewIdAndVoteType(id, VoteType.DOWNVOTE);
        }

        return 0;
    }

}
