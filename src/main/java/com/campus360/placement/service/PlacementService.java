package com.campus360.placement.service;

import com.campus360.placement.domain.*;
import com.campus360.placement.eligibility.EligibilityCriteria;
import com.campus360.placement.eligibility.EligibilityEngine;
import com.campus360.placement.eligibility.EligibilityResult;
import com.campus360.notification.domain.NotificationEvent;
import com.campus360.placement.repository.*;
import com.campus360.placement.web.*;
import com.campus360.platform.error.ApiException;
import com.campus360.platform.security.CurrentUser;
import com.campus360.platform.tenancy.TenantContext;
import com.campus360.student.domain.StudentProfile;
import com.campus360.student.repository.StudentProfileRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class PlacementService {

    private final CompanyRepository companies;
    private final JobPostingRepository postings;
    private final ApplicationRepository applications;
    private final OfferRepository offers;
    private final StudentProfileRepository students;
    private final CareerProfileRepository careerProfiles;
    private final EligibilityEngine eligibilityEngine;
    private final ObjectMapper objectMapper;
    private final ApplicationEventPublisher events;

    public PlacementService(CompanyRepository companies, JobPostingRepository postings,
                            ApplicationRepository applications, OfferRepository offers,
                            StudentProfileRepository students, CareerProfileRepository careerProfiles,
                            EligibilityEngine eligibilityEngine, ObjectMapper objectMapper,
                            ApplicationEventPublisher events) {
        this.companies = companies;
        this.postings = postings;
        this.applications = applications;
        this.offers = offers;
        this.students = students;
        this.careerProfiles = careerProfiles;
        this.eligibilityEngine = eligibilityEngine;
        this.objectMapper = objectMapper;
        this.events = events;
    }

    // ---- Career Profiles ----
    public CareerProfile updateCareerProfile(Long studentId, String resumeRef, String skills, String certifications, String projects) {
        Long tenant = TenantContext.requireTenantId();
        CareerProfile cp = careerProfiles.findByTenantIdAndStudentId(tenant, studentId)
                .orElseGet(() -> {
                    CareerProfile newCp = new CareerProfile();
                    newCp.setTenantId(tenant);
                    newCp.setStudentId(studentId);
                    return newCp;
                });
        cp.setResumeRef(resumeRef);
        cp.setSkills(skills);
        cp.setCertifications(certifications);
        cp.setProjects(projects);
        
        // rudimentary score
        int score = 0;
        if (resumeRef != null && !resumeRef.isBlank()) score += 20;
        if (skills != null && !skills.isBlank()) score += 30;
        if (projects != null && !projects.isBlank()) score += 30;
        if (certifications != null && !certifications.isBlank()) score += 20;
        cp.setReadinessScore(score);
        
        cp.setUpdatedAt(Instant.now());
        return careerProfiles.save(cp);
    }

    @Transactional(readOnly = true)
    public CareerProfile getCareerProfile(Long studentId) {
        return careerProfiles.findByTenantIdAndStudentId(TenantContext.requireTenantId(), studentId)
                .orElseThrow(() -> ApiException.notFound("Career profile not found for student " + studentId));
    }

    // ---- Companies ----
    public Company createCompany(CompanyRequest req) {
        Company c = new Company();
        c.setTenantId(TenantContext.requireTenantId());
        c.setName(req.name());
        c.setSector(req.sector());
        c.setTier(req.tier());
        c.setWebsite(req.website());
        c.setDescription(req.description());
        return companies.save(c);
    }

    public List<Company> listCompanies() {
        return companies.findByTenantId(TenantContext.requireTenantId());
    }

    // ---- Postings ----
    public JobPosting createPosting(PostingRequest req, Long postedByUserId) {
        Long tenant = TenantContext.requireTenantId();
        companies.findByIdAndTenantId(req.companyId(), tenant)
                .orElseThrow(() -> ApiException.badRequest("Unknown company: " + req.companyId()));

        JobPosting p = new JobPosting();
        p.setTenantId(tenant);
        p.setCompanyId(req.companyId());
        p.setTitle(req.title());
        if (req.type() != null) p.setType(req.type());
        p.setCtc(req.ctc());
        p.setLocation(req.location());
        p.setDescription(req.description());
        p.setEligibility(writeEligibility(req.eligibility()));
        p.setClosesAt(req.closesAt());
        p.setPostedBy(postedByUserId);
        JobPosting saved = postings.save(p);

        events.publishEvent(NotificationEvent.of(tenant, "JOB_POSTED",
                "New opportunity: " + saved.getTitle(),
                "A new posting is open. Check your eligibility and apply."));
        return saved;
    }

    public List<JobPosting> listPostings(boolean openOnly) {
        Long tenant = TenantContext.requireTenantId();
        return openOnly ? postings.findByTenantIdAndStatus(tenant, "OPEN") : postings.findByTenantId(tenant);
    }

    public JobPosting getPosting(Long id) {
        return postings.findByIdAndTenantId(id, TenantContext.requireTenantId())
                .orElseThrow(() -> ApiException.notFound("Posting not found: " + id));
    }

    /** The eligibility engine in action: who can apply to this posting right now. */
    public List<StudentProfile> eligibleStudents(Long postingId) {
        JobPosting posting = getPosting(postingId);
        EligibilityCriteria criteria = eligibilityEngine.parse(posting.getEligibility());
        return students.findByTenantId(posting.getTenantId()).stream()
                .filter(s -> eligibilityEngine.evaluate(criteria, s).eligible())
                .toList();
    }

    // ---- Applications ----
    public Application apply(Long postingId, Long studentId) {
        Long tenant = TenantContext.requireTenantId();
        JobPosting posting = getPosting(postingId);
        if (!"OPEN".equals(posting.getStatus())) {
            throw ApiException.badRequest("Posting is not open for applications");
        }
        StudentProfile student = students.findByIdAndTenantId(studentId, tenant)
                .orElseThrow(() -> ApiException.badRequest("Unknown student: " + studentId));

        EligibilityResult result = eligibilityEngine.evaluate(
                eligibilityEngine.parse(posting.getEligibility()), student);
        if (!result.eligible()) {
            throw ApiException.badRequest("Student not eligible: " + String.join("; ", result.reasons()));
        }
        if (applications.existsByPostingIdAndStudentId(postingId, studentId)) {
            throw ApiException.conflict("Student has already applied to this posting");
        }

        Application a = new Application();
        a.setTenantId(tenant);
        a.setPostingId(postingId);
        a.setStudentId(studentId);
        a.setStatus("APPLIED");
        return applications.save(a);
    }

    public List<Application> applicationsForPosting(Long postingId) {
        getPosting(postingId);
        return applications.findByTenantIdAndPostingId(TenantContext.requireTenantId(), postingId);
    }

    /** Applications belonging to the currently authenticated student. */
    public List<Application> myApplications() {
        Long tenant = TenantContext.requireTenantId();
        StudentProfile me = students.findByUserId(CurrentUser.id())
                .orElseThrow(() -> ApiException.notFound("No student profile for the current user"));
        return applications.findByTenantIdAndStudentId(tenant, me.getId());
    }

    /** Offers belonging to the currently authenticated student. */
    public List<Offer> myOffers() {
        Long tenant = TenantContext.requireTenantId();
        StudentProfile me = students.findByUserId(CurrentUser.id())
                .orElseThrow(() -> ApiException.notFound("No student profile for the current user"));
        List<Long> appIds = applications.findByTenantIdAndStudentId(tenant, me.getId())
                .stream().map(Application::getId).toList();
        return appIds.isEmpty() ? List.of() : offers.findByTenantIdAndApplicationIdIn(tenant, appIds);
    }

    public Application updateApplicationStatus(Long applicationId, String status) {
        Application a = applications.findByIdAndTenantId(applicationId, TenantContext.requireTenantId())
                .orElseThrow(() -> ApiException.notFound("Application not found: " + applicationId));
        a.setStatus(status.toUpperCase());
        a.setUpdatedAt(Instant.now());
        return applications.save(a);
    }

    // ---- Offers ----
    public Offer makeOffer(Long applicationId, OfferRequest req) {
        Long tenant = TenantContext.requireTenantId();
        Application a = applications.findByIdAndTenantId(applicationId, tenant)
                .orElseThrow(() -> ApiException.notFound("Application not found: " + applicationId));
        a.setStatus("OFFERED");
        a.setUpdatedAt(Instant.now());
        applications.save(a);

        Offer offer = new Offer();
        offer.setTenantId(tenant);
        offer.setApplicationId(applicationId);
        offer.setCtc(req.ctc());
        offer.setJoiningDate(req.joiningDate());
        offer.setStatus("EXTENDED");
        Offer saved = offers.save(offer);

        events.publishEvent(NotificationEvent.of(tenant, "OFFER_EXTENDED",
                "Offer extended",
                "An offer has been extended for application #" + applicationId + "."));
        return saved;
    }

    public Offer respondToOffer(Long offerId, String decision) {
        Offer offer = offers.findByIdAndTenantId(offerId, TenantContext.requireTenantId())
                .orElseThrow(() -> ApiException.notFound("Offer not found: " + offerId));
        String d = decision.toUpperCase();
        if (!d.equals("ACCEPT") && !d.equals("DECLINE")) {
            throw ApiException.badRequest("Decision must be ACCEPT or DECLINE");
        }
        offer.setStatus(d.equals("ACCEPT") ? "ACCEPTED" : "DECLINED");
        offer.setUpdatedAt(Instant.now());
        return offers.save(offer);
    }

    // ---- Analytics ----
    @Transactional(readOnly = true)
    public PlacementStats stats() {
        Long tenant = TenantContext.requireTenantId();
        long totalStudents = students.findByTenantId(tenant).size();

        List<Offer> allOffers = offers.findByTenantId(tenant);
        List<Offer> accepted = allOffers.stream().filter(o -> "ACCEPTED".equals(o.getStatus())).toList();

        Set<Long> placedStudentIds = accepted.stream()
                .map(o -> applications.findByIdAndTenantId(o.getApplicationId(), tenant).orElse(null))
                .filter(a -> a != null)
                .map(Application::getStudentId)
                .collect(Collectors.toSet());

        BigDecimal highest = accepted.stream()
                .map(Offer::getCtc).filter(c -> c != null)
                .max(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
        BigDecimal average = accepted.isEmpty() ? BigDecimal.ZERO : accepted.stream()
                .map(o -> o.getCtc() == null ? BigDecimal.ZERO : o.getCtc())
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(accepted.size()), 2, RoundingMode.HALF_UP);

        double rate = totalStudents == 0 ? 0.0
                : Math.round((placedStudentIds.size() * 10000.0) / totalStudents) / 100.0;

        long openPostings = postings.findByTenantIdAndStatus(tenant, "OPEN").size();

        return new PlacementStats(totalStudents, placedStudentIds.size(), rate, highest, average,
                openPostings, allOffers.size());
    }

    private String writeEligibility(EligibilityCriteria criteria) {
        if (criteria == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(criteria);
        } catch (Exception e) {
            throw ApiException.badRequest("Could not serialize eligibility criteria");
        }
    }
}
