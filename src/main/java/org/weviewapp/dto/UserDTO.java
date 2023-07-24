package org.weviewapp.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Data
public class UserDTO {
    private UUID id;
    private String username;
    private byte[] userImage;
    private Boolean isVerified;
    private Integer points;
    private List<String> role;
    private List<ReviewDTO> reviews;
    private List<CommentDTO> comments;
    private Integer totalUpvotes;
    private Integer totalDownvotes;
    private Integer reviewsCurrentPage;
    private Integer reviewsTotalPage;
    private Integer totalReviews;

    // For upload
    private MultipartFile uploadedImage;
}
