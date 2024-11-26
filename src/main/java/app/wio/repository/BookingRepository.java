package app.wio.repository;

import app.wio.entity.Booking;
import app.wio.entity.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    Page<Booking> findByUserId(Long userId, Pageable pageable);

    List<Booking> findBySeatId(Long seatId);

    List<Booking> findBySeatIdAndDate(Long seatId, LocalDate date);

    boolean existsBySeatId(Long seatId);

    @Query("SELECT b.seat.id FROM Booking b WHERE b.seat.floor.id = :floorId AND b.date = :date AND b.status = 'ACTIVE'")
    List<Long> findBookedSeatIdsByFloorIdAndDate(@Param("floorId") Long floorId, @Param("date") LocalDate date);

    @Query("SELECT b FROM Booking b WHERE b.seat.floor.id = :floorId AND b.date = :date AND b.status = 'ACTIVE'")
    List<Booking> findByFloorIdAndDate(@Param("floorId") Long floorId, @Param("date") LocalDate date);
}