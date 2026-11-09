package com.skillforge.dto;

import java.util.List;

public class SkillGapDtos {
    public record RoadmapStep(int step, String skill, String action, String resource) {}

    public record SkillGapResponse(
            String targetRole,
            List<String> requiredSkills,
            List<String> matchedSkills,
            List<String> missingSkills,
            int matchPercent,
            List<RoadmapStep> roadmap
    ) {}

    public record CareerRoleResponse(Long id, String name, String description, List<String> requiredSkills) {}
}