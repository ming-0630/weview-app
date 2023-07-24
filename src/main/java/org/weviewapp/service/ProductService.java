package org.weviewapp.service;

import org.weviewapp.dto.ProductDTO;
import org.weviewapp.entity.Product;

import java.util.List;

public interface ProductService {
    List<ProductDTO> mapToPreviewDTO(List<Product> product);
    ProductDTO mapToEditProductDTO(Product product);
    Boolean hasExceededFeaturedLimit();
    void removeOldestFeaturedProduct();
}
