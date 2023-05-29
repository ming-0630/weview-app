package org.weviewapp.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.weviewapp.dto.ProductDTO;
import org.weviewapp.entity.Product;
import org.weviewapp.exception.WeviewAPIException;
import org.weviewapp.repository.ProductRepository;
import org.weviewapp.service.ProductService;
import org.weviewapp.utils.ImageUtil;

import java.util.ArrayList;
import java.util.List;

@Service
public class ProductServiceImpl implements ProductService {
    @Autowired
    private ProductRepository productRepository;
    @Override
    public String handleImages(MultipartFile image) {

        return "";
    }
    @Override
    public List<ProductDTO> mapToPreviewDTO(List<Product> products) {
        List<ProductDTO> productDTOList = new ArrayList<>();
        for (Product product : products) {
            ProductDTO productDTO = new ProductDTO();

            try {
                if (product.getImages() != null) {
                    productDTO.setCoverImage(
                            ImageUtil.loadImage(product.getImages().get(0).getImageDirectory())
                    );
                }
            } catch (Exception e) {
                throw new WeviewAPIException(HttpStatus.BAD_REQUEST, e.getMessage());
            }

            productDTO.setProductId(product.getId());
            productDTO.setName(product.getName());
            productDTO.setCategory(product.getCategory());
            productDTO.setReleaseYear(product.getReleaseYear());
            productDTO.setDescription(product.getDescription());
            productDTO.setDate_created(product.getCreated());
            productDTO.setDate_updated(product.getUpdated());

            productDTOList.add(productDTO);
        }
        return productDTOList;
    }
}
