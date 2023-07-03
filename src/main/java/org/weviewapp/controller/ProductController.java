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
import org.weviewapp.repository.CommentRepository;
import org.weviewapp.repository.ProductImageRepository;
import org.weviewapp.repository.ProductRepository;
import org.weviewapp.repository.UserRepository;
import org.weviewapp.service.*;
import org.weviewapp.utils.ImageUtil;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
    private ProductImageRepository productImageRepository;
    @Autowired
    private VoteService voteService;
    @Autowired
    private ReviewService reviewService;
    @Autowired
    private WatchlistService watchlistService;
    @Autowired
    private UserService userService;

    @PostMapping("/add")
    public ResponseEntity<?> addProduct(@ModelAttribute ProductDTO productDto) {
        // add check for username exists in database
        if(!productRepository.findByName(productDto.getName()).isEmpty()){
            throw new WeviewAPIException(HttpStatus.BAD_REQUEST, "Product Name exists!");
        }

        Product product = new Product();
        product.setProductId(UUID.randomUUID());
        product.setName(productDto.getName());
        product.setCategory(productDto.getCategory());
        product.setReleaseYear(productDto.getReleaseYear());
        product.setDescription(productDto.getDescription());

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

    @PostMapping("/edit")
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

        if(productDto.getUploadedImages().isEmpty()) {
            throw new WeviewAPIException(HttpStatus.BAD_REQUEST, "No images attached!");
        }

        // Clear prev images
        if (!product.get().getImages().isEmpty()) {
            for (ProductImage image: product.get().getImages()) {
                ImageUtil.deleteImage(image.getImageDirectory());
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

    @GetMapping("/getAllUnpaged")
    public ResponseEntity<?> getAllUnpaged() {

        List<Product> productList = productRepository.findAll();
        if (productList.isEmpty()) {
            throw new WeviewAPIException(HttpStatus.BAD_REQUEST, "No products found!");
        }

        return new ResponseEntity<>(productService.mapToPreviewDTO(productList), HttpStatus.OK);
    }

    @GetMapping("/getProductToEdit")
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
                                               @RequestParam (defaultValue = "asc") String reviewDirection
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
        productDTO.setWatchlisted(watchlistService.getIsWatchlisted(product.get()));

            if (product.get().getReviews().size() > 0) {
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

                // Get all reviews first
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

        // Get current unverified review
        Optional<Review> unverifiedReview = reviewService.getUnverifiedOrReportedReview(
                    userService.getCurrentUser().getId(), product.get().getProductId());

            if (unverifiedReview.isPresent()) {
                productDTO.setUnverifiedReview(reviewService.mapToReviewDTO(List.of(unverifiedReview.get())).get(0));
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
