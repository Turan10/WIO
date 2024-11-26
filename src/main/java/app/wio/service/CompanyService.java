package app.wio.service;

import app.wio.dto.CompanyCreationDto;
import app.wio.entity.Company;
import app.wio.entity.User;
import app.wio.exception.CompanyNotFoundException;
import app.wio.repository.CompanyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CompanyService {

    private final CompanyRepository companyRepository;

    @Autowired
    public CompanyService(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    public Company createCompany(CompanyCreationDto companyDto) {
        // Map CompanyCreationDto to Company entity
        Company company = new Company();
        company.setName(companyDto.getName());
        company.setAddress(companyDto.getAddress());

        return companyRepository.save(company);
    }

    public Company getCompanyById(Long id) {
        return companyRepository.findById(id)
                .orElseThrow(() -> new CompanyNotFoundException("Company with ID " + id + " not found."));
    }

    public Company getCompanyByName(String name) {
        return companyRepository.findByName(name)
                .orElseThrow(() -> new CompanyNotFoundException("Company with name " + name + " not found."));
    }

    public Company getCompanyByAddress(String address) {
        return companyRepository.findByAddress(address)
                .orElseThrow(() -> new CompanyNotFoundException("Company with address " + address + " not found."));
    }

    public List<User> getUsersByCompanyId(Long id) {
        Company company = getCompanyById(id);
        return company.getUsers();
    }
}