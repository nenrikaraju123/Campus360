package com.campus360.admissions.service;

import com.campus360.admissions.domain.AdmissionLead;
import com.campus360.admissions.repository.AdmissionLeadRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdmissionLeadService {

    private final AdmissionLeadRepository leadRepository;

    @Transactional
    public AdmissionLead assignLead(Long tenantId, Long leadId, Long assignedTo, String actorId) {
        AdmissionLead lead = leadRepository.findByIdAndTenantId(leadId, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Lead not found"));
        
        lead.setAssignedTo(assignedTo);
        // We could also record a history here if we wanted to track lead assignments
        return leadRepository.save(lead);
    }

    @Transactional
    public AdmissionLead updateLeadStatus(Long tenantId, Long leadId, String status, String actorId) {
        AdmissionLead lead = leadRepository.findByIdAndTenantId(leadId, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Lead not found"));
        
        lead.setStatus(status);
        return leadRepository.save(lead);
    }
}
