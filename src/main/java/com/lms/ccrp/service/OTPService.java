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

    public String generateOtp(String email) {
        String otp = String.valueOf(100000 + random.nextInt(900000));
        otpStorage.put(email, otp);

        // send email
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Your OTP Code");
        message.setText("Your OTP is: " + otp);
        mailSender.send(message);

        return otp;
    }

    public boolean validateOtp(String email, String enteredOtp) {
        String correctOtp = otpStorage.get(email);
        return correctOtp != null && correctOtp.equals(enteredOtp);
    }

    public void clearOtp(String email) {
        otpStorage.remove(email);
    }
}
