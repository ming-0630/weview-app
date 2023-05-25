package org.weviewapp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.weviewapp.dto.ProductDTO;
import org.weviewapp.entity.Product;
import org.weviewapp.entity.ProductImage;
import org.weviewapp.enums.ImageCategory;
import org.weviewapp.exception.WeviewAPIException;
import org.weviewapp.repository.ProductRepository;
import org.weviewapp.service.ProductService;
import utils.ImageUtil;

import java.util.*;

@RestController
@CrossOrigin
@RequestMapping("/api/product")
public class ProductController {
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
        product.setType(productDto.getCategory());
        product.setReleaseYear(productDto.getReleaseYear());
        product.setDescription(productDto.getDescription());

        for (MultipartFile image: productDto.getImages()) {
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

    @GetMapping("/getPreview")
    public ResponseEntity<?> getPreview() {
        List<Product> productList = productRepository.findAll();
        List<ProductDTO> productDTOList = new ArrayList<>();
        for (Product product: productList) {
            ProductDTO productDTO = new ProductDTO();

            try {
                productDTO.setCoverImage(
                        ImageUtil.loadImage(product.getImages().get(0).getImageDirectory())
                );
            } catch (Exception e) {
                throw new WeviewAPIException(HttpStatus.BAD_REQUEST, e.getMessage());
            }

            productDTO.setProductId(product.getId());
            productDTO.setName(product.getName());
            productDTO.setCategory(product.getType());
            productDTO.setReleaseYear(product.getReleaseYear());
            productDTO.setDescription(product.getDescription());
            
            productDTOList.add(productDTO);
        }

        return new ResponseEntity<>(productDTOList, HttpStatus.OK);
    }
}
