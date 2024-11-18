package app.wio.repository;

import app.wio.entity.Seat;
import app.wio.entity.SeatStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SeatRepository extends JpaRepository<Seat, Long> {

    List<Seat> findByFloorId(Long floorId);

    Optional<Seat> findByIdAndFloorId(Long id, Long floorId);

    boolean existsByFloorId(Long floorId);

    boolean existsBySeatNumberAndFloorId(String seatNumber, Long floorId);

    List<Seat> findByFloorIdAndStatus(Long floorId, SeatStatus status);
}