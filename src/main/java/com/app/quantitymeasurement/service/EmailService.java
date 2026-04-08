package com.app.quantitymeasurement.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;


@Slf4j
@Service
public class EmailService {

	private final JavaMailSender mailSender;
    private final String fromAddress;

    public EmailService(
            JavaMailSender mailSender,
            @Value("${spring.mail.username}") String fromAddress) {

        this.mailSender = mailSender;
        this.fromAddress = fromAddress;
    }

    // -------------------------------------------------------------------------
    // Registration notification
    // -------------------------------------------------------------------------

    /**
     * Sends a welcome email to a newly registered user.
     *
     * @param toEmail   the recipient's email address
     * @param userName  the user's display name
     */
    @Async
    public void sendRegistrationEmail(String toEmail, String userName) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromAddress);
            message.setTo(toEmail);
            message.setSubject("Welcome to Quantity Measurement App!");
            message.setText(
                "Hi " + userName + ",\n\n" +
                "Your account has been created successfully.\n\n" +
                "You can now log in and start measuring quantities.\n\n" +
                "Regards,\nQuantity Measurement Team"
            );
            mailSender.send(message);
            log.info("Registration email sent to {}", toEmail);
        } catch (Exception ex) {
            log.error("Failed to send registration email to {}: {}", toEmail, ex.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // Login notification
    // -------------------------------------------------------------------------

    /**
     * Sends a login-alert email to a user who has just authenticated.
     *
     * @param toEmail  the recipient's email address
     */
    @Async
    public void sendLoginNotificationEmail(String toEmail) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromAddress);
            message.setTo(toEmail);
            message.setSubject("New login to your Quantity Measurement account");
            message.setText("""
                    Hi,

                    We noticed a new login to your account.

                    If this was you, no action is needed.
                    If you did not log in, please reset your password immediately.

                    Regards,
                    Quantity Measurement Team
                    """);
            mailSender.send(message);
            log.info("Login notification email sent to {}", toEmail);
        } catch (Exception ex) {
            log.error("Failed to send login notification to {}: {}", toEmail, ex.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // Forgot password notification
    // -------------------------------------------------------------------------

    /**
     * Sends a confirmation email after a forgotten-password reset.
     *
     * @param toEmail  the recipient's email address
     */
    @Async
    public void sendForgotPasswordEmail(String toEmail) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromAddress);
            message.setTo(toEmail);
            message.setSubject("Your password has been changed");
            message.setText("""
                    Hi,

                    Your password has been changed successfully.

                    If you did not request this change, please contact support immediately.

                    Regards,
                    Quantity Measurement Team
                    """);
            mailSender.send(message);
            log.info("Forgot-password confirmation email sent to {}", toEmail);
        } catch (Exception ex) {
            log.error("Failed to send forgot-password email to {}: {}", toEmail, ex.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // Reset password notification
    // -------------------------------------------------------------------------

    /**
     * Sends a confirmation email after a logged-in user resets their password.
     *
     * @param toEmail  the recipient's email address
     */
    @Async
    public void sendPasswordResetEmail(String toEmail) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromAddress);
            message.setTo(toEmail);
            message.setSubject("Password reset successfully");
            message.setText("""
                    Hi,

                    Your password has been reset successfully.

                    If you did not do this, please contact support immediately.

                    Regards,
                    Quantity Measurement Team
                    """);
            mailSender.send(message);
            log.info("Password-reset confirmation email sent to {}", toEmail);
        } catch (Exception ex) {
            log.error("Failed to send password-reset email to {}: {}", toEmail, ex.getMessage());
        }
    }
}
