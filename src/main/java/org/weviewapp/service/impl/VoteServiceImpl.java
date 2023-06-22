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
import org.weviewapp.service.UserService;
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
    @Autowired
    private UserService userService;

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

        if (comment.getUser().getId().equals(user.getId())) {
            throw new WeviewAPIException(HttpStatus.FORBIDDEN, "Cannot vote on own comment!");
        }

        Optional<Vote> existingVote = voteRepository.findByComment_IdAndUserId(comment.getId(), user.getId());
        if (existingVote.isPresent()) {
            if (existingVote.get().getVoteType().equals(voteType)) {
                voteRepository.delete(existingVote.get());

                // Point change to author
                if (voteType.equals(VoteType.DOWNVOTE)) {
                    // REMOVE DOWNVOTE
                    userService.modifyPoints(comment.getUser().getId(), 2);
                } else if (voteType.equals(VoteType.UPVOTE)) {
                    //REMOVE UPVOTE
                    userService.modifyPoints(comment.getUser().getId(), -2);
                }

                // Point change to current user
                // Removing vote, so remove points
                userService.modifyPoints(user.getId(), -1);
                return null;
            } else {
                existingVote.get().setVoteType(voteType);
                Vote vote = voteRepository.save(existingVote.get());

                // Inverting votes
                if (voteType.equals(VoteType.DOWNVOTE)) {
                    // REMOVE UPVOTE ADD DOWNVOTE
                    userService.modifyPoints(comment.getUser().getId(), -4);
                } else if (voteType.equals(VoteType.UPVOTE)) {
                    // REMOVE DOWNVOTE ADD UPVOTE
                    userService.modifyPoints(comment.getUser().getId(), +4);
                }

                // Invert points so no changes in current user points
                return vote;
            }
        } else {
            Vote vote = new Vote();
            vote.setId(UUID.randomUUID());
            vote.setComment(comment);
            vote.setVoteType(voteType);
            vote.setUser(user);
            Vote newVote = voteRepository.save(vote);

            if (voteType.equals(VoteType.DOWNVOTE)) {
                // ADD DOWNVOTE
                userService.modifyPoints(comment.getUser().getId(), -2);
            } else if (voteType.equals(VoteType.UPVOTE)) {
                // ADD UPVOTE
                userService.modifyPoints(comment.getUser().getId(), +2);
            }

            // +1 for action
            userService.modifyPoints(user.getId(), 1);
            return newVote;
        }
    }

    private Vote voteOnReview(UUID reviewId, User user, VoteType voteType) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new WeviewAPIException(HttpStatus.BAD_REQUEST, "Review not found!"));

        if (review.getUser().getId().equals(user.getId())) {
            throw new WeviewAPIException(HttpStatus.FORBIDDEN, "Cannot vote on own review!");
        }

        Optional<Vote> existingVote = voteRepository.findByReview_IdAndUserId(review.getId(), user.getId());
        if (existingVote.isPresent()) {
            if (existingVote.get().getVoteType().equals(voteType)) {
                voteRepository.delete(existingVote.get());
                if (voteType.equals(VoteType.DOWNVOTE)) {
                    // REMOVE DOWNVOTE
                    userService.modifyPoints(review.getUser().getId(), 4);
                } else if (voteType.equals(VoteType.UPVOTE)) {
                    //REMOVE UPVOTE
                    userService.modifyPoints(review.getUser().getId(), -4);
                }

                // Point change to current user
                // Removing vote, so remove points
                userService.modifyPoints(user.getId(), -1);
                return null;
            } else {
                existingVote.get().setVoteType(voteType);
                Vote vote = voteRepository.save(existingVote.get());

                // Inverting votes
                if (voteType.equals(VoteType.DOWNVOTE)) {
                    // REMOVE UPVOTE ADD DOWNVOTE
                    userService.modifyPoints(review.getUser().getId(), -8);
                } else if (voteType.equals(VoteType.UPVOTE)) {
                    // REMOVE DOWNVOTE ADD UPVOTE
                    userService.modifyPoints(review.getUser().getId(), +8);
                }
                // Invert points so no changes in current user points
                return vote;
            }
        } else {
            Vote vote = new Vote();
            vote.setId(UUID.randomUUID());
            vote.setReview(review);
            vote.setVoteType(voteType);
            vote.setUser(user);

            Vote newVote = voteRepository.save(vote);
            if (voteType.equals(VoteType.DOWNVOTE)) {
                // ADD DOWNVOTE
                userService.modifyPoints(review.getUser().getId(), -4);
            } else if (voteType.equals(VoteType.UPVOTE)) {
                // ADD UPVOTE
                userService.modifyPoints(review.getUser().getId(), 4);
            }

            // +1 for action
            userService.modifyPoints(user.getId(), 1);
            return newVote;
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
