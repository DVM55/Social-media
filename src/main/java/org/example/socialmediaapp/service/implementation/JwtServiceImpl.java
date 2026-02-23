package org.example.socialmediaapp.service.implementation;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import io.jsonwebtoken.security.SignatureException;
import lombok.RequiredArgsConstructor;
import org.example.socialmediaapp.config.JwtConfig;
import org.example.socialmediaapp.service.JwtService;
import org.example.socialmediaapp.service.RedisService;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;


@Component
@RequiredArgsConstructor
public class JwtServiceImpl implements JwtService {
    private final JwtConfig jwtConfig;
    private final RedisService redisService;

    private Key accessSecretKey;
    private Key refreshSecretKey;

    @PostConstruct
    public void init() {
        accessSecretKey = Keys.hmacShaKeyFor(jwtConfig.getAccessSecret().getBytes());
        refreshSecretKey = Keys.hmacShaKeyFor(jwtConfig.getRefreshSecret().getBytes());
    }

    @Override
    public String generateAccessToken(Long userId, String username, String role) {
        String token = generateToken(userId, username, role, jwtConfig.getAccessTokenExpirationMs(), accessSecretKey);
        redisService.setWithTTL("ACCESS_TOKEN:" + userId, token, jwtConfig.getAccessTokenExpirationMs());
        return token;
    }

    @Override
    public String generateRefreshToken(Long userId, String username, String role) {
        return generateToken(userId, username, role, jwtConfig.getRefreshTokenExpirationMs(), refreshSecretKey);
    }

    @Override
    public void validateAccessToken(String token) {
        validateToken(token, accessSecretKey);
    }

    @Override
    public void validateRefreshToken(String token) {
        validateToken(token, refreshSecretKey);
    }

    @Override
    public Long getAccountIdFromAccessToken(String token) {
        Claims claims = getClaims(token, accessSecretKey);
        return claims.get("accountId", Long.class);
    }

    @Override
    public Long getAccountIdFromRefreshToken(String token) {
        Claims claims = getClaims(token, refreshSecretKey);
        return claims.get("accountId", Long.class);
    }

    @Override
    public String getUsernameFromAccessToken(String token) {
        Claims claims = getClaims(token, accessSecretKey);
        return claims.get("username", String.class);
    }

    @Override
    public String getRoleFromAccessToken(String token) {
        Claims claims = getClaims(token, accessSecretKey);
        return claims.get("role", String.class);
    }

    @Override
    public boolean isValidAccessToken(String token){
        try {
            getClaims(token, accessSecretKey);
            return true;
        } catch (ExpiredJwtException | UnsupportedJwtException | MalformedJwtException | SignatureException | IllegalArgumentException e) {
            return false;
        }
    }

    private Claims getClaims(String token, Key key) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private String generateToken(Long userId, String username, String role, long validityInMs, Key key) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + validityInMs);

        return Jwts.builder()
                .claim("username", username)
                .claim("accountId", userId)
                .claim("role", role)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    @Override
    public boolean isAccessTokenMatchRedis(Long userId, String token) {
        String redisKey = "ACCESS_TOKEN:" + userId;
        String storedToken = (String) redisService.get(redisKey);

        if (storedToken == null) {
            return false;
        }

        return storedToken.equals(token);
    }

    // ✅ Parse token và check chữ ký + hết hạn
    private void validateToken(String token, Key key) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
        } catch (ExpiredJwtException e) {
            System.err.println("Token expired at: " + e.getClaims().getExpiration());
            throw e;
        } catch (UnsupportedJwtException e) {
            System.err.println("Unsupported JWT token format");
            throw e;
        } catch (MalformedJwtException e) {
            System.err.println("Malformed JWT token (bad structure)");
            throw e;
        } catch (SignatureException e) {
            System.err.println("Invalid JWT signature");
            throw e;
        } catch (Exception e) {
            System.err.println("Unknown JWT validation error: " + e.getMessage());
            throw e;
        }
    }
}
