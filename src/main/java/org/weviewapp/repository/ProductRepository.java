package org.weviewapp.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.weviewapp.entity.Product;
import org.weviewapp.enums.ProductCategory;

import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID> {
    Page<Product> findByCategory(ProductCategory category, Pageable pageable);
    Page<Product> findByNameContainingIgnoreCase(String keyword, Pageable pageable);
    Page<Product> findByNameContainingIgnoreCaseAndCategory(String keyword, ProductCategory category, Pageable pageable);
    Boolean existsByName(String name);
}
