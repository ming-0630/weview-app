package org.weviewapp.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.weviewapp.entity.Product;
import org.weviewapp.enums.ProductCategory;

import java.util.List;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID> {
    Page<Product> findByCategory(ProductCategory category, Pageable pageable);
    Page<Product> findByNameContainingIgnoreCase(String keyword, Pageable pageable);
    Page<Product> findByNameContainingIgnoreCaseAndCategory(String keyword, ProductCategory category, Pageable pageable);
    @Query("SELECT p FROM Product p JOIN p.reviews r GROUP BY p ORDER BY AVG(r.rating) DESC")
    Page<Product> findAllByOrderByAverageRatingDesc( Pageable pageable);
    @Query("SELECT p FROM Product p JOIN p.reviews r GROUP BY p ORDER BY AVG(r.rating) ASC")
    Page<Product> findAllByOrderByAverageRatingAsc( Pageable pageable);
    @Query("SELECT p FROM Product p JOIN p.reviews r WHERE p.category = :category GROUP BY p ORDER BY AVG(r.rating) DESC")
    Page<Product> findByCategoryOrderByAverageRatingDesc(ProductCategory category, Pageable pageable);
    @Query("SELECT p FROM Product p JOIN p.reviews r WHERE p.category = :category GROUP BY p ORDER BY AVG(r.rating) ASC")
    Page<Product> findByCategoryOrderByAverageRatingAsc(ProductCategory category, Pageable pageable);
    @Query("SELECT p FROM Product p JOIN p.reviews r WHERE LOWER(p.name) LIKE %:keyword% " +
            "GROUP BY p ORDER BY AVG(r.rating) DESC")
    Page<Product> findByNameContainingIgnoreCaseOrderByAverageRatingDesc(String keyword, Pageable pageable);
    @Query("SELECT p FROM Product p JOIN p.reviews r WHERE LOWER(p.name) LIKE %:keyword% " +
            "GROUP BY p ORDER BY AVG(r.rating) ASC")
    Page<Product> findByNameContainingIgnoreCaseOrderByAverageRatingAsc(String keyword, Pageable pageable);
    @Query("SELECT p FROM Product p JOIN p.reviews r WHERE LOWER(p.name) LIKE %:keyword% " +
            "AND p.category = :category GROUP BY p ORDER BY AVG(r.rating) DESC")
    Page<Product> findByNameContainingIgnoreCaseAndCategoryOrderByAverageRatingDesc(String keyword, ProductCategory category, Pageable pageable);

    @Query("SELECT p FROM Product p JOIN p.reviews r WHERE LOWER(p.name) LIKE %:keyword% " +
            "AND p.category = :category GROUP BY p ORDER BY AVG(r.rating) ASC")
    Page<Product> findByNameContainingIgnoreCaseAndCategoryOrderByAverageRatingAsc(String keyword, ProductCategory category, Pageable pageable);
    List<Product> findByName(String name);
}
