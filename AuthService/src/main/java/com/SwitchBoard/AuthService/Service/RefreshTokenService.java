package com.SwitchBoard.AuthService.Service;

import com.SwitchBoard.AuthService.Model.Account;
import com.SwitchBoard.AuthService.Model.RefreshToken;
import com.SwitchBoard.AuthService.Repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class RefreshTokenService {
    
    private final RefreshTokenRepository refreshTokenRepository;
    
    @Value("${jwt.refresh-expiration}")
    private Long refreshTokenExpiration;
    
    public RefreshToken createRefreshToken(Account account) {
        log.info("RefreshTokenService : createRefreshToken : Creating refresh token for account - {}", account.getEmail());
        
        // Revoke existing tokens for this account
        revokeAllTokensByAccount(account);
        
        RefreshToken refreshToken = RefreshToken.builder()
                .token(generateRefreshTokenValue())
                .account(account)
                .expiryDate(LocalDateTime.now().plusSeconds(refreshTokenExpiration))
                .build();
        
        RefreshToken saved = refreshTokenRepository.save(refreshToken);
        log.info("RefreshTokenService : createRefreshToken : Refresh token created successfully for account - {}", account.getEmail());
        return saved;
    }
    
    public Optional<RefreshToken> findByToken(String token) {
        log.debug("RefreshTokenService : findByToken : Looking for refresh token");
        return refreshTokenRepository.findValidToken(token, LocalDateTime.now());
    }
    
    public boolean isTokenValid(RefreshToken token) {
        log.debug("RefreshTokenService : isTokenValid : Validating refresh token");
        return !token.getIsRevoked() && token.getExpiryDate().isAfter(LocalDateTime.now());
    }
    
    public void revokeToken(RefreshToken token) {
        log.info("RefreshTokenService : revokeToken : Revoking refresh token");
        token.setIsRevoked(true);
        refreshTokenRepository.save(token);
    }
    
    public void revokeAllTokensByAccount(Account account) {
        log.info("RefreshTokenService : revokeAllTokensByAccount : Revoking all tokens for account - {}", account.getEmail());
        refreshTokenRepository.revokeAllTokensByAccount(account);
    }
    
    public void deleteExpiredTokens() {
        log.info("RefreshTokenService : deleteExpiredTokens : Cleaning up expired tokens");
        refreshTokenRepository.deleteExpiredTokens(LocalDateTime.now());
    }
    
    private String generateRefreshTokenValue() {
        // Generate a secure random token
        return UUID.randomUUID().toString() + "-" + UUID.randomUUID().toString();
    }
}