package app.wio.repository;

import app.wio.entity.Floor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FloorRepository extends JpaRepository<Floor, Long> {

    List<Floor> findByCompanyId(Long companyId);

    Optional<Floor> findByNameAndCompanyId(String name, Long companyId);
}
