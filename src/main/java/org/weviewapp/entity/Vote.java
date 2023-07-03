package org.weviewapp.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import lombok.*;
import org.weviewapp.enums.VoteType;

import java.util.UUID;

@Entity
@Data
@Table(name="vote")
@NoArgsConstructor
@AllArgsConstructor
public class Vote {
    @Id
    @Column(name="vote_id")
    private UUID id;

    @JsonBackReference(value = "review-vote")
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne
    @Nullable
    @JoinColumn(name = "review_id")
    private Review review;

    @JsonBackReference(value = "comment-vote")
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne
    @Nullable
    @JoinColumn(name = "comment_id")
    private Comment comment;

    @JsonBackReference(value = "user-vote")
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    private VoteType voteType;
}


