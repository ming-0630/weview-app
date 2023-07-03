package org.weviewapp.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;
import org.weviewapp.entity.Product;
import org.weviewapp.enums.VoteType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class ReviewDTO {
    // Common fields
    private UUID reviewId;
    private Integer rating;
    private BigDecimal price;
    private String title;
    private String description;
    private LocalDateTime date_created;
    private LocalDateTime date_updated;
    private Integer votes;
    private VoteType currentUserVote;
    private Integer commentCount;
    private boolean isVerified;

    // When add
    private UUID productId;
    private UUID userId;
    private List<MultipartFile> uploadedImages;

    // When get
    private UserDTO user;
    private Product product;
    private List<byte[]> images;
    private UUID reportId;
}