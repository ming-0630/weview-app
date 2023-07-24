package org.weviewapp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.weviewapp.dto.AddCodeDTO;
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

    @PostMapping("/admin/add")
    public ResponseEntity<?> addReward(@ModelAttribute RewardDTO reward) {
        Reward r = rewardService.addReward(reward);
        return new ResponseEntity<>(r, HttpStatus.OK);
    }
    @PostMapping("/admin/edit")
    public ResponseEntity<?> editReward(@ModelAttribute RewardDTO reward) {
        Reward r = rewardService.editReward(reward);
        return new ResponseEntity<>(r, HttpStatus.OK);
    }
    @GetMapping("/admin/getCodes")
    public ResponseEntity<?> getCodes(@RequestParam String rewardId) {
        return new ResponseEntity<>(rewardService.getCodes(UUID.fromString(rewardId)), HttpStatus.OK);
    }

    @PostMapping("/admin/addCodes")
    public ResponseEntity<?> addCodes(
            @RequestBody AddCodeDTO dto) {
        rewardService.addCodes(UUID.fromString(dto.getRewardId()), dto.getCodes());
        return new ResponseEntity<>("Added successfully", HttpStatus.OK);
    }

    @PostMapping("/redeem")
    public ResponseEntity<?> redeemReward(@RequestParam String rewardId) {
        return new ResponseEntity<>(rewardService.redeemReward(UUID.fromString(rewardId)), HttpStatus.OK);
    }
    @GetMapping("/getRewards")
    public ResponseEntity<?> getRewards(@RequestParam Integer pageNum,
                                        @RequestParam (defaultValue = "name") String sortBy,
                                        @RequestParam (defaultValue = "asc") String direction) {
        Sort.Direction sortDirection = Sort.Direction.ASC;

        if(direction.equalsIgnoreCase("desc")) {
            sortDirection = Sort.Direction.DESC;
        }

        Pageable pageable = PageRequest.of(pageNum - 1, 10, sortDirection, sortBy);
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

