package com.skillforge.service;

import com.skillforge.dto.ProfileDtos.SkillCreateRequest;
import com.skillforge.dto.ProfileDtos.SkillResponse;
import com.skillforge.entity.Skill;
import com.skillforge.exception.DuplicateResourceException;
import com.skillforge.repository.SkillRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SkillService {

    private final SkillRepository skillRepository;

    public List<SkillResponse> search(String query) {
        List<Skill> skills = (query == null || query.isBlank())
                ? skillRepository.findAll()
                : skillRepository.findByNameContainingIgnoreCaseOrderByNameAsc(query.trim());
        return skills.stream().map(s -> new SkillResponse(s.getId(), s.getName(), s.getCategory())).toList();
    }

    public SkillResponse create(SkillCreateRequest req) {
        String name = req.name().trim();
        if (skillRepository.existsByNameIgnoreCase(name)) {
            throw new DuplicateResourceException("Skill already exists: " + name);
        }
        Skill saved = skillRepository.save(new Skill(name, req.category()));
        return new SkillResponse(saved.getId(), saved.getName(), saved.getCategory());
    }
}
