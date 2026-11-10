package com.skillforge.opportunity.integration;

import com.skillforge.entity.ProfileSkill;
import com.skillforge.entity.StudentProfile;
import com.skillforge.repository.StudentProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Component
@Primary
@RequiredArgsConstructor
public class JpaStudentProfileGateway implements StudentProfileGateway {

    private final StudentProfileRepository studentProfileRepository;

    @Override
    @Transactional(readOnly = true)
    public StudentProfileData getStudentProfile(Long userId) {
        return studentProfileRepository.findByUserId(userId)
                .map(this::mapToData)
                .orElse(null);
    }

    private StudentProfileData mapToData(StudentProfile profile) {
        Integer year = profile.getSemester() != null ? (profile.getSemester() + 1) / 2 : null;

        Map<Long, Integer> skillLevels = new HashMap<>();
        if (profile.getSkills() != null) {
            for (ProfileSkill ps : profile.getSkills()) {
                if (ps.getSkill() != null && ps.getLevel() != null) {
                    int numericLevel = switch (ps.getLevel()) {
                        case BEGINNER -> 2;
                        case INTERMEDIATE -> 3;
                        case ADVANCED -> 5;
                    };
                    skillLevels.put(ps.getSkill().getId(), numericLevel);
                }
            }
        }

        return new StudentProfileData(
                profile.getId(),
                profile.getUser().getId(),
                profile.getName(),
                profile.getUser().getEmail(),
                profile.getCgpa(),
                profile.getBranch(),
                year,
                skillLevels
        );
    }
}
