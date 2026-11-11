package com.skillforge.opportunity.integration;

import java.util.Map;

public record StudentProfileData(
        Long profileId,
        Long userId,
        String name,
        String email,
        Double cgpa,
        String branch,
        Integer year,
        Map<Long, Integer> skillLevels
) {}
