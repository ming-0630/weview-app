package org.weviewapp.dto;

import lombok.Data;
import org.weviewapp.enums.ProductCategory;

@Data
public class SearchProductDTO {
    private String keyword;
    private ProductCategory category;
}
