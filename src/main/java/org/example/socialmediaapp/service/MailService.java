package org.example.socialmediaapp.service;

public interface MailService {

    /**
     * Gửi email text thường (plain text)
     */
    void sendSimple(String to, String subject, String body);

    /**
     * Gửi email HTML
     */
    void sendHtml(String to, String subject, String htmlBody);
}