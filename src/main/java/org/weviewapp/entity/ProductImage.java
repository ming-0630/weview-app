package org.weviewapp.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Data
@Table(name="product_image")
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ProductImage {
    @Id
    @Column(name="product_image_id")
    private @Getter
    @Setter UUID id;

    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @Column(name="image_dir")
    private @Getter @Setter String imageDirectory;
}
