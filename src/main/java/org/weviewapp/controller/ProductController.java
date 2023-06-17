package org.weviewapp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.weviewapp.dto.ProductDTO;
import org.weviewapp.dto.ProductResponseDTO;
import org.weviewapp.dto.ReviewDTO;
import org.weviewapp.dto.UserDTO;
import org.weviewapp.entity.*;
import org.weviewapp.enums.ImageCategory;
import org.weviewapp.enums.ProductCategory;
import org.weviewapp.enums.VoteOn;
import org.weviewapp.enums.VoteType;
import org.weviewapp.exception.WeviewAPIException;
import org.weviewapp.repository.CommentRepository;
import org.weviewapp.repository.ProductRepository;
import org.weviewapp.repository.UserRepository;
import org.weviewapp.service.ProductService;
import org.weviewapp.service.VoteService;
import org.weviewapp.service.WatchlistService;
import org.weviewapp.utils.ImageUtil;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@RestController
@CrossOrigin
@RequestMapping("/api/product")
public class ProductController {
    private static final int ITEMS_PER_PAGE = 5;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ProductService productService;
    @Autowired
    private CommentRepository commentRepository;
    @Autowired
    private VoteService voteService;
    @Autowired
    private WatchlistService watchlistService;

    @PostMapping("/add")
    public ResponseEntity<?> addProduct(@ModelAttribute ProductDTO productDto) {
        // add check for username exists in database
        if(productRepository.existsByName(productDto.getName())){
            throw new WeviewAPIException(HttpStatus.BAD_REQUEST, "Product Name exists");
        }

        Product product = new Product();
        product.setProductId(UUID.randomUUID());
        product.setName(productDto.getName());
        product.setCategory(productDto.getCategory());
        product.setReleaseYear(productDto.getReleaseYear());
        product.setDescription(productDto.getDescription());

        for (MultipartFile image: productDto.getUploadedImages()) {
            ProductImage newImage = new ProductImage();
            newImage.setId(UUID.randomUUID());
            newImage.setProduct(product);

            String imgDir = ImageUtil.uploadImage(image, ImageCategory.PRODUCT_IMG);
            newImage.setImageDirectory(imgDir);
            product.getImages().add(newImage);
        }

        Product addedProduct = productRepository.save(product);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Added successfully!");
        response.put("product", addedProduct);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/getAllPreview")
    public ResponseEntity<?> getAllPreview(@RequestParam (defaultValue = "0") Integer pageNum,
                                           @RequestParam (defaultValue = "name") String sortBy,
                                           @RequestParam (defaultValue = "asc") String direction
    ) {
        Sort.Direction sortDirection = Sort.Direction.ASC;

        if(direction.equalsIgnoreCase("desc")) {
            sortDirection = Sort.Direction.DESC;
        }

        // Pageable starts page at 0, while front end starts at 1
        Pageable pageable = PageRequest.of(pageNum - 1, ITEMS_PER_PAGE, sortDirection, sortBy);

        Page<Product> productList = productRepository.findAll(pageable);

        if (productList.isEmpty()) {
            throw new WeviewAPIException(HttpStatus.BAD_REQUEST, "No products found!");
        }

        List<ProductDTO> result = productService.mapToPreviewDTO(productList.getContent());

        ProductResponseDTO dto = new ProductResponseDTO();
        dto.setProductDTOs(result);
        dto.setTotalPages(productList.getTotalPages());
        dto.setCurrentPage(productList.getPageable().getPageNumber());

        return new ResponseEntity<>(dto, HttpStatus.OK);
    }

    @GetMapping("/getCategoryPreview")
    public ResponseEntity<?> getAllPreview(@RequestParam ProductCategory category,
                                           @RequestParam Integer pageNum,
                                           @RequestParam (defaultValue = "name") String sortBy,
                                           @RequestParam (defaultValue = "asc") String direction) {
        Sort.Direction sortDirection = Sort.Direction.ASC;

        if(direction.equalsIgnoreCase("desc")) {
            sortDirection = Sort.Direction.DESC;
        }

        Pageable pageable = PageRequest.of(pageNum - 1, ITEMS_PER_PAGE, sortDirection, sortBy);
        Page<Product> productList = productRepository.findByCategory(category, pageable);

        if (productList.isEmpty()) {
            throw new WeviewAPIException(HttpStatus.BAD_REQUEST, "No products found!");
        }

        List<ProductDTO> result = productService.mapToPreviewDTO(productList.getContent());

        ProductResponseDTO dto = new ProductResponseDTO();
        dto.setProductDTOs(result);
        dto.setTotalPages(productList.getTotalPages());
        dto.setCurrentPage(productList.getPageable().getPageNumber());

        return new ResponseEntity<>(dto, HttpStatus.OK);
    }
    @GetMapping("/search")
    public ResponseEntity<?> searchProducts(@RequestParam String keyword,
                                            @RequestParam ProductCategory category,
                                            @RequestParam Integer pageNum,
                                            @RequestParam (defaultValue = "name") String sortBy,
                                            @RequestParam (defaultValue = "asc") String direction) {
        Sort.Direction sortDirection = Sort.Direction.ASC;

        if(direction.equalsIgnoreCase("desc")) {
            sortDirection = Sort.Direction.DESC;
        }

        Page<Product> productList;
        Pageable pageable = PageRequest.of(pageNum - 1, ITEMS_PER_PAGE, sortDirection, sortBy);

        if (category == null || category.equals(ProductCategory.ALL)) {
            productList = productRepository
                    .findByNameContainingIgnoreCase(keyword, pageable);
        } else {
            productList = productRepository
                    .findByNameContainingIgnoreCaseAndCategory(keyword, category, pageable);
        }

        if (productList.isEmpty()) {
            throw new WeviewAPIException(HttpStatus.BAD_REQUEST, "No products found with the keyword " +
                    keyword + " and Category: " + category);
        }

        List<ProductDTO> result = productService.mapToPreviewDTO(productList.getContent());

        ProductResponseDTO dto = new ProductResponseDTO();
        dto.setProductDTOs(result);
        dto.setTotalPages(productList.getTotalPages());
        dto.setCurrentPage(productList.getPageable().getPageNumber());

        return new ResponseEntity<>(dto, HttpStatus.OK);
    }

    @GetMapping("/details")
    public ResponseEntity<?> getProductDetails(@RequestParam String id) {

        Optional<Product> product = productRepository
                .findById(UUID.fromString(id));

        if (product.isEmpty()) {
            throw new WeviewAPIException(HttpStatus.BAD_REQUEST, "Cannot find the specified product");
        }

        ProductDTO productDTO = new ProductDTO();
        try {
            if (product.get().getImages() != null) {
                List<byte[]> images = new ArrayList<>();
                for (ProductImage img : product.get().getImages()) {
                    byte[] file = ImageUtil.loadImage(img.getImageDirectory());
                    images.add(file);
                }
                productDTO.setImages(images);
            }
        } catch (Exception e) {
            throw new WeviewAPIException(HttpStatus.BAD_REQUEST, e.getMessage());
        }

        productDTO.setProductId(product.get().getProductId());
        productDTO.setName(product.get().getName());
        productDTO.setCategory(product.get().getCategory());
        productDTO.setReleaseYear(product.get().getReleaseYear());
        productDTO.setDescription(product.get().getDescription());
        productDTO.setDate_created(product.get().getCreated());
        productDTO.setDate_updated(product.get().getUpdated());
        productDTO.setWatchlisted(watchlistService.getIsWatchlisted(product.get()));

        try {
            if (product.get().getReviews().size() > 0) {
                List<ReviewDTO> list = new ArrayList<>();

                List<Integer> ratingList = new ArrayList<>();
                List<BigDecimal> priceList = new ArrayList<>();
                // If there are reviews, do this for all the reviews

                for (Review review: product.get().getReviews()) {
                    ReviewDTO reviewDTO = new ReviewDTO();

                    reviewDTO.setReviewId(review.getId());
                    reviewDTO.setTitle(review.getTitle());
                    reviewDTO.setDescription(review.getDescription());
                    reviewDTO.setDate_created(review.getDateCreated());
                    reviewDTO.setRating(review.getRating());
                    reviewDTO.setVotes(voteService.getTotalUpvotes(VoteOn.REVIEW, review.getId()) -
                            voteService.getTotalDownvotes(VoteOn.REVIEW, review.getId()));
                    reviewDTO.setCommentCount(commentRepository.countByReviewId(review.getId()));

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

                    ratingList.add(review.getRating());
                    priceList.add(review.getPrice());

                    // Get User details
                    UserDTO userDTO = new UserDTO();
                    userDTO.setUser_id(review.getUser().getId());
                    userDTO.setUsername(review.getUser().getUsername());

                    if (!review.getUser().getProfileImageDirectory().isEmpty())
                    userDTO.setUserImage(
                            ImageUtil.loadImage(
                                    review.getUser().getProfileImageDirectory()
                            )
                    );

                    reviewDTO.setUser(userDTO);

                    // Retrieve ALL images from review
                    List<byte[]> images = new ArrayList<>();
                    for (ReviewImage img : review.getImages()) {
                        byte[] file = ImageUtil.loadImage(img.getImageDirectory());
                        images.add(file);
                    }
                    reviewDTO.setImages(images);
                    list.add(reviewDTO);
                }
                list.sort(Comparator.comparing(ReviewDTO::getDate_created).reversed());
                productDTO.setReviews(list);

                OptionalDouble averageRating = ratingList
                        .stream()
                        .mapToDouble(a -> a)
                        .average();

                productDTO.setRating(averageRating.getAsDouble());

                BigDecimal averagePrice = priceList.stream()
                        .reduce(BigDecimal.ZERO, BigDecimal::add)
                        .divide(BigDecimal.valueOf(ratingList.size()), RoundingMode.HALF_UP);

                Optional<BigDecimal> minPrice = priceList.stream()
                        .min(BigDecimal::compareTo);

                Optional<BigDecimal> maxPrice = priceList.stream()
                        .max(BigDecimal::compareTo);

                productDTO.setAveragePrice(averagePrice);
                productDTO.setMaxPrice(maxPrice.get());
                productDTO.setMinPrice(minPrice.get());
            }
        } catch (Exception e) {
            throw new WeviewAPIException(HttpStatus.BAD_REQUEST, e.getMessage());
        }

        return new ResponseEntity<>(productDTO, HttpStatus.OK);
    }
}
