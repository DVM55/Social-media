package org.example.socialmediaapp.service;

public interface JwtService {
    String generateAccessToken(Long userId, String username, String role);

    String generateRefreshToken(Long userId, String username, String role);

    void validateAccessToken(String token);

    void  validateRefreshToken(String token);

    Long getAccountIdFromAccessToken(String token);

    Long getAccountIdFromRefreshToken(String token);

    String getUsernameFromAccessToken(String token);

    String getRoleFromAccessToken(String token);

    boolean isValidAccessToken(String token);

    boolean isAccessTokenMatchRedis(Long userId, String token);
}
