package com.skillforge.controller;

import com.skillforge.dto.ProfileDtos.SkillCreateRequest;
import com.skillforge.dto.ProfileDtos.SkillResponse;
import com.skillforge.service.SkillService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Master skill list. Other modules (Skill Gap, Matching) can reuse the Skill entity/repository. */
@RestController
@RequestMapping("/api/skills")
@RequiredArgsConstructor
public class SkillController {

    private final SkillService skillService;

    @GetMapping
    public List<SkillResponse> list(@RequestParam(required = false) String q) {
        return skillService.search(q);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public SkillResponse create(@Valid @RequestBody SkillCreateRequest req) {
        return skillService.create(req);
    }
}
