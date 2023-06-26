package org.weviewapp.service;

import org.weviewapp.dto.CodeDTO;
import org.weviewapp.dto.RedemptionResponseDTO;
import org.weviewapp.dto.RewardDTO;
import org.weviewapp.entity.Reward;

import java.util.List;
import java.util.UUID;

public interface RewardService {
    public Reward addReward(RewardDTO reward);
    public Reward editReward(RewardDTO reward);
    public List<CodeDTO> getCodes(UUID rewardId);
    public void addCodes(UUID rewardId, List<String> codes);
    public RedemptionResponseDTO redeemReward(UUID rewardId);
    public RewardDTO mapToRewardDTO(Reward reward);
}
