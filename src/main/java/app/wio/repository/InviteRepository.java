
package app.wio.repository;

import app.wio.entity.Invite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface InviteRepository extends JpaRepository<Invite, Long> {

    Optional<Invite> findByToken(String token);

    void deleteByExpiryDateBefore(LocalDateTime now);
}