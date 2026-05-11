package com.doctorat.suividoctorat.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
public class OTPService {

    @Autowired
    private JavaMailSender mailSender;

    private Map<String, OTPData> otpStorage = new HashMap<>();

    private static class OTPData {
        String otp;
        long expiryTime;

        OTPData(String otp, long expiryTime) {
            this.otp = otp;
            this.expiryTime = expiryTime;
        }
    }

    public String generateOTP(String email) {
        String otp = String.format("%06d", new Random().nextInt(999999));
        long expiryTime = System.currentTimeMillis() + (5 * 60 * 1000);
        otpStorage.put(email, new OTPData(otp, expiryTime));
        return otp;
    }

    public void sendOTPEmail(String email, String otp) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email);
            message.setSubject("PhD Portal - Email Verification OTP");
            message.setText("Dear User,\n\nYour OTP for email verification is: " + otp + "\n\nThis OTP is valid for 5 minutes.\n\nBest regards,\nPhD Tracking Portal");
            mailSender.send(message);
            System.out.println("OTP sent to: " + email);
        } catch (Exception e) {
            System.out.println("Failed to send OTP: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public boolean verifyOTP(String email, String userOTP) {
        OTPData otpData = otpStorage.get(email);

        if (otpData == null) {
            return false;
        }

        if (System.currentTimeMillis() > otpData.expiryTime) {
            otpStorage.remove(email);
            return false;
        }

        boolean isValid = otpData.otp.equals(userOTP);

        if (isValid) {
            otpStorage.remove(email);
        }

        return isValid;
    }
}