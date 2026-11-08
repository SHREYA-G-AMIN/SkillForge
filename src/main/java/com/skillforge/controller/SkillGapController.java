package com.skillforge.controller;

import com.skillforge.dto.SkillGapDtos.SkillGapResponse;
import com.skillforge.service.SkillGapService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/skill-gap")
@RequiredArgsConstructor
public class SkillGapController {
    private final SkillGapService skillGapService;

    @GetMapping
    public SkillGapResponse analyze(Authentication auth, @RequestParam(required = false) String targetRole) {
        return skillGapService.analyze(auth.getName(), targetRole);
    }
}