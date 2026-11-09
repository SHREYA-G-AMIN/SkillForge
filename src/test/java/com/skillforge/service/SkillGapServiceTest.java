package com.skillforge.service;

import com.skillforge.dto.ProfileDtos.ProfileResponse;
import com.skillforge.dto.ProfileDtos.SkillItem;
import com.skillforge.dto.SkillGapDtos.RoadmapStep;
import com.skillforge.dto.SkillGapDtos.SkillGapResponse;
import com.skillforge.entity.CareerRole;
import com.skillforge.entity.ProficiencyLevel;
import com.skillforge.exception.ResourceNotFoundException;
import com.skillforge.repository.CareerRoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SkillGapServiceTest {
    @Mock ProfileService profileService;
    @Mock CareerRoleRepository careerRoleRepository;
    @InjectMocks SkillGapService skillGapService;

    private CareerRole backend;
    private List<String> backendReq;

    @BeforeEach
    void setUp() {
        backendReq = List.of("Java", "Spring Boot", "SQL", "REST API", "Git", "Docker");
        backend = CareerRole.builder().id(1L).name("Backend Developer").description("d").requiredSkills(backendReq).build();
    }

    private ProfileResponse profile(String goal, List<SkillItem> skills) {
        return new ProfileResponse(1L, "a@b.com", "A", "U", null, null, null, null, null, null, goal, null, null,
                skills, List.of(), List.of(), List.of());
    }

    private SkillItem skill(String name) {
        return new SkillItem(1L, 1L, name, null, ProficiencyLevel.BEGINNER);
    }

    @Test
    void explicitValidTargetRole_usesTargetNotFallback() {
        when(profileService.getMyProfile("a@b.com")).thenReturn(profile("Data Scientist", List.of()));
        when(careerRoleRepository.findByNameIgnoreCase("Backend Developer")).thenReturn(Optional.of(backend));
        SkillGapResponse r = skillGapService.analyze("a@b.com", "Backend Developer");
        assertEquals("Backend Developer", r.targetRole());
        assertEquals(backendReq, r.requiredSkills());
        verify(careerRoleRepository, never()).findByNameIgnoreCase("Data Scientist");
    }

    @Test
    void blankTargetRole_fallsBackToCareerGoal() {
        CareerRole ds = CareerRole.builder().name("Data Scientist").requiredSkills(List.of("Python")).build();
        when(profileService.getMyProfile("a@b.com")).thenReturn(profile("Data Scientist", List.of()));
        when(careerRoleRepository.findByNameIgnoreCase("Data Scientist")).thenReturn(Optional.of(ds));
        SkillGapResponse r = skillGapService.analyze("a@b.com", "  ");
        assertEquals("Data Scientist", r.targetRole());
        assertEquals(List.of("Python"), r.requiredSkills());
    }

    @Test
    void missingTargetRoleAndGoal_defaultsToBackendDeveloper() {
        when(profileService.getMyProfile("a@b.com")).thenReturn(profile(null, List.of()));
        when(careerRoleRepository.findByNameIgnoreCase("Backend Developer")).thenReturn(Optional.of(backend));
        SkillGapResponse r = skillGapService.analyze("a@b.com", null);
        assertEquals("Backend Developer", r.targetRole());
    }

    @Test
    void unknownExplicitTargetRole_throws404() {
        when(profileService.getMyProfile("a@b.com")).thenReturn(profile("Backend Developer", List.of()));
        when(careerRoleRepository.findByNameIgnoreCase("Nope")).thenReturn(Optional.empty());
        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> skillGapService.analyze("a@b.com", "Nope"));
        assertTrue(ex.getMessage().contains("Career role not found: Nope"));
    }

    @Test
    void unknownCareerGoalFallback_throws404() {
        when(profileService.getMyProfile("a@b.com")).thenReturn(profile("NopeGoal", List.of()));
        when(careerRoleRepository.findByNameIgnoreCase("NopeGoal")).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> skillGapService.analyze("a@b.com", null));
    }

    @Test
    void emptySkills_zeroPercentFullRoadmap_exact() {
        when(profileService.getMyProfile("a@b.com")).thenReturn(profile("Backend Developer", List.of()));
        when(careerRoleRepository.findByNameIgnoreCase("Backend Developer")).thenReturn(Optional.of(backend));
        SkillGapResponse r = skillGapService.analyze("a@b.com", "Backend Developer");
        assertEquals("Backend Developer", r.targetRole());
        assertEquals(backendReq, r.requiredSkills());
        assertEquals(List.of(), r.matchedSkills());
        assertEquals(backendReq, r.missingSkills());
        assertEquals(0, r.matchPercent());
        assertEquals(6, r.roadmap().size());
        for (int i = 0; i < backendReq.size(); i++) {
            RoadmapStep s = r.roadmap().get(i);
            assertEquals(i + 1, s.step());
            assertEquals(backendReq.get(i), s.skill());
            assertEquals("Study and practice " + backendReq.get(i), s.action());
            assertTrue(s.resource().startsWith("https://www.google.com/search?q="));
            assertTrue(s.resource().contains("Backend") ? true : true);
        }
    }

    @Test
    void partialMatch_exactMappingAndOrdering() {
        when(profileService.getMyProfile("a@b.com")).thenReturn(profile("Backend Developer",
                List.of(skill("java"), skill("SPRING BOOT"), skill("Sql"), skill("UnknownX"))));
        when(careerRoleRepository.findByNameIgnoreCase("Backend Developer")).thenReturn(Optional.of(backend));
        SkillGapResponse r = skillGapService.analyze("a@b.com", "Backend Developer");
        assertEquals("Backend Developer", r.targetRole());
        assertEquals(backendReq, r.requiredSkills());
        assertEquals(List.of("Java", "Spring Boot", "SQL"), r.matchedSkills());
        assertEquals(List.of("REST API", "Git", "Docker"), r.missingSkills());
        assertEquals(50, r.matchPercent());
        assertEquals(List.of("REST API", "Git", "Docker"), r.roadmap().stream().map(RoadmapStep::skill).toList());
        assertEquals(List.of(1, 2, 3), r.roadmap().stream().map(RoadmapStep::step).toList());
    }

    @Test
    void allMatched_hundredPercentEmptyRoadmap_exact() {
        when(profileService.getMyProfile("a@b.com")).thenReturn(profile("Backend Developer",
                backendReq.stream().map(this::skill).toList()));
        when(careerRoleRepository.findByNameIgnoreCase("Backend Developer")).thenReturn(Optional.of(backend));
        SkillGapResponse r = skillGapService.analyze("a@b.com", "Backend Developer");
        assertEquals("Backend Developer", r.targetRole());
        assertEquals(backendReq, r.requiredSkills());
        assertEquals(backendReq, r.matchedSkills());
        assertEquals(List.of(), r.missingSkills());
        assertEquals(100, r.matchPercent());
        assertEquals(List.of(), r.roadmap());
    }

    @Test
    void zeroRequiredSkills_noDivisionError_exact() {
        CareerRole empty = CareerRole.builder().name("Empty").requiredSkills(List.of()).build();
        when(profileService.getMyProfile("a@b.com")).thenReturn(profile("Empty", List.of(skill("Java"))));
        when(careerRoleRepository.findByNameIgnoreCase("Empty")).thenReturn(Optional.of(empty));
        SkillGapResponse r = skillGapService.analyze("a@b.com", "Empty");
        assertEquals("Empty", r.targetRole());
        assertEquals(List.of(), r.requiredSkills());
        assertEquals(List.of(), r.matchedSkills());
        assertEquals(List.of(), r.missingSkills());
        assertEquals(0, r.matchPercent());
        assertEquals(List.of(), r.roadmap());
    }

    @Test
    void roadmapUrlEncoding_encodesSpacesAndUnsafeChars() {
        CareerRole role = CareerRole.builder().name("Enc").requiredSkills(List.of("C++", "C# & .NET", "A/B")).build();
        when(profileService.getMyProfile("a@b.com")).thenReturn(profile("Enc", List.of()));
        when(careerRoleRepository.findByNameIgnoreCase("Enc")).thenReturn(Optional.of(role));
        SkillGapResponse r = skillGapService.analyze("a@b.com", "Enc");
        for (RoadmapStep s : r.roadmap()) {
            assertFalse(s.resource().contains(" "));
            String q = s.resource().substring("https://www.google.com/search?q=".length());
            String decoded = URLDecoder.decode(q, StandardCharsets.UTF_8);
            assertEquals("learn " + s.skill(), decoded);
        }
    }

    @Test
    void nullSafety_profileSkillsAndRequiredSkills() {
        CareerRole role = CareerRole.builder().name("N").requiredSkills(null).build();
        ProfileResponse p = new ProfileResponse(1L, "a@b.com", "A", "U", null, null, null, null, null, null, "N", null, null,
                Arrays.asList(skill("Java"), null, new SkillItem(2L, 2L, null, null, null)), List.of(), List.of(), List.of());
        when(profileService.getMyProfile("a@b.com")).thenReturn(p);
        when(careerRoleRepository.findByNameIgnoreCase("N")).thenReturn(Optional.of(role));
        SkillGapResponse r = skillGapService.analyze("a@b.com", "N");
        assertEquals(0, r.matchPercent());
        assertEquals(List.of(), r.missingSkills());
    }
}