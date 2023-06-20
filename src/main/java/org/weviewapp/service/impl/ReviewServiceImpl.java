package org.weviewapp.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.weviewapp.dto.ReviewDTO;
import org.weviewapp.dto.UserDTO;
import org.weviewapp.entity.Review;
import org.weviewapp.entity.ReviewImage;
import org.weviewapp.entity.User;
import org.weviewapp.enums.VoteOn;
import org.weviewapp.enums.VoteType;
import org.weviewapp.exception.WeviewAPIException;
import org.weviewapp.repository.CommentRepository;
import org.weviewapp.repository.ReviewRepository;
import org.weviewapp.repository.UserRepository;
import org.weviewapp.service.ReviewService;
import org.weviewapp.service.VoteService;
import org.weviewapp.utils.ImageUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ReviewServiceImpl implements ReviewService {
    @Autowired
    VoteService voteService;
    @Autowired
    CommentRepository commentRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    ReviewRepository reviewRepository;
    @Override
    public List<ReviewDTO> mapToReviewDTO(List<Review> reviews) {
        List<ReviewDTO> list = new ArrayList<>();
        // If there are reviews, do this for all the reviews

        for (Review review: reviews) {
            ReviewDTO reviewDTO = new ReviewDTO();

            reviewDTO.setReviewId(review.getId());
            reviewDTO.setTitle(review.getTitle());
            reviewDTO.setDescription(review.getDescription());
            reviewDTO.setDate_created(review.getDateCreated());
            reviewDTO.setRating(review.getRating());
            reviewDTO.setVotes(voteService.getTotalUpvotes(VoteOn.REVIEW, review.getId()) -
                    voteService.getTotalDownvotes(VoteOn.REVIEW, review.getId()));
            reviewDTO.setCommentCount(commentRepository.countByReviewId(review.getId()));
            reviewDTO.setPrice(review.getPrice());

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()) {
                Optional<User> user = userRepository.findByEmail(authentication.getName());

                if (!user.isEmpty()) {
                    VoteType voteType = voteService.getCurrentUserVote(VoteOn.REVIEW,
                            review.getId(), user.get().getId());
                    if (voteType != null){
                        reviewDTO.setCurrentUserVote(voteType);
                    }
                }
            }

            // Get User details
            UserDTO userDTO = new UserDTO();
            userDTO.setId(review.getUser().getId());
            userDTO.setUsername(review.getUser().getUsername());


            if (!review.getUser().getProfileImageDirectory().isEmpty()) {
                try {
                    userDTO.setUserImage(
                            ImageUtil.loadImage(
                                    review.getUser().getProfileImageDirectory()
                            )
                    );
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            reviewDTO.setUser(userDTO);

            // Retrieve ALL images from review
            List<byte[]> images = new ArrayList<>();
            for (ReviewImage img : review.getImages()) {
                try {
                    byte[] file  = ImageUtil.loadImage(img.getImageDirectory());
                    images.add(file);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            reviewDTO.setImages(images);
            list.add(reviewDTO);
        }

        return list;
    }

    @Override
    public void deleteReview(UUID reviewId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            Optional<User> user = userRepository.findByEmail(authentication.getName());

            if (user.isEmpty()) {
                throw new WeviewAPIException(HttpStatus.UNAUTHORIZED, "Failed to authorize user! Please login again to continue");
            }
            Optional<Review> review = reviewRepository.findById(reviewId);
            if (review.isEmpty()) {
                throw new WeviewAPIException(HttpStatus.BAD_REQUEST, "Review not found");
            }
            reviewRepository.delete(review.get());
        }
    }
}
