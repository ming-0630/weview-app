package org.weviewapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.weviewapp.entity.ProductImage;

import java.util.UUID;

public interface ProductImageRepository extends JpaRepository<ProductImage, UUID> {
}
