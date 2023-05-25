package org.weviewapp.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;
import org.weviewapp.entity.Product;
import org.weviewapp.entity.Review;

import java.time.Year;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Data
public class ProductDTO {
    private UUID productId;
    private String name;
    private Product.Category category;
    private Year releaseYear;
    private String description;
    private Date date_created;
    private Date date_updated;
    private byte[] coverImage;
    private List<MultipartFile> images;
    private List<Review> reviews;
}