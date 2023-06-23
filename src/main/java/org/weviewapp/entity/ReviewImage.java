package org.weviewapp.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Data
@Table(name="review_image")
@NoArgsConstructor
@AllArgsConstructor
public class ReviewImage {
    @Id
    @Column(name="review_image_id")
    private UUID id;

    @JsonBackReference
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id")
    private Review review;

    @Column(name="image_dir")
    private String imageDirectory;
}
