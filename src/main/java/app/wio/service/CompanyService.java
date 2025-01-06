package app.wio.service;

import app.wio.dto.CompanyCreationDto;
import app.wio.dto.response.CompanyDto;
import app.wio.dto.response.UserResponseDto;
import app.wio.entity.Company;
import app.wio.entity.User;
import app.wio.exception.CompanyNotFoundException;
import app.wio.exception.ResourceConflictException;
import app.wio.mapper.CompanyMapper;
import app.wio.mapper.UserMapper;
import app.wio.repository.CompanyRepository;
import app.wio.repository.UserRepository;
import app.wio.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final CompanyMapper companyMapper;
    private final UserMapper userMapper;
    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    @Autowired
    public CompanyService(
            CompanyRepository companyRepository,
            CompanyMapper companyMapper,
            UserMapper userMapper,
            UserService userService,
            JwtTokenProvider jwtTokenProvider,
            UserRepository userRepository
    ) {
        this.companyRepository = companyRepository;
        this.companyMapper = companyMapper;
        this.userMapper = userMapper;
        this.userService = userService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.userRepository = userRepository;
    }

    @Transactional
    public CompanyDto createCompany(CompanyCreationDto companyDto) {
        companyRepository.findByName(companyDto.getName()).ifPresent(c -> {
            throw new ResourceConflictException("A company with this name already exists.");
        });

        Company company = new Company();
        company.setName(companyDto.getName());
        company.setAddress(companyDto.getAddress());
        Company savedCompany = companyRepository.save(company);


        User adminUser = userService.createAdminForCompany(
                savedCompany,
                companyDto.getAdminName(),
                companyDto.getAdminEmail(),
                companyDto.getAdminPassword()
        );
        userRepository.flush();
        savedCompany.getUsers().add(adminUser);


        String adminToken = jwtTokenProvider.generateToken(adminUser);

        CompanyDto companyResponse = companyMapper.toDto(savedCompany);
        companyResponse.setAdminToken(adminToken);
        return companyResponse;
    }

    public Company getCompanyById(Long id) {
        return companyRepository.findById(id)
                .orElseThrow(() ->
                        new CompanyNotFoundException("Company with ID " + id + " not found."));
    }

    public CompanyDto getCompanyDtoById(Long id) {
        Company company = getCompanyById(id);
        return companyMapper.toDto(company);
    }

    public List<UserResponseDto> getUsersByCompanyId(Long id) {
        Company company = getCompanyById(id);
        List<User> users = company.getUsers();
        return users.stream()
                .map(userMapper::toDto)
                .collect(Collectors.toList());
    }
}
