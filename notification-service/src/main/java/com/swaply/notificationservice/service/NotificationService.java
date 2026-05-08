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
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Properties;


@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationService {

    private final ObjectProvider<JavaMailSender> mailSenderProvider;

    @Value("${FROM_EMAIL:${SPRING_MAIL_USERNAME:}}")
    private String fromEmail;

    @Value("${spring.mail.host:}")
    private String mailHost;

    @Value("${spring.mail.port:0}")
    private int mailPort;

    @Value("${spring.mail.username:}")
    private String springMailUsername;

    @Value("${spring.mail.password:}")
    private String springMailPassword;

    @Value("${spring.mail.properties.mail.smtp.auth:true}")
    private boolean smtpAuth;

    @Value("${spring.mail.properties.mail.smtp.starttls.enable:false}")
    private boolean smtpStartTlsEnable;

    @Value("${spring.mail.properties.mail.smtp.starttls.required:false}")
    private boolean smtpStartTlsRequired;

    @Value("${spring.mail.properties.mail.smtp.ssl.enable:false}")
    private boolean smtpSslEnable;

    @Value("${MAIL_ENABLED:true}")
    private boolean mailEnabled;


    @Async
    public void sendVerificationEmail(VerificationRequest request) {
        JavaMailSender mailSender = resolveMailSender();
        String effectiveFromEmail = resolveFromEmail();
        if (!mailEnabled) {
            log.warn("Email sending is disabled; skipping verification email for {}", request.email);
            return;
        }

        if (mailSender == null || effectiveFromEmail.isBlank()) {
            log.warn("JavaMailSender or FROM_EMAIL is not configured; skipping verification email for {}", request.email);
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(effectiveFromEmail);
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

    @Async
    public void sendProductDeletedEmail(ProductDeletedEmailRequest request) {
        JavaMailSender mailSender = resolveMailSender();
        String effectiveFromEmail = resolveFromEmail();
        if (!mailEnabled) {
            log.warn("Email sending is disabled; skipping product deletion email for {}", request.email);
            return;
        }

        if (mailSender == null || effectiveFromEmail.isBlank()) {
            log.warn("JavaMailSender or FROM_EMAIL is not configured; skipping product deletion email for {}", request.email);
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(effectiveFromEmail);
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
        JavaMailSender mailSender = resolveMailSender();
        String effectiveFromEmail = resolveFromEmail();
        if (!mailEnabled) {
            log.warn("Email sending is disabled; skipping email for {}", ticket.getUserEmail());
            return;
        }

        if (mailSender == null || effectiveFromEmail.isBlank()) {
            log.warn("JavaMailSender or FROM_EMAIL is not configured; skipping ticket response email for {}", ticket.getUserEmail());
            return;
        }

        try {
            log.info("{}",ticket.toString());
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(effectiveFromEmail);
            helper.setTo(ticket.getUserEmail());
            helper.setSubject("Netbazar - Şikayətinizə cavab");
            helper.setText(EmailContext.setResponseReportMessage(ticket), true);

            mailSender.send(message);
            log.info("Ticket Response email sent to {}", ticket.getUserEmail());
        } catch (MessagingException | RuntimeException e) {
            log.warn("SMTP ticket response email send failed for {}: {}", ticket.getUserEmail(), e.getMessage());
            throw new NotificationException("SMTP email send failed: " + e.getMessage());
        }
    }

    private JavaMailSender resolveMailSender() {
        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
        if (mailSender != null) {
            return mailSender;
        }

        if (mailHost == null || mailHost.isBlank() || springMailUsername == null || springMailUsername.isBlank()) {
            return null;
        }

        JavaMailSenderImpl fallbackSender = new JavaMailSenderImpl();
        fallbackSender.setHost(mailHost);
        fallbackSender.setPort(mailPort > 0 ? mailPort : 465);
        fallbackSender.setUsername(springMailUsername);
        fallbackSender.setPassword(springMailPassword);

        Properties props = fallbackSender.getJavaMailProperties();
        props.put("mail.smtp.auth", String.valueOf(smtpAuth));
        props.put("mail.smtp.starttls.enable", String.valueOf(smtpStartTlsEnable));
        props.put("mail.smtp.starttls.required", String.valueOf(smtpStartTlsRequired));
        props.put("mail.smtp.ssl.enable", String.valueOf(smtpSslEnable));

        return fallbackSender;
    }

    private String resolveFromEmail() {
        if (fromEmail != null && !fromEmail.isBlank()) {
            return fromEmail;
        }
        return springMailUsername != null ? springMailUsername : "";
    }
}
