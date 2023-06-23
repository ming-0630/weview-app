package org.weviewapp.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.weviewapp.dto.RedemptionResponseDTO;
import org.weviewapp.dto.RewardDTO;
import org.weviewapp.entity.Reward;
import org.weviewapp.entity.RewardCode;
import org.weviewapp.entity.User;
import org.weviewapp.enums.ImageCategory;
import org.weviewapp.exception.WeviewAPIException;
import org.weviewapp.repository.RewardCodeRepository;
import org.weviewapp.repository.RewardRepository;
import org.weviewapp.service.RewardService;
import org.weviewapp.service.UserService;
import org.weviewapp.utils.EncryptionUtil;
import org.weviewapp.utils.ImageUtil;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class RewardServiceImpl implements RewardService {
    @Autowired
    private EncryptionUtil encryptionUtil;
    @Autowired
    private RewardRepository rewardRepository;
    @Autowired
    private RewardCodeRepository rewardCodeRepository;
    @Autowired
    private UserService userService;
    @Override
    public Reward addReward(RewardDTO reward) {
        if (reward.getCodes().isEmpty()) {
            throw new WeviewAPIException(HttpStatus.BAD_REQUEST, "Cannot have a reward with no codes!");
        }

        if (reward.getName().isEmpty() || reward.getPoints().equals(0)) {
            throw new WeviewAPIException(HttpStatus.BAD_REQUEST, "Empty fields!");
        }

        if (reward.getUploadedImage().isEmpty()) {
            throw new WeviewAPIException(HttpStatus.BAD_REQUEST, "No uploaded images!");
        }

        Reward newReward = new Reward();
        newReward.setId(UUID.randomUUID());
        newReward.setName(reward.getName());
        newReward.setPoints(reward.getPoints());
        newReward.setImageDir(ImageUtil.uploadImage(reward.getUploadedImage(), ImageCategory.REWARD_IMG));
//        List<RewardCode> codeList = new ArrayList<>();

        for(String code: reward.getCodes()) {
            RewardCode rc = new RewardCode();
            rc.setId(UUID.randomUUID());
            rc.setEncryptedCode(encryptionUtil.encrypt(code));
            rc.setReward(newReward);
            newReward.getRewardCodeList().add(rc);
        }
//        newReward.setRewardCodeList(codeList);
        return rewardRepository.save(newReward);
    }

    @Override
    public RedemptionResponseDTO redeemReward(UUID rewardId) {
        User user = userService.getCurrentUser();
        Optional<Reward> reward = rewardRepository.findById(rewardId);

        if (reward.isEmpty()) {
            throw new WeviewAPIException(HttpStatus.BAD_REQUEST, "Unable to find reward");
        }

        if (user.getPoints() < reward.get().getPoints()) {
            throw new WeviewAPIException(HttpStatus.BAD_REQUEST, "Insufficient points!");
        }

        Optional<RewardCode> rewardCode = rewardCodeRepository.findFirstByUserIdIsNull();

        if (rewardCode.isEmpty()) {
            throw new WeviewAPIException(HttpStatus.BAD_REQUEST, "No codes are left!");
        }

        rewardCode.get().setUser(user);
        rewardCode.get().setDateRedeemed(LocalDateTime.now());
        rewardCodeRepository.save(rewardCode.get());

        User updatedUser = userService.modifyPoints(user.getId(), -reward.get().getPoints());

        RedemptionResponseDTO response = new RedemptionResponseDTO();
        response.setUser(userService.mapUserToDTO(updatedUser));
        response.setReward(mapToRewardDTO(rewardRepository.findById(rewardId).get()));
        response.setCode(decryptCode(rewardCode.get().getEncryptedCode()));

        return response;
    }

    @Override
    public RewardDTO mapToRewardDTO(Reward reward) {
        RewardDTO dto = new RewardDTO();
        dto.setName(reward.getName());
        dto.setId(reward.getId());
        dto.setPoints(reward.getPoints());
        dto.setCodeCount(reward.getRewardCodeList().size());
        try {
            dto.setImage(ImageUtil.loadImage(reward.getImageDir()));
        } catch (Exception ex) {
            throw new WeviewAPIException(HttpStatus.BAD_REQUEST, ex.getMessage());
        }

        return dto;
    }

    private String decryptCode(String encryptedCode) {
        return encryptionUtil.decrypt(encryptedCode);
    }
}
