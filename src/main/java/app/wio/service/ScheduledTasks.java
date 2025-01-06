package app.wio.service;

import app.wio.entity.Share;
import app.wio.repository.PasswordResetTokenRepository;
import app.wio.repository.ShareRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ScheduledTasks {

    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final ShareRepository shareRepository;

    @Autowired
    public ScheduledTasks(
            PasswordResetTokenRepository passwordResetTokenRepository,
            ShareRepository shareRepository
    ) {
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.shareRepository = shareRepository;
    }

    @Scheduled(cron = "0 0 * * * *")
    public void cleanUpExpiredTokensAndShares() {
        LocalDateTime now = LocalDateTime.now();
        passwordResetTokenRepository.deleteByExpiryDateBefore(now);
        cleanUpExpiredShares();
    }

    private void cleanUpExpiredShares() {
        LocalDate today = LocalDate.now();
        List<Share> allShares = shareRepository.findAll();
        List<Share> expiredShares = allShares.stream()
                .filter(s -> s.getMaxBookingDate() != null)
                .filter(s -> {
                    LocalDate cutoff = s.getMaxBookingDate().plusDays(30);
                    return today.isAfter(cutoff);
                })
                .collect(Collectors.toList());
        if (!expiredShares.isEmpty()) {
            shareRepository.deleteAllInBatch(expiredShares);
        }
    }
}