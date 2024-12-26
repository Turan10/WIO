package app.wio.repository;

import app.wio.entity.FloorLock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FloorLockRepository extends JpaRepository<FloorLock, Long> {

    Optional<FloorLock> findByFloorId(Long floorId);

    void deleteByFloorId(Long floorId);
}
