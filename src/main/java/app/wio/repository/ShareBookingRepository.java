package app.wio.repository;

import app.wio.entity.ShareBooking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShareBookingRepository extends JpaRepository<ShareBooking, Long> {

}