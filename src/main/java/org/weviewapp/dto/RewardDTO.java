package org.weviewapp.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Data
public class RewardDTO {
    private UUID id;
    private String name;
    private Integer points;
    private Integer codeCount;

    private byte[] image;

    // Upload
    private List<String> codes;
    private MultipartFile uploadedImage;
}
