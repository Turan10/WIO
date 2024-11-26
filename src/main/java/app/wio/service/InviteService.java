
package app.wio.service;

import app.wio.entity.Company;
import app.wio.entity.Invite;
import app.wio.exception.CompanyNotFoundException;
import app.wio.exception.InviteNotFoundException;
import app.wio.repository.CompanyRepository;
import app.wio.repository.InviteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class InviteService {

    private final InviteRepository inviteRepository;
    private final CompanyRepository companyRepository;

    @Autowired
    public InviteService(InviteRepository inviteRepository, CompanyRepository companyRepository) {
        this.inviteRepository = inviteRepository;
        this.companyRepository = companyRepository;
    }

    public Invite createInvite(Long companyId, int expirationInHours) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new CompanyNotFoundException("Company not found."));

        Invite invite = new Invite(company, expirationInHours);
        return inviteRepository.save(invite);
    }

    public Invite getInviteByToken(String token) {
        return inviteRepository.findByToken(token)
                .orElseThrow(() -> new InviteNotFoundException("Invalid or expired invite token."));
    }

    public void cleanUpExpiredInvites() {
        inviteRepository.deleteByExpiryDateBefore(LocalDateTime.now());
    }
}