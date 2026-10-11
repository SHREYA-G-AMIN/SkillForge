package com.skillforge.repository;

import com.skillforge.entity.Skill;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SkillRepository extends JpaRepository<Skill, Long> {
    boolean existsByNameIgnoreCase(String name);
    List<Skill> findByNameContainingIgnoreCaseOrderByNameAsc(String name);
}
