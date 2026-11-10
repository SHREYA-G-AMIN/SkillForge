package com.skillforge.opportunity.controller;

import com.skillforge.opportunity.dto.ApplicationDtos.ApplicationResponse;
import com.skillforge.opportunity.dto.OpportunityDtos.OpportunityDetailResponse;
import com.skillforge.opportunity.dto.OpportunityDtos.OpportunitySummaryResponse;
import com.skillforge.opportunity.model.OpportunityType;
import com.skillforge.opportunity.service.ApplicationService;
import com.skillforge.opportunity.service.OpportunityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class OpportunityController {

    private final OpportunityService opportunityService;
    private final ApplicationService applicationService;

    // ----- Public / Student Browse & Search -----
    @GetMapping("/api/opportunities")
    public List<OpportunitySummaryResponse> list(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) OpportunityType type,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) Long skillId
    ) {
        return opportunityService.searchOpportunities(q, type, location, skillId);
    }

    @GetMapping("/api/opportunities/{id}")
    public OpportunityDetailResponse getById(@PathVariable Long id) {
        return opportunityService.getOpportunityDetails(id);
    }

    // ----- Student Application & Bookmarks -----
    @PostMapping("/api/opportunities/{id}/apply")
    @ResponseStatus(HttpStatus.CREATED)
    public ApplicationResponse apply(@PathVariable Long id) {
        return applicationService.apply(id);
    }

    @PostMapping("/api/opportunities/{id}/bookmark")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void addBookmark(@PathVariable Long id) {
        opportunityService.addBookmark(id);
    }

    @DeleteMapping("/api/opportunities/{id}/bookmark")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeBookmark(@PathVariable Long id) {
        opportunityService.removeBookmark(id);
    }

    @GetMapping("/api/profile/bookmarks")
    public List<OpportunitySummaryResponse> getBookmarks() {
        return opportunityService.getBookmarks();
    }

    @GetMapping("/api/profile/applications")
    public List<ApplicationResponse> getMyApplications() {
        return applicationService.getMyApplications();
    }
}
