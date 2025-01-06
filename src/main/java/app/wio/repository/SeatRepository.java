package app.wio.repository;

import app.wio.entity.Seat;
import app.wio.entity.SeatStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SeatRepository extends JpaRepository<Seat, Long> {
    List<Seat> findByFloorId(Long floorId);
    List<Seat> findByFloorIdAndStatus(Long floorId, SeatStatus status);
}