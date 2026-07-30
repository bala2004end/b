package com.aidb.assistant.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * Generates and validates signed JWT tokens using HMAC-SHA256.
 * Tokens embed userId and username as claims for stateless user resolution.
 */
@Component
public class JwtTokenProvider {

    private static final Logger log = LoggerFactory.getLogger(JwtTokenProvider.class);

    private static final String ISSUER = "aidb-assistant";
    private static final String CLAIM_USER_ID = "uid";

    private final String jwtSecret;
    private final long jwtExpirationMs;

    public JwtTokenProvider(
            @Value("${aidb.jwt.secret}") String jwtSecret,
            @Value("${aidb.jwt.expiration-ms:86400000}") long jwtExpirationMs) {
        this.jwtSecret = jwtSecret;
        this.jwtExpirationMs = jwtExpirationMs;
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(Authentication authentication) {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

        return Jwts.builder()
                .issuer(ISSUER)
                .subject(principal.getUsername())
                .claim(CLAIM_USER_ID, principal.getId())
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    public String getUsernameFromToken(String token) {
        return parseClaims(token).getSubject();
    }

    public Long getUserIdFromToken(String token) {
        return parseClaims(token).get(CLAIM_USER_ID, Long.class);
    }

    public boolean validateToken(String authToken) {
        try {
            Claims claims = parseClaims(authToken);
            if (!ISSUER.equals(claims.getIssuer())) {
                log.warn("JWT token has unexpected issuer: {}", claims.getIssuer());
                return false;
            }
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("JWT token is expired");
        } catch (JwtException e) {
            log.warn("JWT token validation failed: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("JWT token is empty or null");
        }
        return false;
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
