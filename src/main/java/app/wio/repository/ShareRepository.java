package app.wio.repository;

import app.wio.entity.Share;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShareRepository extends JpaRepository<Share, Long> {

    List<Share> findByRecipientIdOrderByCreatedAtDesc(Long recipientId);


    List<Share> findByRecipientIdAndReadAtIsNullOrderByCreatedAtDesc(Long recipientId);

}
