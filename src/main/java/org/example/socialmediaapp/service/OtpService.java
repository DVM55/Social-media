package org.example.socialmediaapp.service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
@RequiredArgsConstructor
public class OtpService {
    private final RedisService redisService;
    private final MailService mailService;

    public String generateOtp(String email) {
        String otp = String.format("%06d", new Random().nextInt(999999));
        redisService.setWithTTL("OTP:" + email, otp, 900); // TTL = 15 phút
        return otp;
    }

    public boolean validateOtp(String email, String otp) {
        String key = "OTP:" + email;
        Object cached = redisService.get(key);

        return cached != null && cached.toString().equals(otp);
    }

    public void deleteOtp(String email) {
        redisService.delete("OTP:" + email);
    }

    @Async
    public void sendOTPAsync(String email, int type) {
        String otp = generateOtp(email);

        System.out.println("OTP: " + otp);

        String subject;
        String body;

        switch (type) {
            case 1: // Xác thực tài khoản
                subject = "Mã OTP xác thực tài khoản";
                body = "<p>Xin chào,</p>" +
                        "<p>Mã OTP của bạn là: <b>" + otp + "</b></p>" +
                        "<p>Mã này có hiệu lực trong 1 phút.</p>";
                break;

            case 2: // Quên mật khẩu
                subject = "Mã OTP khôi phục mật khẩu";
                body = "<p>Xin chào,</p>" +
                        "<p>Mã OTP của bạn là: <b>" + otp + "</b></p>" +
                        "<p>Mã này có hiệu lực trong 15 phút.</p>";
                break;

            default:
                throw new IllegalArgumentException("Loại OTP không hợp lệ: " + type);
        }

        mailService.sendHtml(email, subject, body);
    }

}
