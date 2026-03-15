package com.openclaw.test.service;

import com.openclaw.test.dto.IdentityCreateRequest;
import com.openclaw.test.dto.IdentityResponse;
import com.openclaw.test.entity.Identity;
import com.openclaw.test.entity.IdentityType;
import com.openclaw.test.exception.IdentityNotFoundException;
import com.openclaw.test.repository.IdentityRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class IdentityService {

    private final IdentityRepository identityRepository;

    public IdentityService(IdentityRepository identityRepository) {
        this.identityRepository = identityRepository;
    }

    @Transactional
    public IdentityResponse createIdentity(IdentityCreateRequest request) {
        Identity identity = new Identity();
        identity.setName(request.getName());
        identity.setType(request.getType());

        Identity savedIdentity = identityRepository.save(identity);
        return IdentityResponse.fromEntity(savedIdentity);
    }

    public Page<IdentityResponse> getIdentities(int page, int size, IdentityType type) {
        Pageable pageable = PageRequest.of(page, size);

        Page<Identity> identityPage;
        if (type != null) {
            identityPage = identityRepository.findByType(type, pageable);
        } else {
            identityPage = identityRepository.findAll(pageable);
        }

        return identityPage.map(IdentityResponse::fromEntity);
    }

    public IdentityResponse getIdentityById(Long id) {
        Identity identity = identityRepository.findById(id)
                .orElseThrow(() -> new IdentityNotFoundException(id));
        return IdentityResponse.fromEntity(identity);
    }

    @Transactional
    public void deleteIdentity(Long id) {
        if (!identityRepository.existsById(id)) {
            throw new IdentityNotFoundException(id);
        }
        identityRepository.deleteById(id);
    }

    @Transactional
    public IdentityResponse regenerateApiKey(Long id) {
        Identity identity = identityRepository.findById(id)
                .orElseThrow(() -> new IdentityNotFoundException(id));

        identity.setApiKey("sk-" + java.util.UUID.randomUUID().toString().replace("-", ""));

        Identity savedIdentity = identityRepository.save(identity);
        return IdentityResponse.fromEntity(savedIdentity);
    }
}
