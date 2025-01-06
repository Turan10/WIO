package app.wio.repository;

import app.wio.dto.response.BookingResponseDto;
import app.wio.entity.Booking;
import app.wio.entity.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    Page<Booking> findByUserId(Long userId, Pageable pageable);
    List<Booking> findBySeatId(Long seatId);
    List<Booking> findBySeatIdAndDate(Long seatId, LocalDate date);
    @Query("""
        SELECT b.seat.id FROM Booking b
        WHERE b.seat.floor.id = :floorId
          AND b.date = :date
          AND b.status = 'ACTIVE'
    """)
    List<Long> findBookedSeatIdsByFloorIdAndDate(@Param("floorId") Long floorId, @Param("date") LocalDate date);
    @Query("""
        SELECT b FROM Booking b
        WHERE b.seat.floor.id = :floorId
          AND b.date = :date
          AND b.status = 'ACTIVE'
    """)
    List<Booking> findByFloorIdAndDate(@Param("floorId") Long floorId, @Param("date") LocalDate date);
    @Query("""
        SELECT new app.wio.dto.response.BookingResponseDto(
            b.id,
            b.date,
            CAST(b.status as string),
            b.user.id,
            b.seat.id,
            b.seat.seatNumber,
            b.seat.floor.floorNumber,
            b.seat.floor.name
        )
        FROM Booking b
        WHERE b.user.id = :userId
        ORDER BY b.date DESC
    """)
    Page<BookingResponseDto> findBookingsByUserIdWithFloor(@Param("userId") Long userId, Pageable pageable);
    @Query("""
        SELECT b FROM Booking b
        WHERE b.seat.id = :seatId
          AND b.date > :today
          AND b.status = :status
    """)
    List<Booking> findBySeatIdAndDateAfterAndStatus(
            @Param("seatId") Long seatId,
            @Param("today") LocalDate date,
            @Param("status") BookingStatus status
    );
    @Query("""
        SELECT b FROM Booking b
        WHERE b.seat.floor.id = :floorId
          AND b.date > :today
          AND b.status = :status
    """)
    List<Booking> findByFloorIdAndDateAfterAndStatus(
            @Param("floorId") Long floorId,
            @Param("today") LocalDate today,
            @Param("status") BookingStatus status
    );
    boolean existsByUserIdAndDate(Long userId, LocalDate date);
}