package org.weviewapp.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

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
@ToString

public class Product {
    public enum Category {
        SMARTPHONES,
        COMPUTERS,
        HOMEAPPLIANCES,
        MUSIC,
    }

    @Id
    @Column(name="product_id")
    private @Getter @Setter UUID id;

    @Column(name="name")
    private @Getter @Setter String name;

    @Enumerated(EnumType.STRING)
    @Column(name="category")
    private @Getter @Setter Category type;

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

    @PrePersist
    protected void onCreate() {
        updated = created = new Date();
    }

    @PreUpdate
    protected void onUpdate() {
        updated = new Date();
    }

    @JsonManagedReference
    @OneToMany(mappedBy = "product", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private List<ProductImage> images = new ArrayList<>();

    @OneToMany(mappedBy = "product", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private List<Review> reviews = new ArrayList<>();
}
