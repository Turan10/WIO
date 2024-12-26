package app.wio.repository;

import app.wio.entity.OneTimeCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface OneTimeCodeRepository extends JpaRepository<OneTimeCode, Long> {

    Optional<OneTimeCode> findByCode(String code);

    void deleteByExpiryDateBefore(LocalDateTime now);
}
