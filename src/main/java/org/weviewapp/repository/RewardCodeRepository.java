package org.weviewapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.weviewapp.entity.RewardCode;

import java.util.Optional;
import java.util.UUID;

public interface RewardCodeRepository extends JpaRepository<RewardCode, UUID> {
    Optional<RewardCode> findFirstByRewardIdAndUserIdIsNull(UUID id);
    Integer countByRewardIdAndUser_IdIsNull(UUID id);
}
