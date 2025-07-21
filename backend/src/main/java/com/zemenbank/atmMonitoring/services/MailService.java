package com.zemenbank.atmMonitoring.services;


public interface MailService {
    /**
     * Sends an email to the specified recipient.
     *
     * @param subject the email subject
     * @param message the email message
     * @param to the recipient's email address
     */
    void sendMail(String subject, String message, String to);
}
