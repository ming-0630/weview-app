package org.weviewapp.dto;

import lombok.Data;

@Data
public class JWTRefreshResponse {
    private String accessToken;
    private String refreshToken;
    private String tokenType = "Bearer";
}
