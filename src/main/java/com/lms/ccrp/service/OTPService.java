package com.lms.ccrp.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OTPService {

    private final Map<String, String> otpStorage = new ConcurrentHashMap<>();
    private final Random random = new Random();

    @Autowired
    private JavaMailSender mailSender;

    public void generateOTP(String email) {
        String otp = String.valueOf(100000 + random.nextInt(900000));
        otpStorage.put(email, otp);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Your OTP Code");
        message.setText(generatePasswordResetOTPMessage(otp));
        mailSender.send(message);
    }

    public boolean validateOTP(String email, String enteredOtp) {
        String correctOtp = otpStorage.get(email);
        return correctOtp != null && correctOtp.equals(enteredOtp);
    }

    public void clearOTP(String email) {
        otpStorage.remove(email);
    }

    private String generatePasswordResetOTPMessage(String otp) {
        return "Hi," + "\n\n" +
                "Your OTP for resetting your password is: " + otp + "\n\n" +
                "This OTP is valid for 60 minutes. Please do not share it with anyone.\n\n" +
                "If you did not request this, please contact our support team immediately.\n\n" +
                "– CCRP Team";
    }


}
