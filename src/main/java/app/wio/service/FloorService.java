package app.wio.service;

import app.wio.dto.FloorCreationDto;
import app.wio.entity.*;
import app.wio.exception.CompanyNotFoundException;
import app.wio.exception.FloorNotFoundException;
import app.wio.exception.LockNotAvailableException;
import app.wio.exception.ResourceConflictException;
import app.wio.repository.BookingRepository;
import app.wio.repository.CompanyRepository;
import app.wio.repository.FloorLockRepository;
import app.wio.repository.FloorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class FloorService {
    private final FloorRepository floorRepository;
    private final CompanyRepository companyRepository;
    private final FloorLockRepository floorLockRepository;
    private final BookingRepository bookingRepository;

    @Autowired
    public FloorService(
            FloorRepository floorRepository,
            CompanyRepository companyRepository,
            FloorLockRepository floorLockRepository,
            BookingRepository bookingRepository
    ) {
        this.floorRepository = floorRepository;
        this.companyRepository = companyRepository;
        this.floorLockRepository = floorLockRepository;
        this.bookingRepository = bookingRepository;
    }

    public Floor createFloor(FloorCreationDto floorDto) {
        Company company = companyRepository.findById(floorDto.getCompanyId())
                .orElseThrow(() -> new CompanyNotFoundException("Company not found."));

        Floor floor = new Floor();
        floor.setName(floorDto.getName());
        floor.setFloorNumber(floorDto.getFloorNumber());
        floor.setCompany(company);

        Floor savedFloor = floorRepository.save(floor);
        if (savedFloor.getId() == null) {
            throw new IllegalStateException("Floor ID was not generated.");
        }
        return savedFloor;
    }

    public Floor getFloorById(Long id) {
        return floorRepository.findById(id)
                .orElseThrow(() ->
                        new FloorNotFoundException("Floor with ID " + id + " not found."));
    }

    public void deleteFloorById(Long id) {
        Floor floor = floorRepository.findById(id)
                .orElseThrow(() -> new FloorNotFoundException("Floor with ID " + id + " not found."));

        LocalDate today = LocalDate.now();
        List<Booking> futureBookings = bookingRepository.findByFloorIdAndDateAfterAndStatus(
                id, today, BookingStatus.ACTIVE
        );
        if (!futureBookings.isEmpty()) {
            throw new ResourceConflictException(
                    "Cannot delete floor; it has seats with future active bookings."
            );
        }
        floorRepository.delete(floor);
    }

    public List<Floor> getFloorsByCompanyId(Long companyId) {
        return floorRepository.findByCompanyId(companyId);
    }

    public void lockFloor(Long floorId, Long userId) {
        // Check if locked by another user
        floorLockRepository.findByFloorId(floorId).ifPresent(lock -> {
            if (!lock.getLockedByUserId().equals(userId)) {
                throw new LockNotAvailableException("This floor is currently locked by another user.");
            }
        });

        floorLockRepository.findByFloorId(floorId).orElseGet(() -> {
            FloorLock lock = new FloorLock();
            lock.setFloorId(floorId);
            lock.setLockedByUserId(userId);
            lock.setLockedAt(LocalDateTime.now());
            return floorLockRepository.save(lock);
        });
    }

    public void unlockFloor(Long floorId, Long userId) {
        floorLockRepository.findByFloorId(floorId).ifPresent(lock -> {
            if (lock.getLockedByUserId().equals(userId)) {
                floorLockRepository.delete(lock);
            } else {
                throw new LockNotAvailableException("You do not hold the lock for this floor.");
            }
        });
    }
}
