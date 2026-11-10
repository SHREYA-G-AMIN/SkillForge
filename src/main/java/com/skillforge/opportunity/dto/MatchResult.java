package com.skillforge.opportunity.dto;

import java.util.List;

public record MatchResult(
        int matchPercentage,
        boolean eligible,
        List<String> eligibilityReasons,
        List<SkillRequirementDetail> matchedSkills,
        List<SkillRequirementDetail> missingSkills
) {
    public record SkillRequirementDetail(
            Long skillId,
            String skillName,
            int requiredLevel,
            Integer studentLevel,
            boolean satisfied
    ) {}
}
