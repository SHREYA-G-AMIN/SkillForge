package com.skillforge.service;

import com.skillforge.dto.ProfileDtos.ProfileResponse;
import com.skillforge.dto.ProfileDtos.SkillItem;
import com.skillforge.dto.SkillGapDtos.*;
import com.skillforge.entity.CareerRole;
import com.skillforge.repository.CareerRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SkillGapService {
    private final ProfileService profileService;
    private final CareerRoleRepository careerRoleRepository;

    public SkillGapResponse analyze(String email, String targetRole) {
        ProfileResponse p = profileService.getMyProfile(email);
        String role = (targetRole == null || targetRole.isBlank()) ? p.careerGoal() : targetRole;
        if (role == null || role.isBlank()) role = "Backend Developer";
        final String finalRole = role;
        CareerRole cr = careerRoleRepository.findByNameIgnoreCase(finalRole)
                .orElseThrow(() -> new com.skillforge.exception.ResourceNotFoundException("Career role not found: " + finalRole));
        List<SkillItem> profileSkills = p.skills() == null ? List.of() : p.skills();
        Set<String> have = profileSkills.stream()
                .filter(s -> s != null && s.name() != null)
                .map(s -> s.name().trim().toLowerCase())
                .filter((String n) -> !n.isEmpty())
                .collect(Collectors.toSet());
        List<String> required = cr.getRequiredSkills() == null ? List.of() : cr.getRequiredSkills();
        List<String> matched = required.stream().filter(s -> s != null && have.contains(s.toLowerCase())).toList();
        List<String> missing = required.stream().filter(s -> s == null || !have.contains(s.toLowerCase())).toList();
        int percent = required.isEmpty() ? 0 : (int) Math.round(matched.size() * 100.0 / required.size());
        List<RoadmapStep> roadmap = new ArrayList<>();
        for (int i = 0; i < missing.size(); i++) {
            String s = missing.get(i);
            String label = s == null ? "" : s;
            String q = URLEncoder.encode("learn " + label, StandardCharsets.UTF_8);
            roadmap.add(new RoadmapStep(i + 1, label, "Study and practice " + label, "https://www.google.com/search?q=" + q));
        }
        return new SkillGapResponse(cr.getName(), required, matched, missing, percent, roadmap);
    }

    public List<RoadmapStep> roadmap(String email, String targetRole) {
        return analyze(email, targetRole).roadmap();
    }

    public List<CareerRoleResponse> roles() {
        return careerRoleRepository.findAll().stream()
                .map(r -> new CareerRoleResponse(r.getId(), r.getName(), r.getDescription(), r.getRequiredSkills()))
                .toList();
    }
}