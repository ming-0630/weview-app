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
import org.weviewapp.dto.ProductDTO;
import org.weviewapp.dto.ProductResponseDTO;
import org.weviewapp.dto.ReviewDTO;
import org.weviewapp.entity.Product;
import org.weviewapp.entity.ProductImage;
import org.weviewapp.entity.Review;
import org.weviewapp.enums.ImageCategory;
import org.weviewapp.enums.ProductCategory;
import org.weviewapp.exception.WeviewAPIException;
import org.weviewapp.repository.*;
import org.weviewapp.service.*;
import org.weviewapp.utils.ImageUtil;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@CrossOrigin
@RequestMapping("/api/product")
public class ProductController {
    private static final int ITEMS_PER_PAGE = 10;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ProductService productService;
    @Autowired
    private CommentRepository commentRepository;
    @Autowired
    private ReviewRepository reviewRepository;
    @Autowired
    private ProductImageRepository productImageRepository;
    @Autowired
    private VoteService voteService;
    @Autowired
    private ReviewService reviewService;
    @Autowired
    private WatchlistService watchlistService;
    @Autowired
    private UserService userService;

    @GetMapping("/admin/add/checkFeaturedLimit")
    public ResponseEntity<?> checkFeatured(@ModelAttribute ProductDTO productDto) {
        Map<String, Object> response = new HashMap<>();
        response.put("hasExceededFeaturedLimit", productService.hasExceededFeaturedLimit());

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
    @PostMapping("/admin/add")
    public ResponseEntity<?> addProduct(@ModelAttribute ProductDTO productDto) {
        // add check for username exists in database
        if(!productRepository.findByName(productDto.getName()).isEmpty()){
            throw new WeviewAPIException(HttpStatus.BAD_REQUEST, "Product Name exists!");
        }

        if (productDto.getName().isBlank()) {
            throw new WeviewAPIException(HttpStatus.BAD_REQUEST, "Missing Product Name");
        }

        if (productDto.getCategory() == null) {
            throw new WeviewAPIException(HttpStatus.BAD_REQUEST, "Missing Product Category");
        }

        if (productDto.getDescription().isBlank()) {
            throw new WeviewAPIException(HttpStatus.BAD_REQUEST, "Missing Product Description");
        }

        if (productDto.getMinProductPriceRange() == null) {
            throw new WeviewAPIException(HttpStatus.BAD_REQUEST, "Missing Product Min Price Range");
        }

        if (productDto.getMaxProductPriceRange() == null) {
            throw new WeviewAPIException(HttpStatus.BAD_REQUEST, "Missing Product Max Price Range");
        }

        Product product = new Product();
        product.setProductId(UUID.randomUUID());
        product.setName(productDto.getName());
        product.setCategory(productDto.getCategory());
        product.setReleaseYear(productDto.getReleaseYear());
        product.setDescription(productDto.getDescription());
        product.setMinProductPriceRange(productDto.getMinProductPriceRange());
        product.setMaxProductPriceRange(productDto.getMaxProductPriceRange());

        if(productDto.getIsFeatured() && productService.hasExceededFeaturedLimit()) {
            productService.removeOldestFeaturedProduct();
            product.setFeaturedDate(LocalDateTime.now());
            product.setIsFeatured(true);
        } else {
            product.setFeaturedDate(LocalDateTime.now());
            product.setIsFeatured(true);
        }

        if(productDto.getUploadedImages().isEmpty()) {
            throw new WeviewAPIException(HttpStatus.BAD_REQUEST, "No images attached!");
        }

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

    @PostMapping("/admin/edit")
    public ResponseEntity<?> editProduct(@ModelAttribute ProductDTO productDto) {
        // add check for username exists in database
        List<Product> products = productRepository.findByName(productDto.getName());
        if(!products.isEmpty()){
            boolean nameExists = products.stream()
                    .anyMatch(product -> product.getName().equals(productDto.getName()) && !product.getProductId().equals(productDto.getProductId()));

            if (nameExists) {
                throw new WeviewAPIException(HttpStatus.BAD_REQUEST, "Product Name exists!");
            }
        }

        Optional<Product> product = productRepository.findById(productDto.getProductId());
        if (product.isEmpty()) {
            throw new WeviewAPIException(HttpStatus.BAD_REQUEST, "Cannot find product!");
        }

        product.get().setName(productDto.getName());
        product.get().setCategory(productDto.getCategory());
        product.get().setReleaseYear(productDto.getReleaseYear());
        product.get().setDescription(productDto.getDescription());
        product.get().setMinProductPriceRange(productDto.getMinProductPriceRange());
        product.get().setMaxProductPriceRange(productDto.getMaxProductPriceRange());

        if(productDto.getIsFeatured() && productService.hasExceededFeaturedLimit()) {
            productService.removeOldestFeaturedProduct();
            product.get().setFeaturedDate(LocalDateTime.now());
            product.get().setIsFeatured(true);
        } else {
            if (productDto.getIsFeatured()) {
                product.get().setFeaturedDate(LocalDateTime.now());
                product.get().setIsFeatured(true);
            } else {
                product.get().setFeaturedDate(null);
                product.get().setIsFeatured(false);
            }
        }

        if(productDto.getUploadedImages().isEmpty()) {
            throw new WeviewAPIException(HttpStatus.BAD_REQUEST, "No images attached!");
        }

        // Clear prev images
        if (!product.get().getImages().isEmpty()) {
            for (ProductImage image: product.get().getImages()) {
                // Prevent deleting default images from initDatabase as the image are shared
                if (!image.getImageDirectory().equals("PRODUCT_IMG_9d8c272a-f1b5-4bb0-bac7-0dc76d88bc65.png") &&
                        !image.getImageDirectory().equals("PRODUCT_IMG_70b8d218-72ba-4bec-98fa-69ce70801750.png") &&
                        !image.getImageDirectory().equals("PRODUCT_IMG_fc772fcc-88f7-40a1-9cb3-9f2edd67a841.png") &&
                        !image.getImageDirectory().equals("PRODUCT_IMG_f60dcc58-3230-442d-933c-67854bfbb061.png") &&
                        !image.getImageDirectory().equals("PRODUCT_IMG_0973d924-d55c-45e7-9335-57773f6a8cd0.jpg") &&
                        !image.getImageDirectory().equals("PRODUCT_IMG_c1c0dc33-13f0-4ccf-89cf-cf04a85662f1.png") &&
                        !image.getImageDirectory().equals("PRODUCT_IMG_86b37a2d-5f27-4df9-a73c-92ebe4146a71.png") &&
                        !image.getImageDirectory().equals("PRODUCT_IMG_21224d46-de75-48a2-acec-5aa1238c7a44.jpg") &&
                        !image.getImageDirectory().equals("PRODUCT_IMG_3166aecd-cafc-4069-899b-d1c300e234a3.jpg") &&
                        !image.getImageDirectory().equals("PRODUCT_IMG_53f4ed65-80af-4e6d-be8d-908acd7f22cb.png") &&
                        !image.getImageDirectory().equals("PRODUCT_IMG_0520b7ad-8c1a-4620-a69a-406b496786a6.png") &&
                        !image.getImageDirectory().equals("PRODUCT_IMG_78772259-4d4f-4d50-af06-cc97e8e19501.jpg")
                ) {
                    // Only delete if the directory is not default images
                    ImageUtil.deleteImage(image.getImageDirectory());
                }
            }
            product.get().getImages().clear();
        }

        for (MultipartFile image: productDto.getUploadedImages()) {
            ProductImage newImage = new ProductImage();
            newImage.setId(UUID.randomUUID());
            newImage.setProduct(product.get());

            String imgDir = ImageUtil.uploadImage(image, ImageCategory.PRODUCT_IMG);
            newImage.setImageDirectory(imgDir);
            product.get().getImages().add(newImage);
        }

        Product addedProduct = productRepository.save(product.get());

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Edited successfully!");
        response.put("product", addedProduct);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/getAllFeaturedProducts")
    public ResponseEntity<?> getAllFeaturedProducts() {
        List<Product> featured = productRepository.findByIsFeaturedIsTrueOrderByFeaturedDateDesc();

        List<ProductDTO> result = productService.mapToPreviewDTO(featured);

        return new ResponseEntity<>(result, HttpStatus.OK);
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

        Page<Product> productList = null;
        if (sortBy.equals("rating")) {
            Pageable pageable = PageRequest.of(pageNum - 1, ITEMS_PER_PAGE);

            if(sortDirection.isAscending()) {
                productList = productRepository.findAllByOrderByAverageRatingAsc(pageable);
            } else {
                productList = productRepository.findAllByOrderByAverageRatingDesc(pageable);
            }

        } else {
            // Pageable starts page at 0, while front end starts at 1
            Pageable pageable = PageRequest.of(pageNum - 1, ITEMS_PER_PAGE, sortDirection, sortBy);
           productList = productRepository.findAll(pageable);
        }

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

    @GetMapping("/admin/getAllUnpaged")
    public ResponseEntity<?> getAllUnpaged() {
        List<Product> productList = productRepository.findAll();
        if (productList.isEmpty()) {
            throw new WeviewAPIException(HttpStatus.BAD_REQUEST, "No products found!");
        }

        return new ResponseEntity<>(productService.mapToPreviewDTO(productList), HttpStatus.OK);
    }

    @GetMapping("/admin/getProductToEdit")
    public ResponseEntity<?> getOneProduct(@RequestParam String productId) {

        Optional<Product> product = productRepository.findById(UUID.fromString(productId));
        if (product.isEmpty()) {
            throw new WeviewAPIException(HttpStatus.BAD_REQUEST, "Cannot find this product!");
        }

        return new ResponseEntity<>(productService.mapToEditProductDTO(product.get()), HttpStatus.OK);
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

        Page<Product> productList = null;
        if (sortBy.equals("rating")) {
            Pageable pageable = PageRequest.of(pageNum - 1, ITEMS_PER_PAGE);

            if(sortDirection.isAscending()) {
                productList = productRepository.findByCategoryOrderByAverageRatingAsc(category, pageable);
            } else {
                productList = productRepository.findByCategoryOrderByAverageRatingDesc(category, pageable);
            }

        } else {
            // Pageable starts page at 0, while front end starts at 1
            Pageable pageable = PageRequest.of(pageNum - 1, ITEMS_PER_PAGE, sortDirection, sortBy);
            productList = productRepository.findByCategory(category, pageable);
        }

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
        if (sortBy.equals("rating")) {
            Pageable pageable = PageRequest.of(pageNum - 1, ITEMS_PER_PAGE);

            if(sortDirection.isAscending()) {
                if (category == null || category.equals(ProductCategory.ALL)) {
                    productList = productRepository
                            .findByNameContainingIgnoreCaseOrderByAverageRatingAsc(keyword, pageable);
                } else {
                    productList = productRepository
                            .findByNameContainingIgnoreCaseAndCategoryOrderByAverageRatingAsc(keyword, category, pageable);
                }
            } else {

                if (category == null || category.equals(ProductCategory.ALL)) {
                    productList = productRepository
                            .findByNameContainingIgnoreCaseOrderByAverageRatingDesc(keyword, pageable);
                } else {
                    productList = productRepository
                            .findByNameContainingIgnoreCaseAndCategoryOrderByAverageRatingDesc(keyword, category, pageable);
                }
            }
        } else {
            Pageable pageable = PageRequest.of(pageNum - 1, ITEMS_PER_PAGE, sortDirection, sortBy);
            if (category == null || category.equals(ProductCategory.ALL)) {
                productList = productRepository
                        .findByNameContainingIgnoreCase(keyword, pageable);
            } else {
                productList = productRepository
                        .findByNameContainingIgnoreCaseAndCategory(keyword, category, pageable);
            }
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
    public ResponseEntity<?> getProductDetails(@RequestParam String id,
                                               @RequestParam Integer reviewPageNum,
                                               @RequestParam (defaultValue = "name") String reviewSortBy,
                                               @RequestParam (defaultValue = "asc") String reviewDirection,
                                               @RequestParam (required = false) String reviewId
    ) {
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

            if (product.get().getReviews().size() > 0) {

                if(reviewId != null) {
                    // Get specific review location
                    Pageable pageable;
                    Page<Review> pagedReview;
                    Optional<List<Review>> elements = reviewRepository.
                            findAllByIsVerifiedIsTrueAndReportIsNullAndProduct_ProductId(
                                    product.get().getProductId(), Sort.by(Sort.Direction.DESC, "dateCreated"));
                    UUID reviewIdUUID =  UUID.fromString(reviewId);

                    if (!elements.isEmpty()) {
                        long index = 0;
                        boolean foundReview = false;
                        for (Review review : elements.get()) {
                            if (review.getId().equals(reviewIdUUID)) {
                                foundReview = true;
                                break;
                            }
                            index++;
                        }
                        if (foundReview) {
                            int pageIndex = (int) (((index -1) / 5 ) + 1);
                            pageable = PageRequest.of(pageIndex - 1, 5, Sort.by(Sort.Direction.DESC, "dateCreated"));

                            pagedReview = reviewService.getReviewsByProductId(product.get().getProductId(), pageable);
                            productDTO.setCurrentReviewPage(pageIndex);
                            productDTO.setTotalReviewPage(pagedReview.getTotalPages());

                            List<ReviewDTO> reviewDTOS = reviewService.mapToReviewDTO(pagedReview.getContent());
                            productDTO.setReviews(reviewDTOS);
                        } else {
                            throw new WeviewAPIException(HttpStatus.BAD_REQUEST, "Cannot find review position!");
                        }
                    } else {
                        throw new WeviewAPIException(HttpStatus.BAD_REQUEST, "Cannot find review position!");
                    }

                } else {
                    // No specific review location
                    Sort.Direction sortDirection = Sort.Direction.ASC;

                    if(reviewDirection.equalsIgnoreCase("desc")) {
                        sortDirection = Sort.Direction.DESC;
                    }

                    Pageable pageable;
                    Page<Review> pagedReview;
                    if (reviewSortBy.equals("votes")) {
                        pageable = PageRequest.of(reviewPageNum - 1, 5);
                        pagedReview = reviewService.getReviewsByProductIdSortByVotes(product.get().getProductId(), sortDirection.toString(), pageable);
                    } else {
                        pageable = PageRequest.of(reviewPageNum - 1, 5, sortDirection, reviewSortBy);
                        pagedReview = reviewService.getReviewsByProductId(product.get().getProductId(), pageable);
                    }

                    productDTO.setTotalReviewPage(pagedReview.getTotalPages());

                    List<ReviewDTO> reviewDTOS = reviewService.mapToReviewDTO(pagedReview.getContent());
                    productDTO.setReviews(reviewDTOS);
                }


                // Get all reviews for calculations
                List<Review> list = reviewService.getAllReviewsByProductId(product.get().getProductId());
                if (!list.isEmpty()) {
                    productDTO.setRatingCount(list.size());
                    productDTO.setReviewStartDate(reviewService.getEarliestReviewDate(product.get().getProductId()));
                    productDTO.setReviewEndDate(reviewService.getLatestReviewDate(product.get().getProductId()));
                    productDTO.setRatings(reviewService.getRatings(list));

                    OptionalDouble averageRating = list
                            .stream()
                            .mapToDouble(obj -> obj.getRating())
                            .average();

                    productDTO.setRating(averageRating.getAsDouble());

                    BigDecimal averagePrice = list.stream()
                            .map(obj -> obj.getPrice())
                            .reduce(BigDecimal.ZERO, BigDecimal::add)
                            .divide(BigDecimal.valueOf(list.size()), RoundingMode.HALF_UP);

                    Optional<BigDecimal> minPrice = list.stream()
                            .map(obj -> obj.getPrice())
                            .min(BigDecimal::compareTo);

                    Optional<BigDecimal> maxPrice = list.stream()
                            .map(obj -> obj.getPrice())
                            .max(BigDecimal::compareTo);

                    productDTO.setAveragePrice(averagePrice);
                    productDTO.setMaxPrice(maxPrice.get());
                    productDTO.setMinPrice(minPrice.get());
                }
            }

        if (userService.getCurrentUser() != null) {
            // Only perform these methods if logged in
            productDTO.setWatchlisted(watchlistService.getIsWatchlisted(product.get()));

            // Get current unverified review
            Optional<Review> unverifiedReview = reviewService.getUnverifiedOrReportedReview(
                    userService.getCurrentUser().getId(), product.get().getProductId());

            if (unverifiedReview.isPresent()) {
                productDTO.setUnverifiedReview(reviewService.mapToReviewDTO(List.of(unverifiedReview.get())).get(0));
            }
        }

        return new ResponseEntity<>(productDTO, HttpStatus.OK);
    }

    @GetMapping("/get")
    public ResponseEntity<?> getProduct(@RequestParam String id) {

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

        return new ResponseEntity<>(productDTO, HttpStatus.OK);
    }
}
