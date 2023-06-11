package org.weviewapp.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class UserDTO {
    private UUID user_id;
    private String username;
    private byte[] userImage;
}
