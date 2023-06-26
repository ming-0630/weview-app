package org.weviewapp.dto;

import lombok.Data;

import java.util.List;

@Data
public class AddCodeDTO {
    private List<String> codes;
    private String rewardId;
}
