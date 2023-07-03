package org.weviewapp.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@Table(name="reward_code")
@NoArgsConstructor
@AllArgsConstructor
public class RewardCode {
    @Id
    @GeneratedValue
    private UUID id;

    @JsonBackReference(value = "reward-rewardCode")
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne
    @Nullable
    @JoinColumn(name = "reward_id")
    private Reward reward;

    private String encryptedCode;

    @JsonBackReference(value = "user-rewardCode")
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne
    @Nullable
    @JoinColumn(name = "user_id")
    private User user;

    @Nullable
    private LocalDateTime dateRedeemed;
}
