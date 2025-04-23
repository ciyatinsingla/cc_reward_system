package com.lms.ccrp.util;

import com.lms.ccrp.dto.NotificationDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class EmailTemplateGenerator {

    @Value("${source.file.path}")
    private String emailFile;

    private static final String SIGNATURE = "Best Regards,<br>CCRP Team";
    private static final String FOOTER = "You are receiving this email because you have enrolled in our program.";

    public String generateEmailTemplateForPointsRedemptionRequest(String userName, long points) {
        return "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "<style>" +
                "   body { font-family: Arial, sans-serif; }" +
                "   .container { width: 100%; max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #ddd; }" +
                "   .content { padding: 10px; }" +
                "   .signature { margin-top: 20px; font-style: italic; }" +
                "   .footer { text-align: center; color: #777777; font-size: 12px; margin-top: 30px; }" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<div class='container'>" +
                "   <div class='content'>" +
                "       <p>Hi " + userName + ",</p>" +
                "       <p>You have successfully opted for redemption of <strong>" + points + " points</strong> and we have sent the same to the Source System.</p>" +
                "       <p>You'll be notified once it is successfully evaluated and the redemption is processed.</p>" +
                "   </div>" +
                "   <div class='signature'>" +
                "       <p>" + SIGNATURE + "</p>" +
                "   </div>" +
                "</div>" +
                "<div class='footer'>" +
                "   <p>" + FOOTER + "</p>" +
                "</div>" +
                "</body>" +
                "</html>";
    }

    public String generateEmailTemplateForPointsRedemptionSuccess(NotificationDTO notificationDTO) {
        return "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "<style>" +
                "   body { font-family: Arial, sans-serif; }" +
                "   .container { width: 100%; max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #ddd; }" +
                "   .content { padding: 10px; }" +
                "   .signature { margin-top: 20px; font-style: italic; }" +
                "   .footer { text-align: center; color: #777777; font-size: 12px; margin-top: 30px; }" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<div class='container'>" +
                "   <div class='content'>" +
                "       <p>Hi " + notificationDTO.getUsername() + ",</p>" +
                "       <p>Congratulations! Your redemption of <strong>" + notificationDTO.getPoints() + " points</strong> has been successfully processed.</p>" +
                "       <p>You should receive the corresponding benefit shortly.</p>" +
                "   </div>" +
                "   <div class='signature'>" +
                "       <p>" + SIGNATURE + "</p>" +
                "   </div>" +
                "</div>" +
                "<div class='footer'>" +
                "   <p>" + FOOTER + "</p>" +
                "</div>" +
                "</body>" +
                "</html>";
    }

    public String generateEmailTemplateForPointsRedemptionFailure(NotificationDTO notificationDTO) {
        return "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "<style>" +
                "   body { font-family: Arial, sans-serif; }" +
                "   .container { width: 100%; max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #ddd; }" +
                "   .content { padding: 10px; }" +
                "   .signature { margin-top: 20px; font-style: italic; }" +
                "   .footer { text-align: center; color: #777777; font-size: 12px; margin-top: 30px; }" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<div class='container'>" +
                "   <div class='content'>" +
                "       <p>Hi " + notificationDTO.getUsername() + ",</p>" +
                "       <p>We regret to inform you that your redemption request for <strong>" + notificationDTO.getPoints() + " points</strong> could not " +
                "be processed due to <strong>" + notificationDTO.getReason() + "</strong></p>" +
                "   </div>" +
                "   <div class='signature'>" +
                "       <p>" + SIGNATURE + "</p>" +
                "   </div>" +
                "</div>" +
                "<div class='footer'>" +
                "   <p>" + FOOTER + "</p>" +
                "</div>" +
                "</body>" +
                "</html>";
    }

    public String generateEmailTemplateForSuccessPointsEarned(NotificationDTO notificationDTO) {
        return "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "<style>" +
                "   body { font-family: Arial, sans-serif; }" +
                "   .container { width: 100%; max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #ddd; }" +
                "   .content { padding: 10px; }" +
                "   .signature { margin-top: 20px; font-style: italic; }" +
                "   .footer { text-align: center; color: #777777; font-size: 12px; margin-top: 30px; }" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<div class='container'>" +
                "   <div class='content'>" +
                "       <p>Hi " + notificationDTO.getUsername() + ",</p>" +
                "       <p>Great news! You've earned <strong>" + notificationDTO.getPoints() + " new points</strong> as part of your recent activities.</p>" +
                "       <p>Keep going and redeem your rewards soon!</p>" +
                "   </div>" +
                "   <div class='signature'>" +
                "       <p>" + SIGNATURE + "</p>" +
                "   </div>" +
                "</div>" +
                "<div class='footer'>" +
                "   <p>" + FOOTER + "</p>" +
                "</div>" +
                "</body>" +
                "</html>";
    }

    public String generateEmailTemplateForPointsExpired(NotificationDTO notificationDTO) {
        return "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "<style>" +
                "   body { font-family: Arial, sans-serif; }" +
                "   .container { width: 100%; max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #ddd; }" +
                "   .content { padding: 10px; }" +
                "   .signature { margin-top: 20px; font-style: italic; }" +
                "   .footer { text-align: center; color: #777777; font-size: 12px; margin-top: 30px; }" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<div class='container'>" +
                "   <div class='content'>" +
                "       <p>Hi " + notificationDTO.getUsername() + ",</p>" +
                "       <p>We’re sorry to inform you that <strong>" + notificationDTO.getPoints() + " points</strong> in your account have expired and are no longer available for redemption.</p>" +
                "       <p>We encourage you to redeem your points regularly to avoid expiration.</p>" +
                "   </div>" +
                "   <div class='signature'>" +
                "       <p>" + SIGNATURE + "</p>" +
                "   </div>" +
                "</div>" +
                "<div class='footer'>" +
                "   <p>" + FOOTER + "</p>" +
                "</div>" +
                "</body>" +
                "</html>";
    }

}
