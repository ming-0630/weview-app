package org.weviewapp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.weviewapp.dto.RatingData;
import org.weviewapp.dto.ReviewDTO;
import org.weviewapp.entity.Product;
import org.weviewapp.entity.Review;
import org.weviewapp.entity.ReviewImage;
import org.weviewapp.entity.User;
import org.weviewapp.enums.ImageCategory;
import org.weviewapp.exception.WeviewAPIException;
import org.weviewapp.repository.ProductRepository;
import org.weviewapp.repository.ReviewRepository;
import org.weviewapp.repository.UserRepository;
import org.weviewapp.utils.ImageUtil;

import java.time.LocalDateTime;
import java.util.*;

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
        if(reviewRepository.existsByProduct_ProductIdAndUser_Id(reviewDTO.getProductId(), reviewDTO.getUserId())){
            throw new WeviewAPIException(HttpStatus.BAD_REQUEST, "User already has a review for this product!");
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

        if (reviewDTO.getUploadedImages() != null) {
            for (MultipartFile image: reviewDTO.getUploadedImages()) {
                ReviewImage newImage = new ReviewImage();
                newImage.setId(UUID.randomUUID());
                newImage.setReview(review);

                String imgDir = ImageUtil.uploadImage(image, ImageCategory.REVIEW_IMG);
                newImage.setImageDirectory(imgDir);
                review.getImages().add(newImage);
            }
        }


        Review addedReview = reviewRepository.save(review);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Added successfully!");
        response.put("review", addedReview);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/checkUserEligibility")
    public ResponseEntity<?> checkUserEligibility(@RequestParam String userId,
                                                  @RequestParam String productId
    ) {
        if (userId.isEmpty()) {
            throw new WeviewAPIException(HttpStatus.BAD_REQUEST, "UserId parameter is empty!");
        }

        if (productId.isEmpty()) {
            throw new WeviewAPIException(HttpStatus.BAD_REQUEST, "ProductId parameter is empty!");
        }

        Optional<User> user = userRepository
                .findById(UUID.fromString(userId));

        if(reviewRepository.existsByProduct_ProductIdAndUser_Id(
                UUID.fromString(productId),
                UUID.fromString(userId))) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Sorry, you already have a review on this product!");
            response.put("isEligible", false);
            return new ResponseEntity<>(response, HttpStatus.OK);
        }

        // Add account age check, verified check.


        // Success case
        Map<String, Object> response = new HashMap<>();
        response.put("isEligible", true);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/getRating1M")
    public ResponseEntity<?> getRating1M(@RequestParam String productId) {

        LocalDateTime endDate = LocalDateTime.now();

        LocalDateTime startDate = LocalDateTime.now().minusDays(30);

        Optional<List<Review>> historicalReviews = reviewRepository.findByProduct_ProductIdAndDateCreatedBeforeOrderByDateCreated(UUID.fromString(productId), startDate);

        double initialRating = calculateInitialRating(historicalReviews.get());
        Integer numberOfElements = historicalReviews.get().size();

        List<RatingData> ratingDataList = getRatingDataList(
                startDate,
                endDate,
                productId,
                initialRating,
                numberOfElements);

        Map<String, Object> response = new HashMap<>();
        response.put("ratingData", ratingDataList);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/getRating1Y")
    public ResponseEntity<?> getRating1Y(@RequestParam String productId) {

        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = LocalDateTime.now().minusDays(365);

        Optional<List<Review>> historicalReviews = reviewRepository.findByProduct_ProductIdAndDateCreatedBeforeOrderByDateCreated(UUID.fromString(productId), startDate);

        double initialRating = calculateInitialRating(historicalReviews.get());
        Integer numberOfElements = historicalReviews.get().size();

        List<RatingData> ratingDataList = getRatingDataList(
                startDate,
                endDate,
                productId,
                initialRating,
                numberOfElements);

        Map<String, Object> response = new HashMap<>();
        response.put("ratingData", ratingDataList);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/getRatingMAX")
    public ResponseEntity<?> getRatingMax(@RequestParam String productId) {

        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = reviewRepository.findFirstByProduct_ProductIdOrderByDateCreatedAsc(UUID.fromString(productId)).get().getDateCreated();

        List<RatingData> ratingDataList = getRatingDataList(
                startDate,
                endDate,
                productId,
                0.0,
                0);
        Map<String, Object> response = new HashMap<>();
        response.put("ratingData", ratingDataList);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    private List<RatingData> getRatingDataList(LocalDateTime startDate,
                                               LocalDateTime endDate,
                                               String productId,
                                               Double initialRating,
                                               Integer numberOfElements) {
        Optional<List<Review>> reviews = reviewRepository.
                findByProduct_ProductIdAndDateCreatedBetweenOrderByDateCreated(UUID.fromString(productId), startDate, endDate);

        List<RatingData> ratingDataList = new ArrayList<>();

        double currentRating = initialRating;

        for (LocalDateTime date = startDate; date.isBefore(endDate) || date.isEqual(endDate); date = date.plusDays(1)) {

            for (Review review : reviews.get()) {
                if (review.getDateCreated().toLocalDate().isEqual(date.toLocalDate())) {
                    currentRating = ((currentRating * numberOfElements) + review.getRating()) / (numberOfElements + 1);
                    numberOfElements++;
                }
            }

            RatingData ratingData = new RatingData(date, currentRating);
            ratingDataList.add(ratingData);
        }
        return ratingDataList;
    }

    private double calculateInitialRating(List<Review> historicalReviews) {
        if (historicalReviews.isEmpty()) {
            return 0.0;
        }

        double sum = 0.0;
        for (Review review : historicalReviews) {
            sum += review.getRating();
        }

        return sum / historicalReviews.size();
    }
}
