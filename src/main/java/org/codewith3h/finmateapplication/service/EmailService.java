package org.codewith3h.finmateapplication.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.codewith3h.finmateapplication.dto.response.AuthenticationResponse;
import org.codewith3h.finmateapplication.entity.EmailVerification;
import org.codewith3h.finmateapplication.entity.User;
import org.codewith3h.finmateapplication.exception.AppException;
import org.codewith3h.finmateapplication.exception.ErrorCode;
import org.codewith3h.finmateapplication.repository.EmailVerificationRepository;
import org.codewith3h.finmateapplication.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EmailService {
    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
    private static final int MAX_ATTEMPTS = 4;
    private static final int LOCKOUT_MINUTES = 10;
    private static final String PWD_RESET = "PWD_RESET_";

    private final UserRepository userRepository;

    private final JavaMailSender mailSender;

    private final EmailVerificationRepository emailVerificationRepository;
    private final UserService userService;

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
    public void sendVerificationEmail(String toEmail) throws MessagingException {
        // Save verification to database
        User user = userRepository.findByEmail(toEmail)
                .orElseThrow(() -> new AppException(ErrorCode.EMAIL_NOT_FOUND_EXCEPTION));

        if (user.getVerified()) {
            throw new AppException(ErrorCode.EMAIL_ALREADY_VERIFIED_EXCEPTION);
        }

        if (userService.isInLockoutPeriod(user)) {
            throw new AppException(ErrorCode.IN_RESENT_OTP_EXCEPTION);
        }

        String verificationCode = generateOTP();


        EmailVerification verification = new EmailVerification();
        verification.setUser(user);
        verification.setEmail(toEmail);
        verification.setVerificationCode(verificationCode);
        verification.setExpiryTime(LocalDateTime.now().plusMinutes(verificationExpiryMinutes));
        verification.setVerified(false);
        verification.setCreatedAt(LocalDateTime.now());
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
    public void sendPasswordResetEmail(String toEmail) throws MessagingException {
        User user = userRepository.findByEmail(toEmail)
                .orElseThrow(() -> new AppException(ErrorCode.EMAIL_NOT_FOUND_EXCEPTION));

        String token = createPasswordResetToken(user);

        try {
            logger.info("Attempting to send password reset email to: {}", toEmail);

            String subject = "Password Reset Request - FinMate";
            String resetLink = "http://127.0.0.1:5500/pages/reset-password/?token=" + token;

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
                .orElseThrow(() -> new AppException(ErrorCode.EMAIL_NOT_FOUND_EXCEPTION));

        if (!verification.getVerificationCode().equals(code)) {
            logger.info("OTP is incorrect, verification code is {}.", code);
            throw new AppException(ErrorCode.INVALID_VERIFICATION_CODE_EXCEPTION);
        } else if (verification.getVerified()) {
            throw new AppException(ErrorCode.TOKEN_EXPIRED);
        } else if (verification.getExpiryTime().isBefore(LocalDateTime.now())) {
            logger.info("Time to verify is expiry, expiry time: {}", verification.getExpiryTime());
        }

        verification.setVerified(true);
        emailVerificationRepository.save(verification);

        logger.info("Verification record marked as verified for email {}. User status update should happen in AuthController.", email);
        return true;
    }

    @Async
    @Transactional
    public void sendOtpForChangeEmail(String toEmail, User user) throws MessagingException {
        // Không check email đã tồn tại, chỉ gửi OTP về email mới cho user hiện tại
        String verificationCode = generateOTP();
        EmailVerification verification = new EmailVerification();
        verification.setUser(user);
        verification.setEmail(toEmail);
        verification.setVerificationCode(verificationCode);
        verification.setExpiryTime(LocalDateTime.now().plusMinutes(verificationExpiryMinutes));
        verification.setVerified(false);
        verification.setCreatedAt(LocalDateTime.now());
        emailVerificationRepository.save(verification);
        // Create email content
        String subject = "Verify Your New Email";
        String content = String.format(
                "Hello!\n\n" +
                        "You requested to change your email for FinMate.\n\n" +
                        "Please use the following verification code (OTP) to confirm your new email:\n\n" +
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
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, false);
        helper.setFrom(fromEmail);
        helper.setTo(toEmail);
        helper.setSubject(subject);
        helper.setText(content);
        mailSender.send(message);
        logger.info("Change email OTP sent to: {}", toEmail);
    }

    private String generateOTP() {
        Random random = new Random();
        StringBuilder otp = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            otp.append(random.nextInt(10));
        }
        return otp.toString();
    }

    private String createPasswordResetToken(User user) {
        String token = PWD_RESET + UUID.randomUUID().toString();
        user.setPasswordResetToken(token);
        user.setPasswordResetTokenExpiry(LocalDateTime.now().plusMinutes(passwordResetExpiryMinutes));
        userRepository.save(user);
        return token;
    }

    @Async
    @Transactional
    public void sendCustomEmail(String toEmail, String subject, String content, Boolean isHTML) throws MessagingException {

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setFrom(fromEmail);
        helper.setTo(toEmail);
        helper.setSubject(subject);
        helper.setText(content, isHTML);
        mailSender.send(message);
        logger.info("Email sent successfully to: {}", toEmail);
    }

} 