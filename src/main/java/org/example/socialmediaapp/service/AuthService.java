package org.example.socialmediaapp.service;

import org.example.socialmediaapp.dto.req.*;
import org.example.socialmediaapp.dto.res.LoginResponse;
import org.example.socialmediaapp.dto.res.RegisterResponse;

import java.util.Map;

public interface AuthService {
    RegisterResponse register(RegisterRequest request);
    void login(LoginRequest request);
    void ForgotPassword(EmailRequest request);
    boolean verifyOtp(VerifyAccountRequest request);
    void resetPassword(ResetPasswordRequest request);
    Map<String, String> refreshToken(RefreshTokenRequest request);
    void sendOTP( EmailRequest request);
    LoginResponse verifyAccount(VerifyAccountRequest request);
    void logout();
}
