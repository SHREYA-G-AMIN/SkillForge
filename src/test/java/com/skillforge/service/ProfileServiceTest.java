package com.skillforge.service;

import com.skillforge.dto.ProfileDtos.*;
import com.skillforge.entity.*;
import com.skillforge.exception.DuplicateResourceException;
import com.skillforge.exception.ResourceNotFoundException;
import com.skillforge.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProfileServiceTest {

    @Mock StudentProfileRepository profileRepository;
    @Mock SkillRepository skillRepository;
    @Mock ProfileSkillRepository profileSkillRepository;
    @Mock ProjectRepository projectRepository;
    @Mock CertificationRepository certificationRepository;
    @Mock ExperienceRepository experienceRepository;
    @InjectMocks ProfileService profileService;

    private StudentProfile profile;

    @BeforeEach
    void setUp() {
        User user = new User();
        user.setEmail("a@b.com");
        profile = new StudentProfile();
        profile.setId(10L);
        profile.setUser(user);
        profile.setName("Asha");
        profile.setUsn("NNM24CS001");
    }

    private void profileExists() {
        when(profileRepository.findByUserEmail("a@b.com")).thenReturn(Optional.of(profile));
    }

    @Test
    void getMyProfile_returnsMappedProfile() {
        profileExists();

        ProfileResponse res = profileService.getMyProfile("a@b.com");

        assertEquals("Asha", res.name());
        assertEquals("a@b.com", res.email());
        assertNull(res.resumeUrl());
        assertTrue(res.skills().isEmpty());
    }

    @Test
    void getMyProfile_missingProfile_throwsNotFound() {
        when(profileRepository.findByUserEmail("a@b.com")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> profileService.getMyProfile("a@b.com"));
    }

    @Test
    void updateMyProfile_updatesFields() {
        profileExists();
        when(profileRepository.saveAndFlush(any(StudentProfile.class))).thenAnswer(i -> i.getArgument(0));

        ProfileResponse res = profileService.updateMyProfile("a@b.com",
                new ProfileUpdateRequest("Asha K", "nnm24cs001", "NMAMIT", "B.E.", "CSE", 5, 8.4, "9876543210",
                        "Backend Developer", "Java, Cloud", "https://example.com/resume.pdf"));

        assertEquals("Asha K", res.name());
        assertEquals("NNM24CS001", res.usn());
        assertEquals("Backend Developer", res.careerGoal());
        assertEquals(5, res.semester());
        assertEquals("NMAMIT", res.college());
        assertEquals("https://example.com/resume.pdf", res.resumeUrl());
    }

    @Test
    void updateMyProfile_blankOptionalFields_storedAsNull() {
        profileExists();
        when(profileRepository.saveAndFlush(any(StudentProfile.class))).thenAnswer(i -> i.getArgument(0));

        ProfileResponse res = profileService.updateMyProfile("a@b.com",
                new ProfileUpdateRequest("Asha", "NNM24CS001", "", "", "", null, null, "", "", "", ""));

        assertNull(res.college());
        assertNull(res.phone());
        assertNull(res.resumeUrl());
    }

    @Test
    void updateMyProfile_usnUsedByAnotherStudent_throwsConflict() {
        profileExists();
        when(profileRepository.existsByUsnAndIdNot("NNM24CS999", 10L)).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> profileService.updateMyProfile("a@b.com",
                new ProfileUpdateRequest("Asha", "nnm24cs999", null, null, null, null, null, null, null, null, null)));
        verify(profileRepository, never()).saveAndFlush(any());
    }

    @Test
    void addSkill_success() {
        profileExists();
        Skill java = new Skill("Java", "Programming");
        java.setId(1L);
        when(skillRepository.findById(1L)).thenReturn(Optional.of(java));
        when(profileSkillRepository.existsByProfileIdAndSkillId(10L, 1L)).thenReturn(false);
        when(profileSkillRepository.save(any(ProfileSkill.class))).thenAnswer(i -> i.getArgument(0));

        SkillItem item = profileService.addSkill("a@b.com", new SkillRequest(1L, ProficiencyLevel.INTERMEDIATE));

        assertEquals("Java", item.name());
        assertEquals(ProficiencyLevel.INTERMEDIATE, item.level());
    }

    @Test
    void addSkill_duplicate_throwsConflict() {
        profileExists();
        Skill java = new Skill("Java", "Programming");
        java.setId(1L);
        when(skillRepository.findById(1L)).thenReturn(Optional.of(java));
        when(profileSkillRepository.existsByProfileIdAndSkillId(10L, 1L)).thenReturn(true);

        assertThrows(DuplicateResourceException.class,
                () -> profileService.addSkill("a@b.com", new SkillRequest(1L, ProficiencyLevel.BEGINNER)));
        verify(profileSkillRepository, never()).save(any());
    }

    @Test
    void addSkill_unknownSkill_throwsNotFound() {
        profileExists();
        when(skillRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> profileService.addSkill("a@b.com", new SkillRequest(99L, ProficiencyLevel.BEGINNER)));
    }

    @Test
    void deleteProject_ofAnotherStudent_throwsNotFound() {
        profileExists();
        // Lookup is scoped by profileId, so someone else's project id yields empty
        when(projectRepository.findByIdAndProfileId(5L, 10L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> profileService.deleteProject("a@b.com", 5L));
        verify(projectRepository, never()).delete(any());
    }

    @Test
    void addProject_success() {
        profileExists();
        when(projectRepository.save(any(Project.class))).thenAnswer(i -> i.getArgument(0));

        ProjectItem item = profileService.addProject("a@b.com",
                new ProjectRequest("SkillForge", "Career platform", "Java, Spring", "https://github.com/x"));

        assertEquals("SkillForge", item.title());
    }
}
