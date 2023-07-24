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
import org.weviewapp.repository.*;
import org.weviewapp.service.CommentService;
import org.weviewapp.service.ReviewService;
import org.weviewapp.service.UserService;
import org.weviewapp.service.VoteService;
import org.weviewapp.utils.ImageUtil;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
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
    @Autowired
    ReportReasonRepository reportReasonRepository;
    @Autowired
    ReportRepository reportRepository;

    @PostMapping("/add")
    public ResponseEntity<?> addReview(@ModelAttribute ReviewDTO reviewDTO) throws IOException {
        //Check if user exists
        if(reviewRepository.existsByProduct_ProductIdAndUser_Id(reviewDTO.getProductId(), reviewDTO.getUserId())){
            throw new WeviewAPIException(HttpStatus.BAD_REQUEST, "User already has a review for this product!");
        }

        // perform checking
        Review review = new Review();
        review.setId(UUID.randomUUID());

        Optional<Product> product = productRepository.findById(reviewDTO.getProductId());
        if (product.isEmpty()) {
            throw new WeviewAPIException(HttpStatus.BAD_REQUEST, "Error: Cannot find specified product!");
        }

        review.setProduct(product.get());

        if (reviewDTO.getPrice().compareTo(BigDecimal.valueOf(product.get().getMinProductPriceRange())) < 0  ||
                reviewDTO.getPrice().compareTo(BigDecimal.valueOf(product.get().getMaxProductPriceRange())) > 0 ) {
            throw new WeviewAPIException(HttpStatus.BAD_REQUEST, "Error: Acceptable price range for this product is "
                    + product.get().getMinProductPriceRange() + " - " + product.get().getMaxProductPriceRange());
        }

        List<String> imagesBase64 =  new ArrayList<>();
        if (reviewDTO.getUploadedImages() != null) {
            int i = 0;
            for (MultipartFile image: reviewDTO.getUploadedImages()) {
                i++;
                try {
                    byte[] fileBytes = image.getBytes();
                    String base64Bytes = Base64.getEncoder().encodeToString(fileBytes);
                    imagesBase64.add(base64Bytes);

                    ReviewImage newImage = new ReviewImage();
                    newImage.setId(UUID.randomUUID());
                    newImage.setReview(review);

                    String imgDir = ImageUtil.uploadImage(image, ImageCategory.REVIEW_IMG);
                    newImage.setImageDirectory(imgDir);
                    review.getImages().add(newImage);
                } catch (Exception ex) {
                    throw new WeviewAPIException(HttpStatus.BAD_REQUEST, ex.getMessage());
                }
            }
        }

        review.setRating(reviewDTO.getRating());
        review.setPrice(reviewDTO.getPrice());
        review.setTitle(reviewDTO.getTitle());
        review.setDescription(reviewDTO.getDescription());
        review.setSentimentScore(reviewService.sentimentAPICheck(reviewDTO.getDescription()));

        Optional<User> user = userRepository.findById(reviewDTO.getUserId());
        if (user.isEmpty()) {
            throw new WeviewAPIException(HttpStatus.BAD_REQUEST, "Error: Cannot find user!");
        }

        review.setUser(user.get());
        review.setVerified(false);
        Review addedReview = reviewRepository.save(review);
        userService.modifyPoints(user.get().getId(), 100);

        // Async call
        reviewService.reviewVerify(imagesBase64, addedReview);
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Added successfully!");
        response.put("review", addedReview);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/edit")
    public ResponseEntity<?> editReview(@ModelAttribute ReviewDTO reviewDTO) throws IOException {
        //Check if user exists
        Optional<Review> r = reviewRepository.findById(reviewDTO.getReviewId());

        if(r.isEmpty()){
            throw new WeviewAPIException(HttpStatus.BAD_REQUEST, "Cannot find review!");
        }

        Review review = r.get();

        if (reviewDTO.getPrice().compareTo(BigDecimal.valueOf(review.getProduct().getMinProductPriceRange())) < 0  ||
                reviewDTO.getPrice().compareTo(BigDecimal.valueOf(review.getProduct().getMaxProductPriceRange())) > 0 ) {
            throw new WeviewAPIException(HttpStatus.BAD_REQUEST, "Error: The acceptable price range for this product is "
                    + review.getProduct().getMinProductPriceRange() + " - " + review.getProduct().getMaxProductPriceRange());
        }

        User currentUser = userService.getCurrentUser();
        if (!review.getUser().getId().equals(currentUser.getId())) {
            throw new WeviewAPIException(HttpStatus.BAD_REQUEST, "Cannot edit other user's review!");
        }

        // Clear prev images
        if (!review.getImages().isEmpty()) {
            for (ReviewImage image: review.getImages()) {
                if(!image.getImageDirectory().equals("template_image.png")) {
                    //Avoid deleting template image
                    ImageUtil.deleteImage(image.getImageDirectory());
                }
            }
            review.getImages().clear();
        }

        List<String> imagesBase64 =  new ArrayList<>();
        if (reviewDTO.getUploadedImages() != null) {
            int i = 0;
            for (MultipartFile image: reviewDTO.getUploadedImages()) {
                i++;
                try {
                    byte[] fileBytes = image.getBytes();
                    String base64Bytes = Base64.getEncoder().encodeToString(fileBytes);
                    imagesBase64.add(base64Bytes);

                    ReviewImage newImage = new ReviewImage();
                    newImage.setId(UUID.randomUUID());
                    newImage.setReview(review);

                    String imgDir = ImageUtil.uploadImage(image, ImageCategory.REVIEW_IMG);
                    newImage.setImageDirectory(imgDir);
                    review.getImages().add(newImage);
                } catch (Exception ex) {
                    throw new WeviewAPIException(HttpStatus.BAD_REQUEST, ex.getMessage());
                }
            }
        }

        review.setRating(reviewDTO.getRating());
        review.setPrice(reviewDTO.getPrice());
        review.setTitle(reviewDTO.getTitle());
        review.setDescription(reviewDTO.getDescription());
        review.setSentimentScore(reviewService.sentimentAPICheck(reviewDTO.getDescription()));

        Optional<Report> prevReport = reportRepository.findByReporterIdAndReviewId(userService.getMLUser().getId(), reviewDTO.getReviewId());

        // Delete previous ML report
        prevReport.ifPresent(report -> reportRepository.delete(report));

        review.setVerified(false);
        Review addedReview = reviewRepository.save(review);

        // Async call
        reviewService.reviewVerify(imagesBase64, addedReview);
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Edited successfully!");
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

        Optional<List<Review>> historicalReviews =
                reviewRepository.findByIsVerifiedIsTrueAndReportIsNullAndDateCreatedBeforeAndProduct_ProductIdOrderByDateCreated(
                        startDate, UUID.fromString(productId));

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

        Optional<List<Review>> historicalReviews =
                reviewRepository.findByIsVerifiedIsTrueAndReportIsNullAndDateCreatedBeforeAndProduct_ProductIdOrderByDateCreated(
                        startDate, UUID.fromString(productId));

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
        LocalDateTime startDate;

        Optional<Review> r = reviewRepository.findFirstByIsVerifiedIsTrueAndReportIsNullAndProduct_ProductIdOrderByDateCreatedAsc(UUID.fromString(productId));

        if (r.isEmpty()) {
            startDate = LocalDateTime.now().minusDays(1);
        } else {
             startDate = r.get().getDateCreated();
        }

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

        Pageable pageable;
        Page<Review> pagedReview;
        if (sortBy.equals("votes")) {
            pageable = PageRequest.of(pageNum - 1, 5);
            pagedReview = reviewService.getReviewsByUserIdSortByVotes(UUID.fromString(userId), sortDirection.toString(), pageable);
        } else {
            pageable = PageRequest.of(pageNum - 1, 5, sortDirection, sortBy);
            pagedReview = reviewService.getReviewsByUserId(UUID.fromString(userId), pageable);
        }

        List<ReviewDTO> reviewDTOS = reviewService.mapToReviewDTO(pagedReview.getContent());


        if (reviewDTOS.isEmpty()) {
            // No reviews
            Map<String, Object> response = new HashMap<>();
            return new ResponseEntity<>(response, HttpStatus.OK);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("reviewList", reviewDTOS);
        response.put("currentPage", pageNum);
        response.put("totalReviews", pagedReview.getTotalElements());
        response.put("totalPages", pagedReview.getTotalPages());

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/getOneReview")
    public ResponseEntity<?> getOneReviews(
            @RequestParam String reviewId
    ) {
        Optional<Review> review = reviewRepository.findById(UUID.fromString(reviewId));

        if (review.isEmpty()) {
            throw new WeviewAPIException(HttpStatus.BAD_REQUEST, "Cannot find review!");
        }

        ReviewDTO reviewDTO = reviewService.mapToReviewDTO(List.of(review.get())).get(0);
        return new ResponseEntity<>(reviewDTO, HttpStatus.OK);
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

        Pageable pageable = PageRequest.of(pageNum - 1, 5, sortDirection, sortBy);

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
                findByIsVerifiedIsTrueAndReportIsNullAndDateCreatedBetweenAndProduct_ProductIdOrderByDateCreated(startDate, endDate, UUID.fromString(productId));

        List<RatingData> ratingDataList = new ArrayList<>();

        double currentRating = initialRating;

        long hoursBetween = ChronoUnit.HOURS.between(startDate, endDate);
        long daysBetween = ChronoUnit.DAYS.between(startDate, endDate);

        if (hoursBetween < 24 && daysBetween < 1) {
            startDate = startDate.minusDays(1);
        }

        for (LocalDateTime date = startDate; date.isBefore(endDate) || date.isEqual(endDate); date = date.plusDays(1)) {
            if (reviews.isEmpty()) {
                RatingData ratingData = new RatingData(date, currentRating);
                ratingDataList.add(ratingData);
            } else {
                for (Review review : reviews.get()) {
                    if (review.getDateCreated().toLocalDate().isEqual(date.toLocalDate())) {
                        currentRating = ((currentRating * numberOfElements) + review.getRating()) / (numberOfElements + 1);
                        numberOfElements++;
                    }
                }

                RatingData ratingData = new RatingData(date, currentRating);
                ratingDataList.add(ratingData);
            }
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
