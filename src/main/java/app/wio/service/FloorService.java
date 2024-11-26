package app.wio.service;

import app.wio.dto.FloorCreationDto;
import app.wio.entity.Company;
import app.wio.entity.Floor;
import app.wio.exception.CompanyNotFoundException;
import app.wio.exception.FloorNotFoundException;
import app.wio.repository.CompanyRepository;
import app.wio.repository.FloorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FloorService {

    private final FloorRepository floorRepository;
    private final CompanyRepository companyRepository;

    @Autowired
    public FloorService(FloorRepository floorRepository, CompanyRepository companyRepository) {
        this.floorRepository = floorRepository;
        this.companyRepository = companyRepository;
    }

    public Floor createFloor(FloorCreationDto floorDto) {
        // Map FloorCreationDto to Floor entity
        Floor floor = new Floor();
        floor.setName(floorDto.getName());
        floor.setFloorNumber(floorDto.getFloorNumber());

        // Fetch the company
        Company company = companyRepository.findById(floorDto.getCompanyId())
                .orElseThrow(() -> new CompanyNotFoundException("Company not found."));
        floor.setCompany(company);

        return floorRepository.save(floor);
    }

    public Floor getFloorByNameAndCompanyId(String name, Long companyId) {
        return floorRepository.findByNameAndCompanyId(name, companyId)
                .orElseThrow(() -> new FloorNotFoundException("Floor not found."));
    }

    public List<Floor> getFloorsByCompanyId(Long companyId) {
        return floorRepository.findByCompanyId(companyId);
    }

    public Floor getFloorById(Long id) {
        return floorRepository.findById(id)
                .orElseThrow(() -> new FloorNotFoundException("Floor with ID " + id + " not found."));
    }

    public void deleteFloorById(Long id) {
        Floor floor = getFloorById(id);
        floorRepository.delete(floor);
    }
}