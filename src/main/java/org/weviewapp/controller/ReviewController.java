package org.weviewapp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.weviewapp.dto.CommentDTO;
import org.weviewapp.dto.RatingData;
import org.weviewapp.dto.ReviewDTO;
import org.weviewapp.entity.*;
import org.weviewapp.enums.ImageCategory;
import org.weviewapp.exception.WeviewAPIException;
import org.weviewapp.repository.CommentRepository;
import org.weviewapp.repository.ProductRepository;
import org.weviewapp.repository.ReviewRepository;
import org.weviewapp.repository.UserRepository;
import org.weviewapp.service.CommentService;
import org.weviewapp.service.ReviewService;
import org.weviewapp.service.UserService;
import org.weviewapp.service.VoteService;
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
    CommentRepository commentRepository;
    @Autowired
    ProductRepository productRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    VoteService voteService;
    @Autowired
    UserService userService;
    @Autowired
    ReviewService reviewService;
    @Autowired
    CommentService commentService;
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

        userService.modifyPoints(user.get().getId(), 100);
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Added successfully!");
        response.put("review", addedReview);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/delete")
    public ResponseEntity<?> deleteReview(@RequestParam String reviewId) {
        reviewService.deleteReview(UUID.fromString(reviewId));
        Map<String, Object> response = new HashMap<>();
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

        // TODO Add account age check, verified check.


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

    @GetMapping("/getReviews")
    public ResponseEntity<?> getReviews(
            @RequestParam String userId,
            @RequestParam Integer pageNum,
            @RequestParam (defaultValue = "dateCreated") String sortBy,
            @RequestParam (defaultValue = "desc") String direction
    ) {
        Sort.Direction sortDirection = Sort.Direction.ASC;

        if(direction.equalsIgnoreCase("desc")) {
            sortDirection = Sort.Direction.DESC;
        }

        Pageable pageable = PageRequest.of(pageNum - 1, 10, sortDirection, sortBy);

        Page<Review> reviewList = reviewRepository.findByUserId(UUID.fromString(userId), pageable);

        if (reviewList.isEmpty()) {
            // No reviews
            Map<String, Object> response = new HashMap<>();
            return new ResponseEntity<>(response, HttpStatus.OK);
        }

        List<ReviewDTO> reviews = reviewService.mapToReviewDTO(reviewList.getContent());

        Map<String, Object> response = new HashMap<>();
        response.put("reviewList", reviews);
        response.put("currentPage", pageNum);
        response.put("totalReviews", reviewList.getTotalElements());
        response.put("totalPages", reviewList.getTotalPages());


        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/getUserComments")
    public ResponseEntity<?> getUserComments(
            @RequestParam String userId,
            @RequestParam Integer pageNum,
            @RequestParam (defaultValue = "dateCreated") String sortBy,
            @RequestParam (defaultValue = "desc") String direction
    ) {
        Sort.Direction sortDirection = Sort.Direction.ASC;

        if(direction.equalsIgnoreCase("desc")) {
            sortDirection = Sort.Direction.DESC;
        }

        Pageable pageable = PageRequest.of(pageNum - 1, 10, sortDirection, sortBy);

        Page<Comment> commentList = commentRepository.findByUserId(UUID.fromString(userId), pageable);

        if (commentList.isEmpty()) {
            // No reviews
            Map<String, Object> response = new HashMap<>();
            return new ResponseEntity<>(response, HttpStatus.OK);
        }

        List<CommentDTO> comments = commentService.mapToCommentDTO(commentList.getContent());

        Map<String, Object> response = new HashMap<>();
        response.put("commentList", comments);
        response.put("currentPage", pageNum);
        response.put("totalComments", commentList.getTotalElements());
        response.put("totalPages", commentList.getTotalPages());


        return new ResponseEntity<>(response, HttpStatus.OK);
    }
    @GetMapping("/getComments")
    public ResponseEntity<?> getComments(
            @RequestParam String reviewId,
            @RequestParam Integer pageNum
    ) {
        Page<Comment> commentList;
        Pageable pageable = PageRequest.of(pageNum - 1, 10);

        commentList = commentRepository.findByReviewIdOrderByDateCreatedDesc(UUID.fromString(reviewId), pageable);

        if (commentList.isEmpty()) {
            // No comments
            Map<String, Object> response = new HashMap<>();
            response.put("commentList", new ArrayList<>());
            response.put("currentPage", pageNum);
            response.put("hasNext", commentList.hasNext());
            return new ResponseEntity<>(response, HttpStatus.OK);
        }

        List<CommentDTO> comments = commentService.mapToCommentDTO(commentList.getContent());

        Map<String, Object> response = new HashMap<>();
        response.put("commentList", comments);
        response.put("currentPage", pageNum);
        response.put("hasNext", commentList.hasNext());

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/addComment")
    public ResponseEntity<?> addComment(
            @RequestParam String reviewId,
            @RequestParam String userId,
            @RequestParam String comment) {

        Comment newComment = new Comment();
        newComment.setId(UUID.randomUUID());
        newComment.setText(comment);

        Optional<Review> review = reviewRepository.findById(UUID.fromString(reviewId));
        if (review.isEmpty()) {
            throw new WeviewAPIException(HttpStatus.BAD_REQUEST, "Error: Cannot find review!");
        }
        newComment.setReview(review.get());

        Optional<User> user = userRepository.findById(UUID.fromString(userId));
        if (user.isEmpty()) {
            throw new WeviewAPIException(HttpStatus.BAD_REQUEST, "Error: Cannot find user!");
        }
        newComment.setUser(user.get());

       Comment savedComment = commentRepository.save(newComment);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Added successfully!");
        response.put("comment", savedComment);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/deleteComment")
    public ResponseEntity<?> deleteComment(@RequestParam String commentId) {
        commentService.deleteComment(UUID.fromString(commentId));
        Map<String, Object> response = new HashMap<>();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // TODO move to service layer

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
