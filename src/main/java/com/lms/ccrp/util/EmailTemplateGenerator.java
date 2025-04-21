package com.lms.ccrp.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class EmailTemplateGenerator {

    @Value("${source.file.path}")
    private String emailFile;

    private static final String APR = "Approved";

    public String generateEmailTemplateForPointsRedemption(String userName, long points) {
        String SIGNATURE = "Best Regards,<br>CCRP Team";
        String FOOTER = "You are receiving this email because you have enrolled in our program.";
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

}
