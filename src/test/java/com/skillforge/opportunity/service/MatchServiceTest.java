package com.skillforge.opportunity.service;

import com.skillforge.opportunity.dto.MatchResult;
import com.skillforge.opportunity.integration.SkillCatalogGateway;
import com.skillforge.opportunity.integration.StudentProfileData;
import com.skillforge.opportunity.model.Opportunity;
import com.skillforge.opportunity.model.OpportunitySkillRequirement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MatchServiceTest {

    @Mock
    private SkillCatalogGateway skillCatalogGateway;

    private MatchService matchService;

    @BeforeEach
    void setUp() {
        matchService = new MatchService(skillCatalogGateway);
    }

    private Opportunity createSampleOpportunity(LocalDate deadline, Double minCgpa, String branches, String years) {
        Opportunity opp = new Opportunity();
        opp.setId(10L);
        opp.setTitle("Software Engineer");
        opp.setCompany("Acme Corp");
        opp.setLocation("Bangalore");
        opp.setDeadline(deadline);
        opp.setMinCgpa(minCgpa);
        opp.setEligibleBranches(branches);
        opp.setEligibleYears(years);

        OpportunitySkillRequirement req1 = new OpportunitySkillRequirement(opp, 1L, 3);
        OpportunitySkillRequirement req2 = new OpportunitySkillRequirement(opp, 2L, 4);
        opp.getRequiredSkills().addAll(List.of(req1, req2));
        return opp;
    }

    @Test
    @DisplayName("Full Match: student meets all skill levels and eligibility criteria")
    void testFullMatch() {
        when(skillCatalogGateway.getSkillName(1L)).thenReturn("Java");
        when(skillCatalogGateway.getSkillName(2L)).thenReturn("Spring Boot");

        Opportunity opp = createSampleOpportunity(LocalDate.now().plusMonths(1), 7.5, "Computer Science, Information Science", "3, 4");
        StudentProfileData profile = new StudentProfileData(
                1L, 100L, "Alice", "alice@example.com", 8.5, "Computer Science", 4, Map.of(1L, 4, 2L, 4)
        );

        MatchResult result = matchService.computeMatch(opp, profile);

        assertEquals(100, result.matchPercentage());
        assertTrue(result.eligible());
        assertTrue(result.eligibilityReasons().isEmpty());
        assertEquals(2, result.matchedSkills().size());
        assertEquals(0, result.missingSkills().size());
    }

    @Test
    @DisplayName("Partial Match: student meets 1 of 2 required skills")
    void testPartialMatch() {
        when(skillCatalogGateway.getSkillName(anyLong())).thenReturn("Sample Skill");

        Opportunity opp = createSampleOpportunity(LocalDate.now().plusMonths(1), 7.0, null, null);
        // Student has skill 1 at level 3 (meets req 3), but skill 2 at level 2 (below req 4)
        StudentProfileData profile = new StudentProfileData(
                1L, 100L, "Bob", "bob@example.com", 7.5, "Computer Science", 4, Map.of(1L, 3, 2L, 2)
        );

        MatchResult result = matchService.computeMatch(opp, profile);

        assertEquals(50, result.matchPercentage());
        assertTrue(result.eligible());
        assertEquals(1, result.matchedSkills().size());
        assertEquals(1, result.missingSkills().size());
    }

    @Test
    @DisplayName("No Skills Required: returns 100% match")
    void testNoSkillsRequired() {
        Opportunity opp = new Opportunity();
        opp.setDeadline(LocalDate.now().plusWeeks(2));
        opp.setMinCgpa(6.0);

        StudentProfileData profile = new StudentProfileData(
                1L, 100L, "Charlie", "charlie@example.com", 7.0, "Mechanical", 2, Map.of()
        );

        MatchResult result = matchService.computeMatch(opp, profile);

        assertEquals(100, result.matchPercentage());
        assertTrue(result.eligible());
        assertEquals(0, result.matchedSkills().size());
        assertEquals(0, result.missingSkills().size());
    }

    @Test
    @DisplayName("Ineligible CGPA: student CGPA lower than minimum required")
    void testIneligibleCgpa() {
        when(skillCatalogGateway.getSkillName(anyLong())).thenReturn("Sample");

        Opportunity opp = createSampleOpportunity(LocalDate.now().plusMonths(1), 8.0, null, null);
        StudentProfileData profile = new StudentProfileData(
                1L, 100L, "Dave", "dave@example.com", 7.2, "Computer Science", 3, Map.of(1L, 5, 2L, 5)
        );

        MatchResult result = matchService.computeMatch(opp, profile);

        assertFalse(result.eligible());
        assertTrue(result.eligibilityReasons().stream().anyMatch(r -> r.contains("CGPA")));
    }

    @Test
    @DisplayName("Ineligible Branch: student branch not in allowed list")
    void testIneligibleBranch() {
        when(skillCatalogGateway.getSkillName(anyLong())).thenReturn("Sample");

        Opportunity opp = createSampleOpportunity(LocalDate.now().plusMonths(1), 6.0, "Computer Science, Information Science", null);
        StudentProfileData profile = new StudentProfileData(
                1L, 100L, "Eve", "eve@example.com", 8.0, "Civil Engineering", 3, Map.of(1L, 5, 2L, 5)
        );

        MatchResult result = matchService.computeMatch(opp, profile);

        assertFalse(result.eligible());
        assertTrue(result.eligibilityReasons().stream().anyMatch(r -> r.contains("branch")));
    }

    @Test
    @DisplayName("Ineligible Academic Year: student year not in allowed list")
    void testIneligibleYear() {
        when(skillCatalogGateway.getSkillName(anyLong())).thenReturn("Sample");

        Opportunity opp = createSampleOpportunity(LocalDate.now().plusMonths(1), 6.0, null, "3, 4");
        StudentProfileData profile = new StudentProfileData(
                1L, 100L, "Frank", "frank@example.com", 8.0, "Computer Science", 1, Map.of(1L, 5, 2L, 5)
        );

        MatchResult result = matchService.computeMatch(opp, profile);

        assertFalse(result.eligible());
        assertTrue(result.eligibilityReasons().stream().anyMatch(r -> r.contains("year")));
    }

    @Test
    @DisplayName("Ineligible Deadline: opportunity deadline has already passed")
    void testIneligibleDeadline() {
        when(skillCatalogGateway.getSkillName(anyLong())).thenReturn("Sample");

        Opportunity opp = createSampleOpportunity(LocalDate.now().minusDays(1), 6.0, null, null);
        StudentProfileData profile = new StudentProfileData(
                1L, 100L, "Grace", "grace@example.com", 8.0, "Computer Science", 3, Map.of(1L, 5, 2L, 5)
        );

        MatchResult result = matchService.computeMatch(opp, profile);

        assertFalse(result.eligible());
        assertTrue(result.eligibilityReasons().stream().anyMatch(r -> r.contains("deadline")));
    }
}
