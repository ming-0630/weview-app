package org.weviewapp.entity;


import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Data
@Table(name="reward")
@NoArgsConstructor
@AllArgsConstructor
public class Reward {
    @Id
    @GeneratedValue
    @Column(name="reward_id")
    private UUID id;

    private String name;
    private int points;

    @JsonManagedReference
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @Nullable
    @OneToMany(mappedBy = "reward", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private List<RewardCode> rewardCodeList = new ArrayList<>();

    private String imageDir;
}
