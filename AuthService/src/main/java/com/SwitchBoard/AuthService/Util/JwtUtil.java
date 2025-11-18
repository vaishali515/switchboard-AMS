package com.SwitchBoard.AuthService.Util;

import com.SwitchBoard.AuthService.DTO.Account.USER_ROLE;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Component
@Slf4j
public class JwtUtil {

    @Value("${jwt.private-key}")
    private String privateKeyPath;

    @Value("${jwt.public-key}")
    private String publicKeyPath;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    private PrivateKey getPrivateKey() throws Exception {
        log.debug("JwtUtil : getPrivateKey : Loading private key from classpath - {}", privateKeyPath);
        try (InputStream inputStream = new ClassPathResource(privateKeyPath).getInputStream()) {
            byte[] keyBytes = inputStream.readAllBytes();
            String key = new String(keyBytes)
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replaceAll("\\s+", "");
            byte[] decoded = Base64.getDecoder().decode(key);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decoded);
            return KeyFactory.getInstance("RSA").generatePrivate(keySpec);
        } catch (Exception e) {
            log.error("JwtUtil : getPrivateKey : Error loading private key - {}", e.getMessage());
            throw e;
        }
    }

    /** Generate JWT with userId, username, role */
    public String generateToken(String email, String username, UUID userId, List<USER_ROLE> role) throws Exception {
        log.info("JwtUtil : generateToken : Generating JWT token for user - {}", email);
        try {
            Date now = new Date();
            Date expiryDate = new Date(System.currentTimeMillis() + jwtExpiration*1000);
            log.debug("JwtUtil : generateToken : Setting token expiration to {}", expiryDate);

            String token = Jwts.builder()
                    .setSubject(email)
                    .claim("userId", userId)        // ADD THIS
                    .claim("username", username)
                    .claim("role", role)
                    .setIssuedAt(now)
                    .setExpiration(expiryDate)
                    .signWith(getPrivateKey(), SignatureAlgorithm.RS256)
                    .compact();

            log.info("JwtUtil : generateToken : JWT token generated successfully");
            return token;
        } catch (Exception e) {
            log.error("JwtUtil : generateToken : Error generating JWT token - {}", e.getMessage());
            throw e;
        }
    }
}
