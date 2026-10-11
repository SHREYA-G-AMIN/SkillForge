package com.skillforge.dto;

import com.skillforge.entity.ProficiencyLevel;
import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.util.List;

public final class ProfileDtos {
    private ProfileDtos() {}

    // ----- requests -----
    public record ProfileUpdateRequest(
            @NotBlank @Size(max = 100) String name,
            @NotBlank @Size(max = 30) String usn,
            @Size(max = 150) String college,
            @Size(max = 100) String degree,
            @Size(max = 100) String branch,
            @Min(1) @Max(8) Integer semester,
            @DecimalMin("0.0") @DecimalMax("10.0") Double cgpa,
            @Pattern(regexp = "^$|^[0-9]{10}$", message = "Phone must be 10 digits") String phone,
            @Size(max = 100) String careerGoal,
            @Size(max = 500) String careerInterests,
            @Size(max = 500) @Pattern(regexp = "^$|^https?://\\S+$", message = "Resume URL must start with http:// or https://") String resumeUrl) {}

    public record SkillRequest(@NotNull Long skillId, @NotNull ProficiencyLevel level) {}

    public record SkillLevelRequest(@NotNull ProficiencyLevel level) {}

    public record ProjectRequest(
            @NotBlank String title,
            @Size(max = 1000) String description,
            String techUsed,
            String link) {}

    public record CertificationRequest(
            @NotBlank String name,
            String issuer,
            LocalDate issueDate) {}

    public record ExperienceRequest(
            @NotBlank String company,
            @NotBlank String role,
            String duration,
            @Size(max = 1000) String description) {}

    public record SkillCreateRequest(@NotBlank String name, String category) {}

    // ----- responses -----
    public record SkillResponse(Long id, String name, String category) {}

    public record SkillItem(Long id, Long skillId, String name, String category, ProficiencyLevel level) {}

    public record ProjectItem(Long id, String title, String description, String techUsed, String link) {}

    public record CertificationItem(Long id, String name, String issuer, LocalDate issueDate) {}

    public record ExperienceItem(Long id, String company, String role, String duration, String description) {}

    public record ProfileResponse(
            Long id, String email, String name, String usn, String college, String degree,
            String branch, Integer semester, Double cgpa, String phone,
            String careerGoal, String careerInterests, String resumeUrl,
            List<SkillItem> skills, List<ProjectItem> projects,
            List<CertificationItem> certifications, List<ExperienceItem> experiences) {}
}
