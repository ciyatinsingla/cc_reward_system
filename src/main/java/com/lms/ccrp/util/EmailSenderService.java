package com.lms.ccrp.util;

import com.lms.ccrp.dto.RewardTransactionHistoryDTO;
import com.lms.ccrp.entity.Customer;
import com.lms.ccrp.entity.User;
import com.lms.ccrp.repository.CustomerRepository;
import com.lms.ccrp.repository.UserRepository;
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

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Value("${spring.mail.username}")
    private String emailUsername;

    public String sendRedemptionEmailToUser(Long userId, RewardTransactionHistoryDTO transactionHistoryDTO) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User doesn't exist"));
        Customer customer = customerRepository.findByUserId(userId).orElseThrow(() -> new RuntimeException("Customer doesn't exist"));
        Long points = transactionHistoryDTO.getNumberOfPoints();
        String toRecipient = user.getEmail();
        String subject = "We’ve Received Your Redemption Request for " + points + " Points";
        String username = customer.getName();
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setFrom(emailUsername);
            helper.setTo(toRecipient);
            helper.setSubject(subject);
            helper.setText(emailTemplateGenerator.generateEmailTemplateForPointsRedemption(username, points), true);

            mailSender.send(message);
            return "Email sent successfully!";
        } catch (MessagingException e) {
            return "Failed to send email: " + e.getMessage();
        }
    }


}
