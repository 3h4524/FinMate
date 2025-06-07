package org.codewith3h.finmateapplication.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.codewith3h.finmateapplication.entity.EmailVerification;
import org.codewith3h.finmateapplication.repository.EmailVerificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Random;

@Service
public class EmailService {
    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
    private static final int MAX_ATTEMPTS = 4;
    private static final int LOCKOUT_MINUTES = 10;

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private EmailVerificationRepository emailVerificationRepository;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.email.verification.expiry-minutes}")
    private int verificationExpiryMinutes;

    @Value("${app.email.verification.code-length}")
    private int verificationCodeLength;

    @Value("${app.password.reset.expiry-minutes}")
    private int passwordResetExpiryMinutes;

    @Async
    @Transactional
    public void sendVerificationEmail(String toEmail, String verificationCode) throws MessagingException {
        // Save verification to database
        EmailVerification verification = new EmailVerification();
        verification.setEmail(toEmail);
        verification.setVerificationCode(verificationCode);
        verification.setExpiryTime(LocalDateTime.now().plusMinutes(verificationExpiryMinutes));
        verification.setVerified(false);
        emailVerificationRepository.save(verification);

        // Create email content
        String subject = "Verify Your Email";
        String content = String.format(
            "Hello!\n\n" +
            "Thank you for registering with FinMate.\n\n" +
            "Please use the following verification code (OTP) to complete your email verification:\n\n" +
            "  %s  \n\n" +
            "This code will expire in %d minutes.\n\n" +
            "If you did not request this, please ignore this email.\n\n" +
            "---\n" +
            "This is an automated email, please do not reply.\n\n" +
            "Best regards,\n" +
            "FinMate Team",
            verificationCode,
            verificationExpiryMinutes
        );

        // Send email
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, false);
        helper.setFrom(fromEmail);
        helper.setTo(toEmail);
        helper.setSubject(subject);
        helper.setText(content);
        mailSender.send(message);

        logger.info("Verification email sent to: {}", toEmail);
    }

    @Async
    public void sendPasswordResetEmail(String toEmail, String token) throws MessagingException {
        try {
            logger.info("Attempting to send password reset email to: {}", toEmail);
            
            String subject = "Password Reset Request - FinMate";
            String resetLink = "http://localhost:8080/reset-password.html?token=" + token;

            String content = String.format(
                "Hello!\n\n" +
                "We received a request to reset your FinMate account password.\n\n" +
                "Please click the link below to reset your password:\n\n" +
                "%s\n\n" +
                "This link will expire in %d minutes.\n\n" +
                "If you did not request this, please ignore this email. Your password will not be changed.\n\n" +
                "---\n" +
                "This is an automated email, please do not reply.\n\n" +
                "Best regards,\n" +
                "FinMate Team",
                resetLink,
                passwordResetExpiryMinutes
            );

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(content);
            
            logger.debug("Email content prepared. From: {}, To: {}, Subject: {}", fromEmail, toEmail, subject);
            
            mailSender.send(message);
            logger.info("Password reset email sent successfully to: {}", toEmail);
        } catch (Exception e) {
            logger.error("Failed to send password reset email to {}: {}", toEmail, e.getMessage(), e);
            if (e instanceof MessagingException) {
                throw (MessagingException) e;
            }
            throw new MessagingException("Failed to send password reset email: " + e.getMessage(), e);
        }
    }

    @Transactional
    public boolean verifyEmail(String email, String code) {
        EmailVerification verification = emailVerificationRepository
            .findFirstByEmailOrderByCreatedAtDesc(email)
            .orElse(null);

        if (verification == null || 
            !verification.getVerificationCode().equals(code) ||
            verification.isVerified() ||
            verification.getExpiryTime().isBefore(LocalDateTime.now())) {
            logger.warn("Verification failed for email {}. Details: verification null={}, code match={}, isVerified={}, expired={}",
                         email,
                         verification == null,
                         verification != null && verification.getVerificationCode().equals(code),
                         verification != null && verification.isVerified(),
                         verification != null && verification.getExpiryTime().isBefore(LocalDateTime.now()));
            return false;
        }

        verification.setVerified(true);
        emailVerificationRepository.save(verification);
        
        logger.info("Verification record marked as verified for email {}. User status update should happen in AuthController.", email);
        return true;
    }

    private String generateOTP() {
        Random random = new Random();
        StringBuilder otp = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            otp.append(random.nextInt(10));
        }
        return otp.toString();
    }
} 