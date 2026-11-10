package com.skillforge.opportunity.dto;

import com.skillforge.opportunity.model.ApplicationStatus;
import com.skillforge.opportunity.model.OpportunityType;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.List;

public final class ApplicationDtos {
    private ApplicationDtos() {}

    public record ApplicationStatusUpdateRequest(
            @NotNull(message = "Status is required") ApplicationStatus status
    ) {}

    public record ApplicationResponse(
            Long id,
            Long opportunityId,
            String opportunityTitle,
            String company,
            String location,
            OpportunityType type,
            ApplicationStatus status,
            LocalDateTime appliedAt,
            LocalDateTime statusUpdatedAt
    ) {}

    public record ApplicantListItem(
            Long applicationId,
            Long applicantId,
            String studentName,
            String email,
            Double cgpa,
            String branch,
            Integer year,
            int matchPercentage,
            List<String> missingSkills,
            ApplicationStatus status,
            List<ApplicationStatus> legalNextStatuses,
            LocalDateTime appliedAt
    ) {}
}
