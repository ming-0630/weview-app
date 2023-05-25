package org.weviewapp.service;

import org.springframework.web.multipart.MultipartFile;

public interface ProductService {
//    String addProduct(ProductDto productDto);
    String handleImages(MultipartFile images);
}
