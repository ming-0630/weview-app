package org.weviewapp.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
@Entity
@Data
@Table(name="comment")
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Comment {
    @Id
    @Column(name="comment_id")
    private UUID id;

    @Column(name="text")
    private String text;

    @JsonManagedReference
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @Nullable
    @OneToMany(mappedBy = "comment")
    private List<Vote> votes = new ArrayList<>();

    @JsonBackReference
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne
    @JoinColumn(name = "review_id")
    private Review review;

    @JsonBackReference
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

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


