package com.skillforge.service;

import com.skillforge.dto.ProfileDtos.ProfileResponse;
import com.skillforge.dto.SkillGapDtos.*;
import com.skillforge.entity.CareerRole;
import com.skillforge.repository.CareerRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
        if (role == null) role = "Backend Developer";
        CareerRole cr = careerRoleRepository.findByNameIgnoreCase(role)
                .orElseGet(() -> careerRoleRepository.findByNameIgnoreCase("Backend Developer").orElseThrow());
        Set<String> have = p.skills().stream().map(s -> s.name().toLowerCase()).collect(Collectors.toSet());
        List<String> required = cr.getRequiredSkills();
        List<String> matched = required.stream().filter(s -> have.contains(s.toLowerCase())).toList();
        List<String> missing = required.stream().filter(s -> !have.contains(s.toLowerCase())).toList();
        int percent = required.isEmpty() ? 0 : (int) Math.round(matched.size() * 100.0 / required.size());
        List<RoadmapStep> roadmap = new ArrayList<>();
        for (int i = 0; i < missing.size(); i++) {
            String s = missing.get(i);
            roadmap.add(new RoadmapStep(i + 1, s, "Study and practice " + s, "https://www.google.com/search?q=learn+" + s.replace(" ", "+")));
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