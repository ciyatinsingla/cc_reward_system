package com.lms.ccrp.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for handling user password reset (UPR) operations via OTP (One-Time Password).
 */
@Service
public class UPRService {

    private final Map<String, String> otpStorage = new ConcurrentHashMap<>();
    private final Random random = new Random();

    @Autowired
    private JavaMailSender mailSender;

    /**
     * Generates a 6-digit OTP for the given email, stores it, and sends it via email.
     *
     * @param email The recipient's email address.
     */
    public void generateOTP(String email) {
        String otp = String.valueOf(100000 + random.nextInt(900000));
        otpStorage.put(email, otp);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Your OTP Code");
        message.setText(generatePasswordResetOTPMessage(otp));
        mailSender.send(message);
    }

    /**
     * Validates the entered OTP against the one stored for the given email.
     *
     * @param email      The email associated with the OTP.
     * @param enteredOtp The OTP entered by the user.
     * @return True if the OTP is correct and matches the stored one; false otherwise.
     */
    public boolean validateOTP(String email, String enteredOtp) {
        String correctOtp = otpStorage.get(email);
        return correctOtp != null && correctOtp.equals(enteredOtp);
    }

    /**
     * Clears the stored OTP for the given email. Typically called after successful validation.
     *
     * @param email The email whose OTP entry should be cleared.
     */
    public void clearOTP(String email) {
        otpStorage.remove(email);
    }

    /**
     * Generates the email message content for sending the OTP.
     *
     * @param otp The OTP to include in the message.
     * @return A formatted string containing the OTP and instructions.
     */
    private String generatePasswordResetOTPMessage(String otp) {
        return "Hi," + "\n\n" +
                "Your OTP for resetting your password is: " + otp + "\n\n" +
                "This OTP is valid for 10 minutes. Please do not share it with anyone.\n\n" +
                "If you did not request this, please contact our support team immediately.\n\n" +
                "– CCRP Team";
    }
}
