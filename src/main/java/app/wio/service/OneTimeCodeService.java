package app.wio.service;

import app.wio.entity.Company;
import app.wio.entity.OneTimeCode;
import app.wio.exception.CompanyNotFoundException;
import app.wio.exception.ResourceNotFoundException;
import app.wio.repository.CompanyRepository;
import app.wio.repository.OneTimeCodeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OneTimeCodeService {

    private final OneTimeCodeRepository oneTimeCodeRepository;
    private final CompanyRepository companyRepository;

    @Autowired
    public OneTimeCodeService(
            OneTimeCodeRepository oneTimeCodeRepository,
            CompanyRepository companyRepository
    ) {
        this.oneTimeCodeRepository = oneTimeCodeRepository;
        this.companyRepository = companyRepository;
    }

    public OneTimeCode generateCode(Long companyId, int expirationInHours) {
        companyRepository.findById(companyId)
                .orElseThrow(() -> new CompanyNotFoundException("Company not found."));
        OneTimeCode code = new OneTimeCode(companyId, expirationInHours);
        return oneTimeCodeRepository.save(code);
    }

    public OneTimeCode getCodeByValue(String codeValue) {
        OneTimeCode code = oneTimeCodeRepository.findByCode(codeValue)
                .orElseThrow(() ->
                        new ResourceNotFoundException("One-time code not found or invalid."));
        if (code.isExpired()) {
            throw new ResourceNotFoundException("One-time code is expired.");
        }
        return code;
    }

    public void cleanUpExpiredCodes() {
        oneTimeCodeRepository.deleteByExpiryDateBefore(LocalDateTime.now());
    }

    public List<OneTimeCode> getActiveCodesForCompany(Long companyId) {
        List<OneTimeCode> all = oneTimeCodeRepository.findAll();
        return all.stream()
                .filter(code -> code.getCompanyId().equals(companyId) && !code.isExpired())
                .collect(Collectors.toList());
    }
}