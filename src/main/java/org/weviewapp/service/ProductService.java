package org.weviewapp.service;

import org.springframework.web.multipart.MultipartFile;
import org.weviewapp.dto.ProductDTO;
import org.weviewapp.entity.Product;

import java.util.List;

public interface ProductService {
//    String addProduct(ProductDto productDto);
    String handleImages(MultipartFile images);

    List<ProductDTO> mapToPreviewDTO(List<Product> product);
}
