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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SkillGapServiceTest {
    @Mock ProfileService profileService;
    @Mock CareerRoleRepository careerRoleRepository;
    @InjectMocks SkillGapService skillGapService;

    private CareerRole backend;

    @BeforeEach
    void setUp() {
        backend = CareerRole.builder().id(1L).name("Backend Developer").description("d")
                .requiredSkills(List.of("Java", "Spring Boot", "SQL", "REST API", "Git", "Docker")).build();
    }

    private ProfileResponse profile(String goal, List<SkillItem> skills) {
        return new ProfileResponse(1L, "a@b.com", "A", "U", null, null, null, null, null, null, goal, null, null,
                skills, List.of(), List.of(), List.of());
    }

    private SkillItem skill(String name) {
        return new SkillItem(1L, 1L, name, null, ProficiencyLevel.BEGINNER);
    }

    @Test
    void unknownTargetRole_throws404() {
        when(profileService.getMyProfile("a@b.com")).thenReturn(profile("Backend Developer", List.of()));
        when(careerRoleRepository.findByNameIgnoreCase("Nope")).thenReturn(Optional.empty());
        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> skillGapService.analyze("a@b.com", "Nope"));
        assertTrue(ex.getMessage().contains("Nope"));
    }

    @Test
    void emptySkills_zeroPercentFullRoadmap() {
        when(profileService.getMyProfile("a@b.com")).thenReturn(profile("Backend Developer", List.of()));
        when(careerRoleRepository.findByNameIgnoreCase("Backend Developer")).thenReturn(Optional.of(backend));
        SkillGapResponse r = skillGapService.analyze("a@b.com", "Backend Developer");
        assertEquals(0, r.matchPercent());
        assertTrue(r.matchedSkills().isEmpty());
        assertEquals(6, r.missingSkills().size());
        assertEquals(6, r.roadmap().size());
        assertEquals(1, r.roadmap().get(0).step());
        assertEquals("Java", r.roadmap().get(0).skill());
    }

    @Test
    void partialMatch_percentAndRoadmap() {
        when(profileService.getMyProfile("a@b.com")).thenReturn(profile("Backend Developer",
                List.of(skill("Java"), skill("Spring Boot"), skill("SQL"))));
        when(careerRoleRepository.findByNameIgnoreCase("Backend Developer")).thenReturn(Optional.of(backend));
        SkillGapResponse r = skillGapService.analyze("a@b.com", "Backend Developer");
        assertEquals(50, r.matchPercent());
        assertEquals(List.of("Java", "Spring Boot", "SQL"), r.matchedSkills());
        assertEquals(3, r.roadmap().size());
    }

    @Test
    void allMatched_hundredPercentEmptyRoadmap() {
        when(profileService.getMyProfile("a@b.com")).thenReturn(profile("Backend Developer",
                backend.getRequiredSkills().stream().map(this::skill).toList()));
        when(careerRoleRepository.findByNameIgnoreCase("Backend Developer")).thenReturn(Optional.of(backend));
        SkillGapResponse r = skillGapService.analyze("a@b.com", "Backend Developer");
        assertEquals(100, r.matchPercent());
        assertTrue(r.missingSkills().isEmpty());
        assertTrue(r.roadmap().isEmpty());
    }

    @Test
    void zeroRequiredSkills_noDivisionError() {
        CareerRole empty = CareerRole.builder().name("Empty").requiredSkills(List.of()).build();
        when(profileService.getMyProfile("a@b.com")).thenReturn(profile("Empty", List.of(skill("Java"))));
        when(careerRoleRepository.findByNameIgnoreCase("Empty")).thenReturn(Optional.of(empty));
        SkillGapResponse r = skillGapService.analyze("a@b.com", "Empty");
        assertEquals(0, r.matchPercent());
        assertTrue(r.roadmap().isEmpty());
    }
}