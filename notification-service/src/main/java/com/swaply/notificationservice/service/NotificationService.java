package com.swaply.notificationservice.service;

import com.swaply.notificationservice.dto.ProductDeletedEmailRequest;
import com.swaply.notificationservice.dto.TicketDto;
import com.swaply.notificationservice.dto.VerificationRequest;
import com.swaply.notificationservice.exception.NotificationException;
import com.swaply.notificationservice.utils.constants.EmailContext;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;


@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationService {

    private final ObjectProvider<JavaMailSender> mailSenderProvider;

    @Value("${FROM_EMAIL:}")
    private String fromEmail;

    @Value("${MAIL_ENABLED:true}")
    private boolean mailEnabled;


    @Async
    public void sendVerificationEmail(VerificationRequest request) {
        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
        if (!mailEnabled) {
            log.warn("Email sending is disabled; skipping verification email for {}", request.email);
            return;
        }

        if (mailSender == null || fromEmail == null || fromEmail.isBlank()) {
            throw new NotificationException("JavaMailSender or FROM_EMAIL is not configured");
        } else {
            try {
                MimeMessage message = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
                helper.setFrom(fromEmail);
                helper.setTo(request.email);
                helper.setSubject("Your verification code is...");
                helper.setText(EmailContext.setToken(request.token), true);

                mailSender.send(message);
                log.info("Email has been sent to {}", request.email);

            } catch (MessagingException | RuntimeException e) {
                log.warn("SMTP email send failed for {}: {}", request.email, e.getMessage());
                throw new NotificationException("SMTP email send failed: " + e.getMessage());
            }
        }
    }

    @Async
    public void sendProductDeletedEmail(ProductDeletedEmailRequest request) {
        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
        if (!mailEnabled) {
            log.warn("Email sending is disabled; skipping product deletion email for {}", request.email);
            return;
        }

        if (mailSender == null || fromEmail == null || fromEmail.isBlank()) {
            throw new NotificationException("JavaMailSender or FROM_EMAIL is not configured");
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(request.email);
            helper.setSubject("Swaply - Məhsul silindi");
            helper.setText(EmailContext.setDeletedProductNotification(request), true);

            mailSender.send(message);
            log.info("Product deletion email sent to {}", request.email);
        } catch (MessagingException | RuntimeException e) {
            log.warn("SMTP product deletion email send failed for {}: {}", request.email, e.getMessage());
            throw new NotificationException("SMTP email send failed: " + e.getMessage());
        }
    }

    @Async
    public void sendTicketResponse(TicketDto ticket) {
        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
        if (!mailEnabled) {
            log.warn("Email sending is disabled; skipping email for {}", ticket.getUserEmail());
            return;
        }

        if (mailSender == null || fromEmail == null || fromEmail.isBlank()) {
            throw new NotificationException("JavaMailSender or FROM_EMAIL is not configured");
        }

        try {
            log.info("{}",ticket.toString());
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(ticket.getUserEmail());
            helper.setSubject("Swaply - Şikayətinizə cavab");
            helper.setText(EmailContext.setResponseReportMessage(ticket), true);

            mailSender.send(message);
            log.info("Ticket Response email sent to {}", ticket.getUserEmail());
        } catch (MessagingException | RuntimeException e) {
            log.warn("SMTP ticket response email send failed for {}: {}", ticket.getUserEmail(), e.getMessage());
            throw new NotificationException("SMTP email send failed: " + e.getMessage());
        }
    }
}
