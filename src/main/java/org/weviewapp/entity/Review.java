package org.weviewapp.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Data
@Table(name="review")
@NoArgsConstructor
@AllArgsConstructor
//@SQLDelete(sql = "UPDATE review SET deleted = true WHERE review_id=?")
@ToString
public class Review {
    @Id
    @Column(name="review_id")
    private UUID id;

    @JsonBackReference
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @Column(name="rating")
    private Integer rating;

    @Column(name="title")
    private String title;

    @Column(name="price")
    private BigDecimal price;

    @Column(name="description", columnDefinition = "TEXT")
    private String description;

    @JsonManagedReference
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @Nullable
    @OneToMany(mappedBy = "review")
    private List<Vote> votes = new ArrayList<>();

    @JsonManagedReference
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @Nullable
    @OneToMany(mappedBy = "review")
    private List<Comment> comments = new ArrayList<>();

    @JsonManagedReference
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @Nullable
    @OneToMany(mappedBy = "review", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private List<ReviewImage> images = new ArrayList<>();

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "user_id")
    private User user;

//    private boolean deleted = Boolean.FALSE;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "date_created", nullable = false)
    private LocalDateTime dateCreated;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "date_updated", nullable = false)
    private LocalDateTime dateUpdated;

    @PrePersist
    protected void onCreate() {
        if(dateCreated == null) {
            dateUpdated = dateCreated = LocalDateTime.now();
            return;
        }

        dateUpdated = dateCreated;
    }

    @PreUpdate
    protected void onUpdate() {
        dateUpdated = LocalDateTime.now();
    }

}
