package com.example.sellerhelp.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
@RequiredArgsConstructor
@Slf4j // For logging
public class EmailService {

    private final JavaMailSender mailSender;

    /**
     * Sends an email asynchronously.
     * @param to The recipient's email address.
     * @param subject The subject of the email.
     * @param htmlBody The HTML content of the email.
     */
    @Async // This annotation makes the email sending non-blocking
    public void sendEmail(String to, String subject, String htmlBody) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true); // true indicates the body is HTML

            mailSender.send(mimeMessage);
            log.info("Email sent successfully to {}", to);

        } catch (MessagingException e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
            // In a real application, you might save the failed email to a database table to retry later.
        }
    }
}