package org.weviewapp.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class CodeDTO {
    private UUID id;
    private String code;
    private String userEmail;
    private LocalDateTime dateRedeemed;
}
