package org.weviewapp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.weviewapp.dto.RewardDTO;
import org.weviewapp.entity.Reward;
import org.weviewapp.repository.RewardRepository;
import org.weviewapp.service.RewardService;

import java.util.*;

@RestController
@CrossOrigin
@RequestMapping("/api/user/reward")
public class RewardController {
    @Autowired
    private RewardService rewardService;
    @Autowired
    private RewardRepository rewardRepository;

    @PostMapping("/add")
    public ResponseEntity<?> addReward(@ModelAttribute RewardDTO reward) {
        Reward r = rewardService.addReward(reward);
        return new ResponseEntity<>(r, HttpStatus.OK);
    }

    @PostMapping("/redeem")
    public ResponseEntity<?> redeemReward(@RequestParam String rewardId) {
        return new ResponseEntity<>(rewardService.redeemReward(UUID.fromString(rewardId)), HttpStatus.OK);
    }
    @GetMapping("/getRewards")
    public ResponseEntity<?> getRewards(@RequestParam Integer pageNum) {
        Pageable pageable = PageRequest.of(pageNum - 1, 10);
        Page<Reward> r = rewardRepository.findAll(pageable);

        List<RewardDTO> dto = new ArrayList<>();
        for(Reward reward: r.getContent()) {
           RewardDTO result = rewardService.mapToRewardDTO(reward);
            dto.add(result);
        }
        Map<String, Object> response = new HashMap<>();
        response.put("pageNum", pageNum);
        response.put("totalPage", r.getTotalPages());
        response.put("rewards", dto);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}

