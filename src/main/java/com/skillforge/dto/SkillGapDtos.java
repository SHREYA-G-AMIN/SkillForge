package com.skillforge.dto;

import java.util.List;

public class SkillGapDtos {
    public record SkillGapResponse(
            String targetRole,
            List<String> requiredSkills,
            List<String> matchedSkills,
            List<String> missingSkills,
            int matchPercent,
            List<String> recommendations
    ) {}
}