package org.weviewapp.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import lombok.*;
import org.weviewapp.enums.ProductCategory;

import java.time.LocalDateTime;
import java.time.Year;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Entity
@Data
@Table(name="product")
@NoArgsConstructor
@AllArgsConstructor
public class Product {
    @Id
    @Column(name="product_id")
    private @Getter @Setter UUID productId;

    @Column(name="name")
    private @Getter @Setter String name;

    @Enumerated(EnumType.STRING)
    @Column(name="category")
    private @Getter @Setter ProductCategory category;

    @Column(name="release_year")
    private @Getter @Setter Year releaseYear;

    @Column(name="description", columnDefinition = "TEXT")
    private @Getter @Setter String description;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "date_created", nullable = false)
    private Date created;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "date_updated", nullable = false)
    private Date updated;

    private Integer minProductPriceRange;

    private Integer maxProductPriceRange;

    private Boolean isFeatured;

    private LocalDateTime featuredDate;
    @PrePersist
    protected void onCreate() {
        updated = created = new Date();
    }
    @PreUpdate
    protected void onUpdate() {
        updated = new Date();
    }

    @JsonManagedReference(value = "product-productImage")
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @Nullable
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductImage> images = new ArrayList<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @Nullable
    private List<Review> reviews = new ArrayList<>();
}
