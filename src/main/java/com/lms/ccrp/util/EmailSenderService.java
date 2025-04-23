package com.lms.ccrp.util;

import com.lms.ccrp.dto.NotificationDTO;
import com.lms.ccrp.dto.RewardHistoryDTO;
import com.lms.ccrp.entity.Customer;
import com.lms.ccrp.entity.User;
import com.lms.ccrp.repository.CustomerRepository;
import com.lms.ccrp.repository.UserRepository;
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
    private String companyEmail;

    public String sendRedemptionRequestEmailToUser(Long userId, RewardHistoryDTO transactionHistoryDTO) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User doesn't exist"));
        Customer customer = customerRepository.findByUserId(userId).orElseThrow(() -> new RuntimeException("Customer doesn't exist"));
        long points = Math.abs(transactionHistoryDTO.getNumberOfPoints());
        String recipientTo = user.getEmail();
        String subject = "We’ve Received Your Redemption Request for " + points + " Points.";
        String username = customer.getName();
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setFrom(companyEmail);
            helper.setTo(recipientTo);
            helper.setSubject(subject);
            helper.setText(emailTemplateGenerator.generateEmailTemplateForPointsRedemptionRequest(username, points), true);

            mailSender.send(message);
            return subject + " Email sent successfully to " + username;
        } catch (Exception e) {
            return subject + " Failed to send email: " + e.getMessage();
        }
    }

    /**
     * Sends a successful redemption email to the user based on the provided notification details.
     * <p>
     * The method constructs an email using the details from the {@link NotificationDTO} object,
     * generates an HTML template for a successful points redemption, and sends the email to the recipient.
     * </p>
     *
     * @param notificationDTO the data transfer object containing the recipient email address,
     *                        subject, username, and the number of redeemed points
     * @return a string message indicating whether the email was sent successfully or if there was an error
     */
    public String sendSuccessfullRedemptionEmailToUser(NotificationDTO notificationDTO) {
        String subject = "You’ve Successfully Redeemed " + notificationDTO.getPoints() + " Points.";
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setFrom(companyEmail);
            helper.setTo(notificationDTO.getRecipientTo());
            helper.setSubject(subject);
            helper.setText(emailTemplateGenerator.generateEmailTemplateForPointsRedemptionSuccess(notificationDTO), true);

            mailSender.send(message);
            return subject + " Email sent successfully to " + notificationDTO.getUsername();
        } catch (Exception e) {
            return subject + " Failed to send email: " + e.getMessage();
        }
    }

    /**
     * Sends a failure notification email to the user when their points redemption request is rejected.
     * <p>
     * This method constructs an email using the details from the provided {@link NotificationDTO},
     * generates a failure email template for the redemption, and attempts to send it to the user's email address.
     * </p>
     *
     * @param notificationDTO the data transfer object containing recipient email, username, subject,
     *                        and the number of points attempted to redeem
     * @return a string message indicating whether the email was sent successfully or if there was an error
     */
    public String sendFailureRedemptionEmailToUser(NotificationDTO notificationDTO) {
        String subject = "Your Redemption Request for " + notificationDTO.getPoints() + " Points Has Been Rejected.";
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setFrom(companyEmail);
            helper.setTo(notificationDTO.getRecipientTo());
            helper.setSubject(subject);
            helper.setText(emailTemplateGenerator.generateEmailTemplateForPointsRedemptionFailure(notificationDTO), true);

            mailSender.send(message);
            return subject + " Email sent successfully to " + notificationDTO.getUsername();
        } catch (Exception e) {
            return subject + " Failed to send email: " + e.getMessage();
        }
    }


    public String sendSuccessPointsEarnedEmailToUser(NotificationDTO notificationDTO) {
        String subject = "You've earned " + notificationDTO.getPoints() + " Points.";
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setFrom(companyEmail);
            helper.setTo(notificationDTO.getRecipientTo());
            helper.setSubject(subject);
            helper.setText(emailTemplateGenerator.generateEmailTemplateForSuccessPointsEarned(notificationDTO), true);

            mailSender.send(message);
            return subject + " Email sent successfully to " + notificationDTO.getUsername();
        } catch (Exception e) {
            return subject + " Failed to send email: " + e.getMessage();
        }
    }

    public String sendPointsExpiredEmailToUser(NotificationDTO notificationDTO) {
        String subject = "Your " + notificationDTO.getPoints() + " Points Has Been Expired.";
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setFrom(companyEmail);
            helper.setTo(notificationDTO.getRecipientTo());
            helper.setSubject(subject);
            helper.setText(emailTemplateGenerator.generateEmailTemplateForPointsExpired(notificationDTO), true);

            mailSender.send(message);
            return subject + " Email sent successfully to " + notificationDTO.getUsername();
        } catch (Exception e) {
            return subject + " Failed to send email: " + e.getMessage();
        }
    }
}
