package com.skillforge.controller;

import com.skillforge.dto.ProfileDtos.*;
import com.skillforge.service.ProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    // ----- profile -----
    @GetMapping("/me")
    public ProfileResponse me(Authentication auth) {
        return profileService.getMyProfile(auth.getName());
    }

    @PutMapping("/me")
    public ProfileResponse update(Authentication auth, @Valid @RequestBody ProfileUpdateRequest req) {
        return profileService.updateMyProfile(auth.getName(), req);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ProfileResponse byId(@PathVariable Long id) {
        return profileService.getProfileById(id);
    }

    // ----- skills -----
    @PostMapping("/skills")
    @ResponseStatus(HttpStatus.CREATED)
    public SkillItem addSkill(Authentication auth, @Valid @RequestBody SkillRequest req) {
        return profileService.addSkill(auth.getName(), req);
    }

    @PutMapping("/skills/{id}")
    public SkillItem updateSkill(Authentication auth, @PathVariable Long id,
                                 @Valid @RequestBody SkillLevelRequest req) {
        return profileService.updateSkill(auth.getName(), id, req);
    }

    @DeleteMapping("/skills/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSkill(Authentication auth, @PathVariable Long id) {
        profileService.deleteSkill(auth.getName(), id);
    }

    // ----- projects -----
    @PostMapping("/projects")
    @ResponseStatus(HttpStatus.CREATED)
    public ProjectItem addProject(Authentication auth, @Valid @RequestBody ProjectRequest req) {
        return profileService.addProject(auth.getName(), req);
    }

    @PutMapping("/projects/{id}")
    public ProjectItem updateProject(Authentication auth, @PathVariable Long id,
                                     @Valid @RequestBody ProjectRequest req) {
        return profileService.updateProject(auth.getName(), id, req);
    }

    @DeleteMapping("/projects/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProject(Authentication auth, @PathVariable Long id) {
        profileService.deleteProject(auth.getName(), id);
    }

    // ----- certifications -----
    @PostMapping("/certifications")
    @ResponseStatus(HttpStatus.CREATED)
    public CertificationItem addCertification(Authentication auth, @Valid @RequestBody CertificationRequest req) {
        return profileService.addCertification(auth.getName(), req);
    }

    @PutMapping("/certifications/{id}")
    public CertificationItem updateCertification(Authentication auth, @PathVariable Long id,
                                                 @Valid @RequestBody CertificationRequest req) {
        return profileService.updateCertification(auth.getName(), id, req);
    }

    @DeleteMapping("/certifications/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCertification(Authentication auth, @PathVariable Long id) {
        profileService.deleteCertification(auth.getName(), id);
    }

    // ----- experience -----
    @PostMapping("/experience")
    @ResponseStatus(HttpStatus.CREATED)
    public ExperienceItem addExperience(Authentication auth, @Valid @RequestBody ExperienceRequest req) {
        return profileService.addExperience(auth.getName(), req);
    }

    @PutMapping("/experience/{id}")
    public ExperienceItem updateExperience(Authentication auth, @PathVariable Long id,
                                           @Valid @RequestBody ExperienceRequest req) {
        return profileService.updateExperience(auth.getName(), id, req);
    }

    @DeleteMapping("/experience/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteExperience(Authentication auth, @PathVariable Long id) {
        profileService.deleteExperience(auth.getName(), id);
    }
}
