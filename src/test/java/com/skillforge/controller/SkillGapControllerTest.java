package com.skillforge.controller;

import com.skillforge.config.SecurityConfig;
import com.skillforge.dto.SkillGapDtos.CareerRoleResponse;
import com.skillforge.dto.SkillGapDtos.RoadmapStep;
import com.skillforge.dto.SkillGapDtos.SkillGapResponse;
import com.skillforge.exception.GlobalExceptionHandler;
import com.skillforge.exception.ResourceNotFoundException;
import com.skillforge.repository.RevokedTokenRepository;
import com.skillforge.security.JwtAuthFilter;
import com.skillforge.security.JwtService;
import com.skillforge.service.SkillGapService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SkillGapController.class)
@Import({SecurityConfig.class, JwtAuthFilter.class, GlobalExceptionHandler.class})
class SkillGapControllerTest {
    @Autowired MockMvc mvc;
    @MockBean SkillGapService skillGapService;
    @MockBean JwtService jwtService;
    @MockBean RevokedTokenRepository revokedTokenRepository;
    @MockBean org.springframework.security.core.userdetails.UserDetailsService userDetailsService;

    @Test
    void roles_isPublic_andReturnsJson() throws Exception {
        when(skillGapService.roles()).thenReturn(List.of(
                new CareerRoleResponse(1L, "Backend Developer", "d", List.of("Java", "SQL"))));
        mvc.perform(get("/api/skill-gap/roles"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Backend Developer"))
                .andExpect(jsonPath("$[0].requiredSkills[0]").value("Java"));
    }

    @Test
    void skillGap_unauthenticated_returns401() throws Exception {
        mvc.perform(get("/api/skill-gap").param("targetRole", "Backend Developer"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void roadmap_unauthenticated_returns401() throws Exception {
        mvc.perform(get("/api/skill-gap/roadmap").param("targetRole", "Backend Developer"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "a@b.com")
    void unknownTargetRole_returns404Json() throws Exception {
        when(skillGapService.analyze(eq("a@b.com"), eq("Nope")))
                .thenThrow(new ResourceNotFoundException("Career role not found: Nope"));
        mvc.perform(get("/api/skill-gap").param("targetRole", "Nope"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Career role not found: Nope"));
    }

    @Test
    @WithMockUser(username = "a@b.com")
    void skillGap_authenticated_returnsJson() throws Exception {
        SkillGapResponse resp = new SkillGapResponse("Backend Developer", List.of("Java", "SQL"),
                List.of("Java"), List.of("SQL"), 50,
                List.of(new RoadmapStep(1, "SQL", "Study and practice SQL", "https://www.google.com/search?q=learn+SQL")));
        when(skillGapService.analyze("a@b.com", "Backend Developer")).thenReturn(resp);
        mvc.perform(get("/api/skill-gap").param("targetRole", "Backend Developer"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.targetRole").value("Backend Developer"))
                .andExpect(jsonPath("$.requiredSkills[1]").value("SQL"))
                .andExpect(jsonPath("$.matchedSkills[0]").value("Java"))
                .andExpect(jsonPath("$.missingSkills[0]").value("SQL"))
                .andExpect(jsonPath("$.matchPercent").value(50))
                .andExpect(jsonPath("$.roadmap[0].skill").value("SQL"));
    }

    @Test
    @WithMockUser(username = "a@b.com")
    void roadmap_authenticated_returnsJsonArray() throws Exception {
        when(skillGapService.roadmap("a@b.com", "Backend Developer")).thenReturn(
                List.of(new RoadmapStep(1, "SQL", "Study and practice SQL", "https://www.google.com/search?q=learn+SQL")));
        mvc.perform(get("/api/skill-gap/roadmap").param("targetRole", "Backend Developer"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].step").value(1))
                .andExpect(jsonPath("$[0].skill").value("SQL"));
    }
}