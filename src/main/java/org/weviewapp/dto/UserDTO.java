package org.weviewapp.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Data
public class UserDTO {
    private UUID id;
    private String username;
    private byte[] userImage;
    private Boolean isVerified;
    private Integer points;

    // For upload
    private MultipartFile uploadedImage;
}
