package com.skillforge.opportunity.dto;

import com.skillforge.opportunity.model.OpportunityStatus;
import com.skillforge.opportunity.model.OpportunityType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public final class OpportunityDtos {
    private OpportunityDtos() {}

    public record SkillRequirementDto(
            @NotNull(message = "Skill is required") Long skillId,
            String skillName,
            @NotNull(message = "Level is required") @Min(value = 1, message = "Level must be between 1 and 5") @Max(value = 5, message = "Level must be between 1 and 5") Integer requiredLevel
    ) {}

    public record OpportunityCreateRequest(
            @NotBlank(message = "Title is required") @Size(max = 255) String title,
            @NotBlank(message = "Company is required") @Size(max = 255) String company,
            @NotBlank(message = "Location is required") @Size(max = 255) String location,
            @NotNull(message = "Type is required") OpportunityType type,
            @NotBlank(message = "Description is required") @Size(max = 4000) String description,
            @NotNull(message = "Deadline is required") @FutureOrPresent(message = "Deadline must be today or in the future") LocalDate deadline,
            @DecimalMin(value = "0.0", message = "Min CGPA must be at least 0.0") @DecimalMax(value = "10.0", message = "Min CGPA cannot exceed 10.0") Double minCgpa,
            List<String> eligibleBranches,
            List<Integer> eligibleYears,
            @Valid List<SkillRequirementDto> requiredSkills
    ) {}

    public record OpportunityStatusUpdateRequest(
            @NotNull(message = "Status is required") OpportunityStatus status
    ) {}

    public record OpportunitySummaryResponse(
            Long id,
            String title,
            String company,
            String location,
            OpportunityType type,
            OpportunityStatus status,
            LocalDate deadline,
            Double minCgpa,
            List<String> eligibleBranches,
            List<Integer> eligibleYears,
            Integer matchPercentage,
            Boolean eligible,
            Boolean bookmarked,
            Long applicantCount,
            LocalDateTime createdAt
    ) {}

    public record OpportunityDetailResponse(
            Long id,
            String title,
            String company,
            String location,
            OpportunityType type,
            OpportunityStatus status,
            String description,
            LocalDate deadline,
            Double minCgpa,
            List<String> eligibleBranches,
            List<Integer> eligibleYears,
            List<MatchResult.SkillRequirementDetail> requiredSkills,
            List<MatchResult.SkillRequirementDetail> matchedSkills,
            List<MatchResult.SkillRequirementDetail> missingSkills,
            Integer matchPercentage,
            Boolean eligible,
            List<String> eligibilityReasons,
            Boolean bookmarked,
            Boolean applied,
            String applicationStatus,
            Long applicantCount,
            LocalDateTime createdAt,
            Long recruiterId
    ) {}
}
