package app.wio.service;

import app.wio.repository.InviteRepository;
import app.wio.repository.PasswordResetTokenRepository;
import app.wio.repository.VerificationTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class ScheduledTasks {

    private final VerificationTokenRepository verificationTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final InviteRepository inviteRepository;

    @Autowired
    public ScheduledTasks(VerificationTokenRepository verificationTokenRepository,
                          PasswordResetTokenRepository passwordResetTokenRepository,
                          InviteRepository inviteRepository) {
        this.verificationTokenRepository = verificationTokenRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.inviteRepository = inviteRepository;
    }

    @Scheduled(cron = "0 0 * * * *") // Runs every hour
    public void cleanUpExpiredTokens() {
        verificationTokenRepository.deleteByExpiryDateBefore(LocalDateTime.now());
        passwordResetTokenRepository.deleteByExpiryDateBefore(LocalDateTime.now());
        inviteRepository.deleteByExpiryDateBefore(LocalDateTime.now());
    }
}