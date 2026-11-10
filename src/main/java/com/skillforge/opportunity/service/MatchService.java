package com.skillforge.opportunity.service;

import com.skillforge.opportunity.dto.MatchResult;
import com.skillforge.opportunity.dto.MatchResult.SkillRequirementDetail;
import com.skillforge.opportunity.integration.SkillCatalogGateway;
import com.skillforge.opportunity.integration.StudentProfileData;
import com.skillforge.opportunity.model.Opportunity;
import com.skillforge.opportunity.model.OpportunitySkillRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MatchService {

    private final SkillCatalogGateway skillCatalogGateway;

    /**
     * Computes the match percentage, eligibility status, and breakdown of matched/missing skills
     * for a given student profile against an opportunity.
     */
    public MatchResult computeMatch(Opportunity opportunity, StudentProfileData profile) {
        List<OpportunitySkillRequirement> requirements = opportunity.getRequiredSkills() != null
                ? opportunity.getRequiredSkills()
                : Collections.emptyList();

        List<SkillRequirementDetail> matchedSkills = new ArrayList<>();
        List<SkillRequirementDetail> missingSkills = new ArrayList<>();

        Map<Long, Integer> studentSkills = (profile != null && profile.skillLevels() != null)
                ? profile.skillLevels()
                : Collections.emptyMap();

        for (OpportunitySkillRequirement req : requirements) {
            String skillName = skillCatalogGateway.getSkillName(req.getSkillId());
            Integer studentLevel = studentSkills.get(req.getSkillId());
            boolean satisfied = studentLevel != null && studentLevel >= req.getRequiredLevel();

            SkillRequirementDetail detail = new SkillRequirementDetail(
                    req.getSkillId(),
                    skillName,
                    req.getRequiredLevel(),
                    studentLevel,
                    satisfied
            );

            if (satisfied) {
                matchedSkills.add(detail);
            } else {
                missingSkills.add(detail);
            }
        }

        int matchPercentage;
        if (requirements.isEmpty()) {
            matchPercentage = 100;
        } else {
            matchPercentage = (int) Math.round(((double) matchedSkills.size() / requirements.size()) * 100.0);
        }

        List<String> eligibilityReasons = new ArrayList<>();

        // 1. Deadline check
        if (opportunity.getDeadline() != null && LocalDate.now().isAfter(opportunity.getDeadline())) {
            eligibilityReasons.add("Application deadline has passed (" + opportunity.getDeadline() + ")");
        }

        // 2. CGPA check
        if (opportunity.getMinCgpa() != null) {
            if (profile == null || profile.cgpa() == null) {
                eligibilityReasons.add("Requires minimum CGPA of " + opportunity.getMinCgpa() + " (not specified in your profile)");
            } else if (profile.cgpa() < opportunity.getMinCgpa()) {
                eligibilityReasons.add("Requires minimum CGPA of " + opportunity.getMinCgpa() + " (your CGPA: " + profile.cgpa() + ")");
            }
        }

        // 3. Branch check
        List<String> eligibleBranches = parseCommaSeparatedStrings(opportunity.getEligibleBranches());
        if (!eligibleBranches.isEmpty()) {
            if (profile == null || profile.branch() == null || profile.branch().isBlank()) {
                eligibilityReasons.add("Eligible branches: " + String.join(", ", eligibleBranches) + " (branch not specified in profile)");
            } else {
                boolean branchMatched = eligibleBranches.stream()
                        .anyMatch(b -> b.equalsIgnoreCase(profile.branch().trim()));
                if (!branchMatched) {
                    eligibilityReasons.add("Requires branch in [" + String.join(", ", eligibleBranches) + "] (your branch: " + profile.branch() + ")");
                }
            }
        }

        // 4. Academic year check
        List<Integer> eligibleYears = parseCommaSeparatedIntegers(opportunity.getEligibleYears());
        if (!eligibleYears.isEmpty()) {
            if (profile == null || profile.year() == null) {
                eligibilityReasons.add("Eligible years: " + eligibleYears + " (year/semester not specified in profile)");
            } else if (!eligibleYears.contains(profile.year())) {
                eligibilityReasons.add("Requires academic year in " + eligibleYears + " (your year: " + profile.year() + ")");
            }
        }

        boolean eligible = eligibilityReasons.isEmpty();

        return new MatchResult(
                matchPercentage,
                eligible,
                eligibilityReasons,
                matchedSkills,
                missingSkills
        );
    }

    public static List<String> parseCommaSeparatedStrings(String value) {
        if (value == null || value.isBlank()) return Collections.emptyList();
        return Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    public static List<Integer> parseCommaSeparatedIntegers(String value) {
        if (value == null || value.isBlank()) return Collections.emptyList();
        List<Integer> list = new ArrayList<>();
        for (String part : value.split(",")) {
            try {
                list.add(Integer.parseInt(part.trim()));
            } catch (NumberFormatException ignored) {}
        }
        return list;
    }
}
