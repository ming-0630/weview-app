package org.weviewapp.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;
import org.weviewapp.entity.Review;
import org.weviewapp.enums.ProductCategory;

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

    // Preview field
    private byte[] coverImage;

    // Details field
    private List<byte[]> images;
    private List<Review> reviews;

    // Uploaded field
    private List<MultipartFile> uploadedImages;
}