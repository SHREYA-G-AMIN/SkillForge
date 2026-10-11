package com.skillforge.repository;

import com.skillforge.entity.Experience;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ExperienceRepository extends JpaRepository<Experience, Long> {
    Optional<Experience> findByIdAndProfileId(Long id, Long profileId);
}
