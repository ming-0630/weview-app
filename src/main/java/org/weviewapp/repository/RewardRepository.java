package org.weviewapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.weviewapp.entity.Reward;

import java.util.UUID;

public interface RewardRepository extends JpaRepository<Reward, UUID> {
}
