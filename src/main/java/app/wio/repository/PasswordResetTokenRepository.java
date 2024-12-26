package app.wio.repository;

import app.wio.entity.PasswordResetToken;
import app.wio.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findByToken(String token);

    Optional<PasswordResetToken> findByUser(User user);

    Optional<PasswordResetToken> findByUser_Email(String email);

    void deleteByExpiryDateBefore(LocalDateTime now);
}
