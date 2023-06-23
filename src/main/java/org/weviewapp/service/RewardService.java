package org.weviewapp.service;

import org.weviewapp.dto.RewardDTO;
import org.weviewapp.dto.RedemptionResponseDTO;
import org.weviewapp.entity.Reward;

import java.util.UUID;

public interface RewardService {
    public Reward addReward(RewardDTO reward);
    public RedemptionResponseDTO redeemReward(UUID rewardId);
    public RewardDTO mapToRewardDTO(Reward reward);
}
