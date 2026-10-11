package com.skillforge.repository;

import com.skillforge.entity.ProfileSkill;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProfileSkillRepository extends JpaRepository<ProfileSkill, Long> {
    boolean existsByProfileIdAndSkillId(Long profileId, Long skillId);
    Optional<ProfileSkill> findByIdAndProfileId(Long id, Long profileId);
}
