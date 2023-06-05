package org.weviewapp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.weviewapp.dto.ReviewDTO;
import org.weviewapp.entity.*;
import org.weviewapp.enums.ImageCategory;
import org.weviewapp.exception.WeviewAPIException;
import org.weviewapp.repository.ProductRepository;
import org.weviewapp.repository.ReviewRepository;
import org.weviewapp.repository.UserRepository;
import org.weviewapp.utils.ImageUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@CrossOrigin
@RequestMapping("/api/review")
public class ReviewController {

    @Autowired
    ReviewRepository reviewRepository;
    @Autowired
    ProductRepository productRepository;
    @Autowired
    UserRepository userRepository;
    @PostMapping("/add")
    public ResponseEntity<?> addReview(@ModelAttribute ReviewDTO reviewDTO) {
        //Check if user exists
        if(reviewRepository.existsByUser_Id(reviewDTO.getUserId())){
            throw new WeviewAPIException(HttpStatus.BAD_REQUEST, "User already has a review!");
        }

        Review review = new Review();
        review.setId(UUID.randomUUID());
        review.setRating(reviewDTO.getRating());
        review.setPrice(reviewDTO.getPrice());
        review.setTitle(reviewDTO.getTitle());
        review.setDescription(reviewDTO.getDescription());

        Optional<Product> product = productRepository.findById(reviewDTO.getProductId());
        if (product.isEmpty()) {
            throw new WeviewAPIException(HttpStatus.BAD_REQUEST, "Error: Cannot find specified product!");
        }

        review.setProduct(product.get());


        Optional<User> user = userRepository.findById(reviewDTO.getUserId());
        if (user.isEmpty()) {
            throw new WeviewAPIException(HttpStatus.BAD_REQUEST, "Error: Cannot find user!");
        }

        review.setUser(user.get());

        for (MultipartFile image: reviewDTO.getUploadedImages()) {
            ReviewImage newImage = new ReviewImage();
            newImage.setId(UUID.randomUUID());
            newImage.setReview(review);

            String imgDir = ImageUtil.uploadImage(image, ImageCategory.REVIEW_IMG);
            newImage.setImageDirectory(imgDir);
            review.getImages().add(newImage);
        }

        Review addedReview = reviewRepository.save(review);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Added successfully!");
        response.put("review", addedReview);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
