package com.skillforge.opportunity.service;

import com.skillforge.entity.Role;
import com.skillforge.entity.User;
import com.skillforge.opportunity.dto.MatchResult;
import com.skillforge.opportunity.exception.DuplicateApplicationException;
import com.skillforge.opportunity.exception.IllegalStatusTransitionException;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApplicationServiceTest {

    @Mock
    private ApplicationRepository applicationRepository;

    @Mock
    private OpportunityRepository opportunityRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CurrentUserProvider currentUserProvider;

    @Mock
    private StudentProfileGateway studentProfileGateway;

    @Mock
    private MatchService matchService;

    private ApplicationService applicationService;

    @BeforeEach
    void setUp() {
        applicationService = new ApplicationService(
                applicationRepository,
                opportunityRepository,
                userRepository,
                currentUserProvider,
                studentProfileGateway,
                matchService
        );
    }

    @Test
    @DisplayName("Duplicate Apply: throws DuplicateApplicationException when student already applied")
    void testDuplicateApplyThrows() {
        Long oppId = 1L;
        Long studentId = 50L;
        when(currentUserProvider.getCurrentUser()).thenReturn(new UserContext(studentId, "student@example.com", "STUDENT"));

        Opportunity opp = new Opportunity();
        opp.setId(oppId);
        opp.setStatus(OpportunityStatus.OPEN);
        opp.setDeadline(LocalDate.now().plusMonths(1));

        when(opportunityRepository.findById(oppId)).thenReturn(Optional.of(opp));
        when(applicationRepository.existsByOpportunityIdAndApplicantId(oppId, studentId)).thenReturn(true);

        assertThrows(DuplicateApplicationException.class, () -> applicationService.apply(oppId));
        verify(applicationRepository, never()).save(any());
    }

    @Test
    @DisplayName("Illegal Status Transition: throws IllegalStatusTransitionException when jumping from APPLIED to SELECTED")
    void testIllegalStatusTransitionThrows() {
        Long appId = 100L;
        Long recruiterId = 10L;

        when(currentUserProvider.getCurrentUser()).thenReturn(new UserContext(recruiterId, "recruiter@example.com", "RECRUITER"));

        User recruiter = new User();
        recruiter.setId(recruiterId);
        recruiter.setRole(Role.RECRUITER);

        Opportunity opp = new Opportunity();
        opp.setId(1L);
        opp.setRecruiter(recruiter);

        Application app = new Application();
        app.setId(appId);
        app.setOpportunity(opp);
        app.setStatus(ApplicationStatus.APPLIED); // can only transition to SHORTLISTED or REJECTED

        when(applicationRepository.findById(appId)).thenReturn(Optional.of(app));

        // Attempting to jump directly from APPLIED to SELECTED
        assertThrows(IllegalStatusTransitionException.class, () ->
                applicationService.updateApplicationStatus(appId, ApplicationStatus.SELECTED));

        verify(applicationRepository, never()).save(any());
    }

    @Test
    @DisplayName("Wrong Recruiter: throws UnauthorizedOpportunityAccessException when another recruiter manages applicants")
    void testWrongRecruiterThrows() {
        Long appId = 100L;
        Long ownerRecruiterId = 10L;
        Long unauthorizedRecruiterId = 99L;

        when(currentUserProvider.getCurrentUser()).thenReturn(new UserContext(unauthorizedRecruiterId, "intruder@example.com", "RECRUITER"));

        User owner = new User();
        owner.setId(ownerRecruiterId);

        Opportunity opp = new Opportunity();
        opp.setId(1L);
        opp.setRecruiter(owner);

        Application app = new Application();
        app.setId(appId);
        app.setOpportunity(opp);
        app.setStatus(ApplicationStatus.APPLIED);

        when(applicationRepository.findById(appId)).thenReturn(Optional.of(app));

        assertThrows(UnauthorizedOpportunityAccessException.class, () ->
                applicationService.updateApplicationStatus(appId, ApplicationStatus.SHORTLISTED));

        verify(applicationRepository, never()).save(any());
    }
}
