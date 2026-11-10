package com.skillforge.opportunity.service;

import com.skillforge.entity.User;
import com.skillforge.exception.BadRequestException;
import com.skillforge.exception.ResourceNotFoundException;
import com.skillforge.opportunity.dto.ApplicationDtos.*;
import com.skillforge.opportunity.dto.MatchResult;
import com.skillforge.opportunity.exception.DuplicateApplicationException;
import com.skillforge.opportunity.exception.IllegalStatusTransitionException;
import com.skillforge.opportunity.exception.IneligibleApplicationException;
import com.skillforge.opportunity.exception.UnauthorizedOpportunityAccessException;
import com.skillforge.opportunity.integration.CurrentUserProvider;
import com.skillforge.opportunity.integration.StudentProfileData;
import com.skillforge.opportunity.integration.StudentProfileGateway;
import com.skillforge.opportunity.integration.UserContext;
import com.skillforge.opportunity.model.Application;
import com.skillforge.opportunity.model.ApplicationStatus;
import com.skillforge.opportunity.model.Opportunity;
import com.skillforge.opportunity.model.OpportunityStatus;
import com.skillforge.opportunity.repository.ApplicationRepository;
import com.skillforge.opportunity.repository.OpportunityRepository;
import com.skillforge.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final OpportunityRepository opportunityRepository;
    private final UserRepository userRepository;
    private final CurrentUserProvider currentUserProvider;
    private final StudentProfileGateway studentProfileGateway;
    private final MatchService matchService;

    @Transactional
    public ApplicationResponse apply(Long opportunityId) {
        UserContext current = currentUserProvider.getCurrentUser();
        if (!"STUDENT".equalsIgnoreCase(current.role())) {
            throw new BadRequestException("Only students can apply to opportunities");
        }

        Opportunity opp = opportunityRepository.findById(opportunityId)
                .orElseThrow(() -> new ResourceNotFoundException("Opportunity not found with id: " + opportunityId));

        if (opp.getStatus() != OpportunityStatus.OPEN) {
            throw new BadRequestException("This opportunity is no longer open for applications");
        }

        if (opp.getDeadline() != null && LocalDate.now().isAfter(opp.getDeadline())) {
            throw new BadRequestException("The deadline for this opportunity has passed");
        }

        if (applicationRepository.existsByOpportunityIdAndApplicantId(opportunityId, current.userId())) {
            throw new DuplicateApplicationException("You have already applied to this opportunity");
        }

        StudentProfileData profile = studentProfileGateway.getStudentProfile(current.userId());
        MatchResult match = matchService.computeMatch(opp, profile);
        if (!match.eligible()) {
            throw new IneligibleApplicationException("You are not eligible: " + String.join("; ", match.eligibilityReasons()));
        }

        User applicant = userRepository.getReferenceById(current.userId());
        Application application = new Application();
        application.setOpportunity(opp);
        application.setApplicant(applicant);
        application.setStatus(ApplicationStatus.APPLIED);
        application.setAppliedAt(LocalDateTime.now());
        application.setStatusUpdatedAt(LocalDateTime.now());

        Application saved = applicationRepository.save(application);
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<ApplicationResponse> getMyApplications() {
        UserContext current = currentUserProvider.getCurrentUser();
        List<Application> apps = applicationRepository.findByApplicantIdOrderByAppliedAtDesc(current.userId());
        return apps.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ApplicantListItem> getApplicantsForOpportunity(Long opportunityId) {
        UserContext current = currentUserProvider.getCurrentUser();
        Opportunity opp = opportunityRepository.findById(opportunityId)
                .orElseThrow(() -> new ResourceNotFoundException("Opportunity not found with id: " + opportunityId));

        validateRecruiterOwnership(opp, current);

        List<Application> apps = applicationRepository.findByOpportunityIdOrderByAppliedAtDesc(opportunityId);
        return apps.stream().map(app -> {
            StudentProfileData profile = studentProfileGateway.getStudentProfile(app.getApplicant().getId());
            MatchResult match = matchService.computeMatch(opp, profile);

            List<String> missingSkillNames = match.missingSkills().stream()
                    .map(MatchResult.SkillRequirementDetail::skillName)
                    .collect(Collectors.toList());

            String studentName = profile != null ? profile.name() : app.getApplicant().getEmail();
            Double cgpa = profile != null ? profile.cgpa() : null;
            String branch = profile != null ? profile.branch() : null;
            Integer year = profile != null ? profile.year() : null;

            return new ApplicantListItem(
                    app.getId(),
                    app.getApplicant().getId(),
                    studentName,
                    app.getApplicant().getEmail(),
                    cgpa,
                    branch,
                    year,
                    match.matchPercentage(),
                    missingSkillNames,
                    app.getStatus(),
                    app.getStatus().legalNextStatuses(),
                    app.getAppliedAt()
            );
        }).collect(Collectors.toList());
    }

    @Transactional
    public ApplicantListItem updateApplicationStatus(Long applicationId, ApplicationStatus newStatus) {
        UserContext current = currentUserProvider.getCurrentUser();
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found with id: " + applicationId));

        Opportunity opp = application.getOpportunity();
        validateRecruiterOwnership(opp, current);

        ApplicationStatus currentStatus = application.getStatus();
        if (!currentStatus.canTransitionTo(newStatus)) {
            throw new IllegalStatusTransitionException("Cannot transition application from "
                    + currentStatus + " to " + newStatus
                    + ". Legal next transitions: " + currentStatus.legalNextStatuses());
        }

        application.setStatus(newStatus);
        application.setStatusUpdatedAt(LocalDateTime.now());
        Application saved = applicationRepository.save(application);

        StudentProfileData profile = studentProfileGateway.getStudentProfile(saved.getApplicant().getId());
        MatchResult match = matchService.computeMatch(opp, profile);
        List<String> missingSkillNames = match.missingSkills().stream()
                .map(MatchResult.SkillRequirementDetail::skillName)
                .collect(Collectors.toList());

        String studentName = profile != null ? profile.name() : saved.getApplicant().getEmail();

        return new ApplicantListItem(
                saved.getId(),
                saved.getApplicant().getId(),
                studentName,
                saved.getApplicant().getEmail(),
                profile != null ? profile.cgpa() : null,
                profile != null ? profile.branch() : null,
                profile != null ? profile.year() : null,
                match.matchPercentage(),
                missingSkillNames,
                saved.getStatus(),
                saved.getStatus().legalNextStatuses(),
                saved.getAppliedAt()
        );
    }

    private void validateRecruiterOwnership(Opportunity opp, UserContext current) {
        if (!opp.getRecruiter().getId().equals(current.userId()) && !"ADMIN".equalsIgnoreCase(current.role())) {
            throw new UnauthorizedOpportunityAccessException("You do not have permission to view or manage applicants for this opportunity");
        }
    }

    private ApplicationResponse mapToResponse(Application app) {
        Opportunity opp = app.getOpportunity();
        return new ApplicationResponse(
                app.getId(),
                opp.getId(),
                opp.getTitle(),
                opp.getCompany(),
                opp.getLocation(),
                opp.getType(),
                app.getStatus(),
                app.getAppliedAt(),
                app.getStatusUpdatedAt()
        );
    }
}
