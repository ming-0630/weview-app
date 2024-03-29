package org.weviewapp.service.impl;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.weviewapp.dto.ClassificationResult;
import org.weviewapp.dto.ReviewDTO;
import org.weviewapp.entity.*;
import org.weviewapp.enums.ReportAction;
import org.weviewapp.enums.VoteOn;
import org.weviewapp.enums.VoteType;
import org.weviewapp.exception.WeviewAPIException;
import org.weviewapp.repository.*;
import org.weviewapp.service.ReviewService;
import org.weviewapp.service.UserService;
import org.weviewapp.service.VoteService;
import org.weviewapp.utils.ImageUtil;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class ReviewServiceImpl implements ReviewService {
    @Autowired
    private VoteService voteService;
    @Autowired
    private CommentRepository commentRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ReviewRepository reviewRepository;
    @Autowired
    private UserService userService;
    @Autowired
    private ReportRepository reportRepository;
    @Autowired
    private ReportReasonRepository reportReasonRepository;

    @Override
    public List<ReviewDTO> mapToReviewDTO(List<Review> reviews) {
        List<ReviewDTO> list = new ArrayList<>();
        // If there are reviews, do this for all the reviews
        for (Review review: reviews) {
            ReviewDTO reviewDTO = new ReviewDTO();

            reviewDTO.setReviewId(review.getId());
            reviewDTO.setTitle(review.getTitle());
            reviewDTO.setDescription(review.getDescription());
            reviewDTO.setDate_created(review.getDateCreated());
            reviewDTO.setDate_updated(review.getDateUpdated());
            reviewDTO.setRating(review.getRating());
            reviewDTO.setVotes(voteService.getTotalUpvotes(VoteOn.REVIEW, review.getId()) -
                    voteService.getTotalDownvotes(VoteOn.REVIEW, review.getId()));
            reviewDTO.setCommentCount(commentRepository.countByReviewId(review.getId()));
            reviewDTO.setPrice(review.getPrice());
            reviewDTO.setVerified(review.isVerified());
            reviewDTO.setProductId(review.getProduct().getProductId());
            reviewDTO.setSentimentScore(review.getSentimentScore());

            if (review.getReport() != null) {
                reviewDTO.setReportId(review.getReport().getId());
            }

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()) {
                Optional<User> user = userRepository.findByEmail(authentication.getName());

                if (!user.isEmpty()) {
                    VoteType voteType = voteService.getCurrentUserVote(VoteOn.REVIEW,
                            review.getId(), user.get().getId());
                    if (voteType != null){
                        reviewDTO.setCurrentUserVote(voteType);
                    }
                }
            }

            reviewDTO.setUser(userService.mapUserToDTO(review.getUser()));

            // Retrieve ALL images from review
            List<byte[]> images = new ArrayList<>();
            for (ReviewImage img : review.getImages()) {
                try {
                    byte[] file  = ImageUtil.loadImage(img.getImageDirectory());
                    images.add(file);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            reviewDTO.setImages(images);
            list.add(reviewDTO);
        }

        return list;
    }

    @Override
    public void deleteReview(UUID reviewId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            Optional<User> user = userRepository.findByEmail(authentication.getName());

            if (user.isEmpty()) {
                throw new WeviewAPIException(HttpStatus.UNAUTHORIZED, "Failed to authorize user! Please login again to continue");
            }

            // delete all reports related to review
            Optional<Review> review = reviewRepository.findById(reviewId);
            if (review.isEmpty()) {
                throw new WeviewAPIException(HttpStatus.BAD_REQUEST, "Review not found");
            }

            // delete all reports related to review
            Optional<List<Report>> reportList = reportRepository.findAllByReviewId(reviewId);
            reportList.ifPresent(reports -> reportRepository.deleteAll(reports));

            if (review.get().getImages() != null) {
                review.get().getImages().forEach(img -> {
                    if (!img.getImageDirectory().equals("template_image.png")) {
                        ImageUtil.deleteImage(img.getImageDirectory());
                    }
                });
            }

            reviewRepository.delete(review.get());
            userService.modifyPoints(user.get().getId(), -100);
        }
    }
    @Override
    public List<Review> getAllReviewsByProductId(UUID productId) {
        Optional<List<Review>> reviews = reviewRepository.findAllByIsVerifiedIsTrueAndReportIsNullAndProduct_ProductId(productId, Sort.by(Sort.DEFAULT_DIRECTION, "dateCreated"));
        return reviews.get();
    }

    @Override
    public Page<Review> getReviewsByProductId(UUID productId, Pageable pageable) {
        Page<Review> reviews = reviewRepository.findByIsVerifiedIsTrueAndReportIsNullAndProduct_ProductId(
                productId, pageable);
        return reviews;
    }
    @Override
    public Page<Review> getReviewsByProductIdSortByVotes(UUID productId, String sortDirection, Pageable pageable) {
        Page<Review> reviews = reviewRepository.findByProductIdSortedByVoteDifference(productId, sortDirection, pageable);
        return reviews;
    }

    @Override
    public Page<Review> getReviewsByUserId(UUID userId, Pageable pageable) {
        Page<Review> reviews;
        if (userService.getCurrentUser() != null && userId.equals(userService.getCurrentUser().getId())) {
            reviews = reviewRepository.findByUserId(userId, pageable);
        } else {
            reviews = reviewRepository.findAllByIsVerifiedIsTrueAndReportIsNullAndUserIdOrderByDateCreatedDesc(userId, pageable);
        }
        return reviews;
    }
    @Override
    public Page<Review> getReviewsByUserIdSortByVotes(UUID userId, String sortDirection, Pageable pageable) {
        Page<Review> reviews = reviewRepository.findByUserIdSortedByVoteDifference(userId, sortDirection, pageable);
        return reviews;
    }

    @Override
    public Optional<Review> getUnverifiedOrReportedReview(UUID userId, UUID productId) {
        return reviewRepository.findFirstByIsVerifiedIsFalseOrReportIsNotNullAndUser_IdAndProduct_ProductId(userId, productId);
    }

    @Override
    public LocalDateTime getEarliestReviewDate(UUID productId) {
        LocalDateTime startDate;
        Optional<Review> r = reviewRepository.findFirstByIsVerifiedIsTrueAndReportIsNullAndProduct_ProductIdOrderByDateCreatedAsc(productId);

        if (!r.isEmpty()) {
            startDate = r.get().getDateCreated();
            return startDate;
        } else {
            return null;
        }
    }

    @Override
    public LocalDateTime getLatestReviewDate(UUID productId) {
        LocalDateTime endDate;
        Optional<Review> r = reviewRepository.findFirstByIsVerifiedIsTrueAndReportIsNullAndProduct_ProductIdOrderByDateCreatedDesc(productId);

        if (!r.isEmpty()) {
            endDate = r.get().getDateCreated();
            return endDate;
        } else {
            return null;
        }
    }
    @Override
    public List<Number> getRatings(List<Review> reviews) {
        List<Number> ratingList = new ArrayList<>();

        for(Review r: reviews) {
            ratingList.add(r.getRating());
        }
        return ratingList;
    }

    @Override
    @Async
    public void reviewVerify(List<String> imageBase64, Review review) {
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        List<String> reportDesc =  new ArrayList<>();
        List<ReportReason> reportReasons =  new ArrayList<>();
        try {
            int i = 0;
            for (String imgString: imageBase64) {
                i++;
                String desc = ImageUtil.imageAPICheck(imgString, i, httpClient);

                if(!desc.isBlank()) {
                    reportDesc.add(desc);
                }
            }

            if (!reportDesc.isEmpty()) {
                reportReasons.add(reportReasonRepository.findByNameIn(List.of("IMAGE")).get().get(0));
            }

            String spamCheckDesc = spamAPICheck(review.getDescription(), httpClient);

            if (!spamCheckDesc.isBlank()) {
                reportDesc.add(spamCheckDesc);
            }

            String gibberishCheckDesc = gibberishAPICheck(review.getDescription(), httpClient);

            if (!gibberishCheckDesc.isBlank()) {
                reportDesc.add(gibberishCheckDesc);
            }

            if (!spamCheckDesc.isBlank() || !gibberishCheckDesc.isBlank()) {
                reportReasons.add(reportReasonRepository.findByNameIn(List.of("FAKE")).get().get(0));
            }

            // Set Verified automatically
            if (!reportDesc.isEmpty()) {
                // Double check if review is deleted or not
                Optional<Review> addedReview = reviewRepository.findById(review.getId());
                if (!addedReview.isEmpty()) {
                    Report r = new Report();
                    r.setAction(ReportAction.REVIEWING);
                    r.setReporter(userRepository.findByEmail("ML").get());
                    r.setId(UUID.randomUUID());
                    r.setReview(review);
                    r.setDescription(generateReport(reportDesc));

                    r.setReportReasons(reportReasons);
                    reportRepository.save(r);
                }
            } else {
                // No flags!
                review.setVerified(true);
                reviewRepository.save(review);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                httpClient.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private String spamAPICheck(String desc, CloseableHttpClient httpClient) throws IOException {
        String apiUrl = "http://localhost:5000/api/spam";
        HttpPost request = new HttpPost(apiUrl);
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("message", desc);

        Gson gson = new Gson();
        String requestBody = gson.toJson(jsonObject);
        StringEntity params = new StringEntity(requestBody);
        request.addHeader("content-type", "application/json");
        request.setEntity(params);

        HttpResponse response = httpClient.execute(request);
        int statusCode = response.getStatusLine().getStatusCode();
        String responseData = EntityUtils.toString(response.getEntity());

        if (statusCode >= 200 && statusCode < 300) {
            ClassificationResult[] results = gson.fromJson(responseData, ClassificationResult[].class);
            double safeProbability = 0.0;
            for (ClassificationResult classifications : results) {
                if (classifications.getLabel().equalsIgnoreCase("ham")) {
                    safeProbability = classifications.getScore();
                    if (safeProbability > 0.5) {
                        return "";
                    }

                    Arrays.sort(results, (a, b) -> Double.compare(b.getScore(), a.getScore()));
                    double totalScore = 0;
                    for (ClassificationResult result : results) {
                        totalScore += result.getScore();
                    }

                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("Spam " + ":\n");
                    for (ClassificationResult result : results) {
                        double percentage = (result.getScore() / totalScore) * 100;
                        stringBuilder.append("- ")
                                .append(result.getLabel().toUpperCase())
                                .append(": ")
                                .append(String.format("%.2f%%", percentage))
                                .append("\n");
                    }
                    return stringBuilder.toString();
                }
            }
        } else {
            throw new WeviewAPIException(HttpStatus.BAD_REQUEST, "Spam verification API failed! Error: " + responseData);
        }
        return "";
    }

    private String gibberishAPICheck(String desc, CloseableHttpClient httpClient) throws IOException {
        String apiUrl = "http://localhost:5000/api/gibberish";
        HttpPost request = new HttpPost(apiUrl);
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("message", desc);

        Gson gson = new Gson();
        String requestBody = gson.toJson(jsonObject);
        StringEntity params = new StringEntity(requestBody);
        request.addHeader("content-type", "application/json");
        request.setEntity(params);

        HttpResponse response = httpClient.execute(request);
        int statusCode = response.getStatusLine().getStatusCode();
        String responseData = EntityUtils.toString(response.getEntity());

        if (statusCode >= 200 && statusCode < 300) {
            ClassificationResult results = gson.fromJson(responseData, ClassificationResult.class);
            if (results != null) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("Gibberish: " + ":\n");
                    double percentage = (results.getScore()) * 100;
                    stringBuilder.append("- ")
                            .append(results.getLabel().toUpperCase())
                            .append(": ")
                            .append(String.format("%.2f%%", percentage))
                            .append("\n");
                return stringBuilder.toString();
            }
        } else {
            throw new WeviewAPIException(HttpStatus.BAD_REQUEST, "Gibberish verification API failed! Error: " + responseData);
        }
        return "";
    }
    private static String generateReport(List<String> desc) {
        StringBuilder sb = new StringBuilder("--- ML Report ---\n");

        for (int i = 0; i < desc.size(); i++) {
            sb.append(desc.get(i));
            sb.append("\n");
        }

        return sb.toString();
    }
    public Integer sentimentAPICheck(String desc) throws IOException {
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        String apiUrl = "http://localhost:5000/api/sentiment";
        HttpPost request = new HttpPost(apiUrl);
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("message", desc);

        Gson gson = new Gson();
        String requestBody = gson.toJson(jsonObject);
        StringEntity params = new StringEntity(requestBody);
        request.addHeader("content-type", "application/json");
        request.setEntity(params);

        HttpResponse response = httpClient.execute(request);
        int statusCode = response.getStatusLine().getStatusCode();
        String responseData = EntityUtils.toString(response.getEntity());

        if (statusCode >= 200 && statusCode < 300) {
            JsonParser jsonParser = new JsonParser();
            JsonObject jsonResult = jsonParser.parse(responseData).getAsJsonObject();
            double score = jsonResult.get("normalized_score").getAsDouble();
            return (int) Math.round(score);
        } else {
            throw new WeviewAPIException(HttpStatus.BAD_REQUEST, "Sentiment analysis API failed! Error: " + responseData);
        }
    }

    public Review getRandomReview() throws IOException {
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        String apiUrl = "http://localhost:5000/api/getReview";
        HttpPost request = new HttpPost(apiUrl);

        request.addHeader("content-type", "application/json");

        HttpResponse response = httpClient.execute(request);
        int statusCode = response.getStatusLine().getStatusCode();
        String responseData = EntityUtils.toString(response.getEntity());

        if (statusCode >= 200 && statusCode < 300) {
            JsonParser jsonParser = new JsonParser();
            JsonObject jsonResult = jsonParser.parse(responseData).getAsJsonObject();
            Review review = new Review();
            review.setDescription(jsonResult.get("review_body").getAsString());
            review.setTitle(jsonResult.get("review_headline").getAsString());
            review.setRating(jsonResult.get("star_rating").getAsInt());
            review.setSentimentScore(sentimentAPICheck(jsonResult.get("review_body").getAsString()));
            return review;
        } else {
            throw new WeviewAPIException(HttpStatus.BAD_REQUEST, "Sentiment analysis API failed! Error: " + responseData);
        }
    }
}
