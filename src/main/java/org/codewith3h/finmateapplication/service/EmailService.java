package org.codewith3h.finmateapplication.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.codewith3h.finmateapplication.dto.response.AuthenticationResponse;
import org.codewith3h.finmateapplication.dto.response.BudgetEntry;
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

import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
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
            String resetLink = "https://www.finmate.fun/pages/reset-password/?token=" + token;

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


    @Async
    @Transactional
    public void sendBudgetRecommendation(Integer userId, List<BudgetEntry> budgetEntries) throws MessagingException {
        User user = userRepository.findById(userId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        String userName = user.getName();

        // Tạo đối tượng format tiền theo chuẩn Việt Nam
        NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

        // Calculate total savings
        double totalSavings = budgetEntries.stream()
                .mapToDouble(BudgetEntry::getSavings)
                .sum();

        // Prepare email content as HTML with enhanced styling
        StringBuilder emailContent = new StringBuilder();
        emailContent.append("<!DOCTYPE html>");
        emailContent.append("<html lang='vi'>");
        emailContent.append("<head>");
        emailContent.append("<meta charset='UTF-8'>");
        emailContent.append("<meta name='viewport' content='width=device-width, initial-scale=1.0'>");
        emailContent.append("<style>");
        emailContent.append("body { font-family: 'Segoe UI', Arial, sans-serif; background-color: #f8f9fa; margin: 0; padding: 20px; }");
        emailContent.append(".container { max-width: 800px; margin: 0 auto; background-color: #ffffff; padding: 30px; border-radius: 10px; box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1); }");
        emailContent.append("h2 { color: #2c3e50; text-align: center; margin-bottom: 30px; font-size: 28px; font-weight: 600; }");
        emailContent.append("p { color: #555; line-height: 1.6; margin-bottom: 15px; }");
        emailContent.append(".greeting { font-size: 16px; margin-bottom: 20px; }");
        emailContent.append("table { width: 100%; border-collapse: collapse; margin: 20px 0; box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1); }");
        emailContent.append("th { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 15px; text-align: left; font-weight: 600; font-size: 14px; text-transform: uppercase; letter-spacing: 0.5px; }");
        emailContent.append("td { padding: 15px; border-bottom: 1px solid #e9ecef; }");
        emailContent.append("tr:nth-child(even) { background-color: #f8f9fa; }");
        emailContent.append("tr:hover { background-color: #e3f2fd; transition: background-color 0.3s ease; }");
        emailContent.append(".amount { text-align: right; font-weight: 600; color: #2c3e50; }");
        emailContent.append(".savings { text-align: right; font-weight: 600; color: #27ae60; }");
        emailContent.append(".total-row { background-color: #e8f5e8 !important; font-weight: bold; border-top: 2px solid #27ae60; }");
        emailContent.append(".total-row td { padding: 18px 15px; font-size: 16px; }");
        emailContent.append(".total-savings { color: #27ae60; font-size: 18px; font-weight: bold; }");
        emailContent.append(".summary-box { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 20px; border-radius: 8px; margin: 20px 0; text-align: center; }");
        emailContent.append(".summary-box h3 { margin: 0 0 10px 0; font-size: 18px; }");
        emailContent.append(".summary-amount { font-size: 24px; font-weight: bold; }");
        emailContent.append(".footer { margin-top: 30px; padding-top: 20px; border-top: 1px solid #e9ecef; color: #7f8c8d; }");
        emailContent.append(".signature { margin-top: 20px; font-weight: 600; color: #2c3e50; }");
        emailContent.append("</style>");
        emailContent.append("</head>");
        emailContent.append("<body>");
        emailContent.append("<div class='container'>");

        emailContent.append("<h2>📊 Budget Recommendation Summary</h2>");
        emailContent.append("<p class='greeting'>Dear ").append(userName).append(",</p>");
        emailContent.append("<p>We have prepared your personalized budget recommendation for this period. Below is a detailed breakdown of your budget allocation and potential savings:</p>");

        // Summary box for total savings
        emailContent.append("<div class='summary-box'>");
        emailContent.append("<h3>💰 Total Potential Savings</h3>");
        emailContent.append("<div class='summary-amount'>").append(currencyFormatter.format(totalSavings)).append("</div>");
        emailContent.append("</div>");

        emailContent.append("<table>");
        emailContent.append("<thead>");
        emailContent.append("<tr>");
        emailContent.append("<th>📋 Category</th>");
        emailContent.append("<th>💳 Budget (VND)</th>");
        emailContent.append("<th>💰 Savings (VND)</th>");
        emailContent.append("</tr>");
        emailContent.append("</thead>");
        emailContent.append("<tbody>");

        // Add each budget entry to the table
        for (BudgetEntry entry : budgetEntries) {
            emailContent.append("<tr>");
            emailContent.append("<td>").append(entry.getCategoryName()).append("</td>");
            emailContent.append("<td class='amount'>")
                    .append(currencyFormatter.format(entry.getBudget())).append("</td>");
            emailContent.append("<td class='savings'>")
                    .append(currencyFormatter.format(entry.getSavings())).append("</td>");
            emailContent.append("</tr>");
        }

        // Add total row
        emailContent.append("<tr class='total-row'>");
        emailContent.append("<td><strong>📊 TOTAL SAVINGS</strong></td>");
        emailContent.append("<td class='amount'>-</td>");
        emailContent.append("<td class='total-savings'>")
                .append(currencyFormatter.format(totalSavings)).append("</td>");
        emailContent.append("</tr>");

        emailContent.append("</tbody>");
        emailContent.append("</table>");

        emailContent.append("<div class='footer'>");
        emailContent.append("<p>💡 <strong>Tips:</strong> Following this budget recommendation can help you save <strong>")
                .append(currencyFormatter.format(totalSavings))
                .append("</strong> this period!</p>");
        emailContent.append("<p>Thank you for using our budgeting service. We're here to help you achieve your financial goals!</p>");
        emailContent.append("<div class='signature'>");
        emailContent.append("Best regards,<br>");
        emailContent.append("🏦 Your Personal Budgeting Team");
        emailContent.append("</div>");
        emailContent.append("</div>");

        emailContent.append("</div>");
        emailContent.append("</body></html>");

        // Fetch user email
        String userEmail = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.EMAIL_NOT_FOUND_EXCEPTION)).getEmail();

        // Send email
        sendCustomEmail(
                userEmail,
                "📊 Your Personal Budget Recommendation",
                emailContent.toString(),
                true // isHTML
        );
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