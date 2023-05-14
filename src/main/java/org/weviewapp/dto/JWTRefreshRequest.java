package org.weviewapp.dto;

import lombok.Data;

@Data
public class JWTRefreshRequest {
    private String refreshToken;
}
