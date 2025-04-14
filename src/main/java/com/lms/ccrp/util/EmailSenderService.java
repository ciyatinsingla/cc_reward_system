package com.lms.ccrp.util;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailSenderService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private EmailTemplateGenerator emailTemplateGenerator;

    @Value("${spring.mail.username}")
    private String emailUsername;

    public String userPointsRedemptionMail(String toRecipient, String subject, String userName, int points) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setFrom(emailUsername);
            helper.setTo(toRecipient);
            helper.setSubject(subject);
            helper.setText(emailTemplateGenerator.generateEmailTemplateForPointsRedemption(userName, points), true);

            mailSender.send(message);

            return "Email sent successfully!";
        } catch (MessagingException e) {
            return "Failed to send email: " + e.getMessage();
        }
    }
}
