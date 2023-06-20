package org.weviewapp.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.weviewapp.dto.CommentDTO;
import org.weviewapp.dto.UserDTO;
import org.weviewapp.entity.Comment;
import org.weviewapp.entity.User;
import org.weviewapp.enums.VoteOn;
import org.weviewapp.enums.VoteType;
import org.weviewapp.exception.WeviewAPIException;
import org.weviewapp.repository.CommentRepository;
import org.weviewapp.repository.ReviewRepository;
import org.weviewapp.repository.UserRepository;
import org.weviewapp.service.CommentService;
import org.weviewapp.service.VoteService;
import org.weviewapp.utils.ImageUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class CommentServiceImpl implements CommentService {
    @Autowired
    VoteService voteService;
    @Autowired
    CommentRepository commentRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    ReviewRepository reviewRepository;
    @Override
    public List<CommentDTO> mapToCommentDTO(List<Comment> comments) {
        List<CommentDTO> list = new ArrayList<>();
        // If there are reviews, do this for all the reviews
        for(Comment c: comments) {
            CommentDTO comment = new CommentDTO();
            comment.setCommentId(c.getId());
            comment.setText(c.getText());
            comment.setDateCreated(c.getDateCreated());
            comment.setReviewId(c.getReview().getId());
            comment.setProductId(c.getReview().getProduct().getProductId());

            comment.setVotes(voteService.getTotalUpvotes(VoteOn.COMMENT, c.getId()) -
                    voteService.getTotalDownvotes(VoteOn.COMMENT, c.getId()));
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()) {
                Optional<User> user = userRepository.findByEmail(authentication.getName());

                if (!user.isEmpty()) {
                    VoteType voteType = voteService.getCurrentUserVote(VoteOn.COMMENT, c.getId(), user.get().getId());
                    if (voteType != null){
                        comment.setCurrentUserVote(voteType);
                    }
                }
            }

            UserDTO userDTO = new UserDTO();
            userDTO.setId(c.getUser().getId());
            userDTO.setUsername(c.getUser().getUsername());

            if(!c.getUser().getProfileImageDirectory().equals("")) {
                try{
                    byte[] userImage = ImageUtil.loadImage(c.getUser().getProfileImageDirectory());
                    userDTO.setUserImage(userImage);
                } catch (Exception e) {
                    throw new WeviewAPIException(HttpStatus.BAD_REQUEST, e.getMessage());
                }
            }
            comment.setUser(userDTO);

            list.add(comment);
        }

        return list;
    }

    @Override
    public void deleteComment(UUID commentId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            Optional<User> user = userRepository.findByEmail(authentication.getName());

            if (user.isEmpty()) {
                throw new WeviewAPIException(HttpStatus.UNAUTHORIZED, "Failed to authorize user! Please login again to continue");
            }
            Optional<Comment> comment = commentRepository.findById(commentId);
            if (comment.isEmpty()) {
                throw new WeviewAPIException(HttpStatus.BAD_REQUEST, "Comment not found");
            }
            commentRepository.delete(comment.get());
        }
    }
}
