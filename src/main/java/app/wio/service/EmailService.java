package app.wio.service;

import app.wio.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Service
public class EmailService {

    @Value("${app.frontend.url}")
    private String frontendUrl;

    @Autowired
    private JavaMailSender mailSender;

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    public void sendVerificationEmail(User user, String token) {
        String verificationLink = frontendUrl + "/verify?token=" + token;
        String subject = "Email Verification";
        String content = "Dear " + user.getName()
                + ",\n\nPlease click the link below to verify your email address:\n"
                + verificationLink + "\n\nThank you!";
        sendEmail(user.getEmail(), subject, content);
    }

    public void sendPasswordResetEmail(User user, String token) {
        String resetLink = frontendUrl + "/password-reset?token=" + token;
        String subject = "Password Reset Request";
        String content = "Dear " + user.getName()
                + ",\n\nPlease click the link below to reset your password:\n"
                + resetLink
                + "\n\nIf you did not request a password reset, please ignore this email.\n\nThank you!";
        sendEmail(user.getEmail(), subject, content);
    }

    private void sendEmail(String to, String subject, String content) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(content);
            mailSender.send(message);
            logger.info("Email sent to {}", to);
        } catch (Exception e) {
            logger.error("Failed to send email to {}", to, e);
        }
    }
}
