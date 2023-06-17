package org.weviewapp.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;
import org.weviewapp.enums.ProductCategory;

import java.math.BigDecimal;
import java.time.Year;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Data
public class ProductDTO {
    // Common fields
    private UUID productId;
    private String name;
    private ProductCategory category;
    private Year releaseYear;
    private String description;
    private Date date_created;
    private Date date_updated;
    private Double rating;
    private Integer ratingCount;
    private BigDecimal averagePrice;
    private BigDecimal maxPrice;
    private BigDecimal minPrice;

    // Preview field
    private byte[] coverImage;
    private boolean isWatchlisted;

    // Details field
    private List<byte[]> images;
    private List<ReviewDTO> reviews;

    // Uploaded field
    private List<MultipartFile> uploadedImages;
}