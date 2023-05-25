package org.weviewapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.weviewapp.entity.Product;

import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID> {
    Boolean existsByName(String name);
}
