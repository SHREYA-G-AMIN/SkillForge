package com.skillforge.service;

import com.skillforge.dto.ProfileDtos.ProfileResponse;
import com.skillforge.dto.SkillGapDtos.SkillGapResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SkillGapService {

    private final ProfileService profileService;

    private static final Map<String, List<String>> ROLE_SKILLS = Map.of(
            "backend developer", List.of("Java", "Spring Boot", "SQL", "REST API", "Git", "Docker"),
            "frontend developer", List.of("JavaScript", "React", "HTML", "CSS", "Git", "REST API"),
            "full stack developer", List.of("JavaScript", "React", "Java", "Spring Boot", "SQL", "Git"),
            "data scientist", List.of("Python", "Machine Learning", "SQL", "Statistics", "Pandas"),
            "devops engineer", List.of("Linux", "Docker", "Kubernetes", "CI/CD", "AWS", "Git")
    );

    public SkillGapResponse analyze(String email, String targetRole) {
        ProfileResponse p = profileService.getMyProfile(email);
        String role = (targetRole == null || targetRole.isBlank()) ? p.careerGoal() : targetRole;
        if (role == null) role = "Backend Developer";
        final String finalRole = role;
        String key = role.toLowerCase();
        List<String> required = ROLE_SKILLS.getOrDefault(key, ROLE_SKILLS.get("backend developer"));
        Set<String> have = p.skills().stream().map(s -> s.name().toLowerCase()).collect(Collectors.toSet());
        List<String> matched = required.stream().filter(s -> have.contains(s.toLowerCase())).toList();
        List<String> missing = required.stream().filter(s -> !have.contains(s.toLowerCase())).toList();
        int percent = required.isEmpty() ? 0 : (int) Math.round(matched.size() * 100.0 / required.size());
        List<String> recs = missing.stream().map(s -> "Learn " + s + " to close the gap for " + finalRole).toList();
        return new SkillGapResponse(finalRole, required, matched, missing, percent, recs);
    }
}