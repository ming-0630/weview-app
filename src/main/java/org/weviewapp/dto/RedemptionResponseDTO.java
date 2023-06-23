package org.weviewapp.dto;

import lombok.Data;

@Data
public class RedemptionResponseDTO {
    private UserDTO user;
    private RewardDTO reward;
    private String code;
}
