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
import org.weviewapp.entity.Product;
import org.weviewapp.entity.ProductImage;
import org.weviewapp.enums.ImageCategory;
import org.weviewapp.enums.ProductCategory;
import org.weviewapp.exception.WeviewAPIException;
import org.weviewapp.repository.ProductRepository;
import org.weviewapp.service.ProductService;
import org.weviewapp.utils.ImageUtil;

import java.util.*;

@RestController
@CrossOrigin
@RequestMapping("/api/product")
public class ProductController {
    private static final int ITEMS_PER_PAGE = 5;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private ProductService productService;

    @PostMapping("/add")
    public ResponseEntity<?> addProduct(@ModelAttribute ProductDTO productDto) {
        // add check for username exists in database
        if(productRepository.existsByName(productDto.getName())){
            throw new WeviewAPIException(HttpStatus.BAD_REQUEST, "Product Name exists");
        }

        Product product = new Product();
        product.setId(UUID.randomUUID());
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

//    @GetMapping("/search")
//    public ResponseEntity<?> searchProducts(@RequestParam("keyword") String keyword) {
//        Optional<List<Product>> productList = productRepository.findByNameContainingIgnoreCase(keyword);
//        productRepository.findAll(Specification.where())
//        if (productList.isEmpty()) {
//            throw new WeviewAPIException(HttpStatus.BAD_REQUEST, "No products found with the keyword " + keyword);
//        }
//        List<ProductDTO> result = productService.mapToDTO(productList.get());
//
//        return new ResponseEntity<>(result, HttpStatus.OK);
//    }

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
    public ResponseEntity<?> searchProducts(@RequestParam String id) {

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

        productDTO.setProductId(product.get().getId());
        productDTO.setName(product.get().getName());
        productDTO.setCategory(product.get().getCategory());
        productDTO.setReleaseYear(product.get().getReleaseYear());
        productDTO.setDescription(product.get().getDescription());
        productDTO.setDate_created(product.get().getCreated());
        productDTO.setDate_updated(product.get().getUpdated());

        return new ResponseEntity<>(productDTO, HttpStatus.OK);
    }
}
