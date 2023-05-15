package org.weviewapp.service.impl;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.weviewapp.entity.RefreshToken;
import org.weviewapp.exception.RefreshTokenException;
import org.weviewapp.repository.RefreshTokenRepository;
import org.weviewapp.repository.UserRepository;
import org.weviewapp.service.RefreshTokenService;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class RefreshTokenServiceImpl implements RefreshTokenService {
    @Value("${app-jwtRefresh-expiration-milliseconds}")
    private Long refreshTokenDurationMs;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;
    @Autowired
    private UserRepository userRepository;

    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    public RefreshToken createRefreshToken(UUID userId) {
        RefreshToken refreshToken = new RefreshToken();

        refreshToken.setUser(userRepository.findById(userId).get());
        refreshToken.setExpiryDate(Instant.now().plusMillis(refreshTokenDurationMs));
        refreshToken.setToken(UUID.randomUUID().toString());

        refreshToken = refreshTokenRepository.save(refreshToken);
        return refreshToken;
    }

    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().compareTo(Instant.now()) < 0) {
            refreshTokenRepository.delete(token);
            throw new RefreshTokenException(token.getToken(), "Refresh token was expired. Please make a new signin request");
        }

        return token;
    }
    @Transactional
    public int deleteByUserId(UUID userId) {
        return refreshTokenRepository.deleteByUser(userRepository.findById(userId).get());
    }
}
