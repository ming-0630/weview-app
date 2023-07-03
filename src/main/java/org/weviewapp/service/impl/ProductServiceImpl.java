package org.weviewapp.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.weviewapp.dto.ProductDTO;
import org.weviewapp.entity.Product;
import org.weviewapp.entity.ProductImage;
import org.weviewapp.entity.Review;
import org.weviewapp.exception.WeviewAPIException;
import org.weviewapp.repository.ProductRepository;
import org.weviewapp.repository.UserRepository;
import org.weviewapp.service.ProductService;
import org.weviewapp.service.ReviewService;
import org.weviewapp.service.WatchlistService;
import org.weviewapp.utils.ImageUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalDouble;

@Service
public class ProductServiceImpl implements ProductService {
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private WatchlistService watchlistService;
    @Autowired
    private ReviewService reviewService;

    @Override
    public List<ProductDTO> mapToPreviewDTO(List<Product> products) {
        List<ProductDTO> productDTOList = new ArrayList<>();
        for (Product product : products) {
            ProductDTO productDTO = new ProductDTO();

            try {
                if (product.getImages() != null) {
                    productDTO.setCoverImage(
                            ImageUtil.loadImage(product.getImages().get(0).getImageDirectory())
                    );
                }
            } catch (Exception e) {
                throw new WeviewAPIException(HttpStatus.BAD_REQUEST, e.getMessage());
            }

            List<Review> list = reviewService.getAllReviewsByProductId(product.getProductId());

            productDTO.setProductId(product.getProductId());
            productDTO.setName(product.getName());
            productDTO.setCategory(product.getCategory());
            productDTO.setReleaseYear(product.getReleaseYear());
            productDTO.setDescription(product.getDescription());
            productDTO.setDate_created(product.getCreated());
            productDTO.setDate_updated(product.getUpdated());
            productDTO.setWatchlisted(watchlistService.getIsWatchlisted(product));

            // Used for non-detailed product getting
            if (!list.isEmpty()) {
                productDTO.setRatingCount(list.size());

                List<Integer> rating = new ArrayList<>();
                for (Review review : list) {
                    rating.add(review.getRating());
                }

                OptionalDouble average = rating
                        .stream()
                        .mapToDouble(a -> a)
                        .average();

                productDTO.setRating(average.getAsDouble());
            }

            productDTOList.add(productDTO);
        }
        return productDTOList;
    }

    @Override
    public ProductDTO mapToEditProductDTO(Product product) {
        ProductDTO productDTO = new ProductDTO();

        try {
            if (product.getImages() != null) {
                List<byte[]> images = new ArrayList<>();
                for (ProductImage img : product.getImages()) {
                    byte[] file = ImageUtil.loadImage(img.getImageDirectory());
                    images.add(file);
                }
                productDTO.setImages(images);
            }
        } catch (Exception e) {
            throw new WeviewAPIException(HttpStatus.BAD_REQUEST, e.getMessage());
        }

        productDTO.setProductId(product.getProductId());
        productDTO.setName(product.getName());
        productDTO.setCategory(product.getCategory());
        productDTO.setReleaseYear(product.getReleaseYear());
        productDTO.setDescription(product.getDescription());
        productDTO.setDate_created(product.getCreated());
        productDTO.setDate_updated(product.getUpdated());

        return productDTO;
    }
}
