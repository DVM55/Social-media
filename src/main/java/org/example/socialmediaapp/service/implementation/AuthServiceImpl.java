package org.example.socialmediaapp.service.implementation;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.example.socialmediaapp.dto.req.*;
import org.example.socialmediaapp.dto.res.LoginResponse;
import org.example.socialmediaapp.dto.res.RegisterResponse;
import org.example.socialmediaapp.entity.Account;
import org.example.socialmediaapp.entity.Key;
import org.example.socialmediaapp.enums.Role;
import org.example.socialmediaapp.exception.ConflictException;

import org.example.socialmediaapp.mapper.AccountMapper;
import org.example.socialmediaapp.repository.AccountRepository;
import org.example.socialmediaapp.repository.KeyRepository;
import org.example.socialmediaapp.service.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final AccountMapper accountMapper;
    private final PasswordEncoder passwordEncoder;
    private final AccountRepository accountRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final KeyRepository keyRepository;
    private final OtpService otpService;
    private final RedisService redisService;

    @Override
    public RegisterResponse register(RegisterRequest request) {
        if (accountRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("Email đã được sử dụng");
        }

        if (accountRepository.existsByUsername(request.getUsername())) {
            throw new ConflictException("Username đã được sử dụng");
        }

        Account account = Account.builder()
                .email(request.getEmail())
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER)
                .build();

        Account savedAccount = accountRepository.save(account);

        return accountMapper.toDto(savedAccount);
    }

    @Override
    public void login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );
        //System.out.println("MAIN THREAD = " + Thread.currentThread().getName());
        otpService.sendOTPAsync(request.getEmail(), 1);
    }

    @Override
    public void sendOTP(EmailRequest request){
        switch (request.getType()) {
            case 1:
                otpService.sendOTPAsync(request.getEmail(), 1);
                break;

            case 2:
                otpService.sendOTPAsync(request.getEmail(), 2);
                break;

            default:
                throw new IllegalArgumentException("Loại OTP không hợp lệ: " + request.getType());
        }
    }

    @Override
    public LoginResponse verifyAccount(VerifyAccountRequest request){
        boolean valid = otpService.validateOtp(request.getEmail(), request.getOtp());
        if (!valid) {
            throw new IllegalArgumentException("OTP không hợp lệ hoặc đã hết hạn");
        }

        Account account = accountRepository.findByEmail(request.getEmail())
                .orElseThrow(()-> new EntityNotFoundException("Tài khoản không tồn tại với email:" + request.getEmail()));

        String accessToken = jwtService.generateAccessToken(
                account.getId(),
                account.getUsername(),
                account.getRole().name()
        );

        String refreshToken = jwtService.generateRefreshToken(
                account.getId(),
                account.getUsername(),
                account.getRole().name()
        );

        System.out.println("AccessToken:" + accessToken);
        System.out.println("RefreshToken:" + refreshToken);

        Optional<Key> keyOptional = keyRepository.findByAccount_Id(account.getId());

        Key key = keyOptional.orElseGet(Key::new);

        key.setAccount(account);
        key.setRefreshToken(refreshToken);

        keyRepository.save(key);

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    @Override
    public void ForgotPassword(EmailRequest request){
        if (!accountRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("Email này chưa được đăng ký. Không thể thực hiện chức năng này");
        }
        otpService.sendOTPAsync(request.getEmail(), 2);
    }

    @Override
    public boolean verifyOtp(VerifyAccountRequest request) {
        boolean valid = otpService.validateOtp(request.getEmail(), request.getOtp());
        if (!valid) {
            throw new IllegalArgumentException("OTP không hợp lệ hoặc đã hết hạn");
        }
        return true;
    }

    @Override
    public void resetPassword(ResetPasswordRequest request) {
        boolean valid = otpService.validateOtp(request.getEmail(), request.getOtp());
        if (!valid) {
            throw new IllegalArgumentException("Phiên đã hết hạn, vui lòng thực hiện quên mật khẩu lại!");
        }

        Account account = accountRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy tài khoản với email: " + request.getEmail()));

        account.setPassword(passwordEncoder.encode(request.getNewPassword()));
        accountRepository.save(account);

        // ✅ Xoá OTP sau khi reset thành công
        otpService.deleteOtp(request.getEmail());
    }

    @Override
    public Map<String, String> refreshToken(RefreshTokenRequest request){
        keyRepository.findByRefreshToken(request.getRefreshToken())
                .orElseThrow(() -> new EntityNotFoundException("Refresh token không hợp lệ"));
        jwtService.validateRefreshToken(request.getRefreshToken());
        Long userId = jwtService.getAccountIdFromRefreshToken(request.getRefreshToken());
        Account account = accountRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy tài khoản với id: " + userId));
        String newAccessToken = jwtService.generateAccessToken(
                account.getId(),
                account.getUsername(),
                account.getRole().name()
        );
        return Map.of("accessToken", newAccessToken);
    }

    @Override
    @Transactional
    public void logout() {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        redisService.delete("ACCESS_TOKEN:" + userId);
        keyRepository.deleteByAccount_Id(userId);
    }
}
