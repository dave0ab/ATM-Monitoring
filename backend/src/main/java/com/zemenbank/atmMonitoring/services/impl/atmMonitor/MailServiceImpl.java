package com.zemenbank.atmMonitoring.services.impl.atmMonitor;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.zemenbank.atmMonitoring.services.MailService;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
@RequiredArgsConstructor
public class MailServiceImpl implements MailService {

    private static final Logger logger = LoggerFactory.getLogger(MailServiceImpl.class);
    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromAddress;

    private static final int MAX_RETRIES = 3; // Maximum number of retries for failed emails
    private final ExecutorService executorService = Executors.newFixedThreadPool(10); // Limit concurrency


    @Override
    public void sendMail(String subject, String message, String to) {
        int attempt = 0;
        boolean success = false;
        to = to + "@zemenbank.com"; // Append the domain to the recipient's email

        while (attempt < MAX_RETRIES && !success) {
            try {
                MimeMessage mimeMessage = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

                // Set email details
                helper.setFrom("ZB-ATM-Monitoring@zemenbank.com"); // Explicitly set the sender address
                helper.setTo(to);
                helper.setSubject(subject);
                helper.setText(message, true); // Set to true to indicate HTML content

                // Send the email
                mailSender.send(mimeMessage);
                logger.info("HTML email sent successfully to {}", to);
                success = true; // Mark as successful
            } catch (MessagingException | RuntimeException e) {
                attempt++;
                logger.error("Failed to send HTML email to {}. Attempt {}: {}", to, attempt, e.getMessage(), e);

                // Optionally, add a small delay before retrying
                try {
                    Thread.sleep(1000); // 1 second delay
                } catch (InterruptedException ignored) {
                    logger.warn("Retry delay interrupted for recipient: {}", to);
                }
            }
        }

        if (!success) {
            logger.error("Failed to send email to {} after {} attempts.", to, MAX_RETRIES);
            // Optionally log to a database or notify an administrator
        }
    }
    /**
     * Sends bulk emails asynchronously.
     *s
     * @param subject     The email subject.
     * @param message     The email body (plain text).
     * @param recipients  A list of recipient email addresses (without domain).
     */
    public void sendBulkEmails(String subject, String message, List<String> recipients) {
        for (String recipient : recipients) {
            executorService.submit(() -> sendMail(subject, message, recipient));
        }
        logger.info("Bulk email sending initiated for {} recipients.", recipients.size());
    }
}
