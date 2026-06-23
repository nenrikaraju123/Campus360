package com.campus360.testsupport;

import com.campus360.institution.domain.Institution;
import com.campus360.institution.repository.InstitutionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class TestTenantFactory {

    @Autowired
    private InstitutionRepository institutionRepository;

    public Institution createTenant(String name) {
        Institution institution = new Institution();
        institution.setName(name);
        institution.setCode(UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        institution.setStatus("ACTIVE");
        institution.setType("UNIVERSITY");
        return institutionRepository.save(institution);
    }
}
