package com.skillforge.opportunity.integration;

import com.skillforge.entity.Skill;
import com.skillforge.repository.SkillRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@Primary
@RequiredArgsConstructor
public class JpaSkillCatalogGateway implements SkillCatalogGateway {

    private final SkillRepository skillRepository;

    @Override
    public boolean existsById(Long skillId) {
        return skillRepository.existsById(skillId);
    }

    @Override
    public String getSkillName(Long skillId) {
        return skillRepository.findById(skillId)
                .map(Skill::getName)
                .orElse("Skill #" + skillId);
    }

    @Override
    public Map<Long, String> getSkillNames(Iterable<Long> skillIds) {
        Map<Long, String> result = new HashMap<>();
        skillRepository.findAllById(skillIds)
                .forEach(skill -> result.put(skill.getId(), skill.getName()));
        return result;
    }
}
