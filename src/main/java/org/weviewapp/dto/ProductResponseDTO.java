package org.weviewapp.dto;

import lombok.Data;

import java.util.List;

@Data
public class ProductResponseDTO {
    private List<ProductDTO> productDTOs;
    private Integer currentPage;
    private Integer totalPages;
}
