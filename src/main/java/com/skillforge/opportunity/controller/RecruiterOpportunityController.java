package com.skillforge.opportunity.controller;

import com.skillforge.opportunity.dto.ApplicationDtos.ApplicantListItem;
import com.skillforge.opportunity.dto.OpportunityDtos.*;
import com.skillforge.opportunity.service.ApplicationService;
import com.skillforge.opportunity.service.OpportunityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recruiter/opportunities")
@RequiredArgsConstructor
public class RecruiterOpportunityController {

    private final OpportunityService opportunityService;
    private final ApplicationService applicationService;

    @GetMapping
    public List<OpportunitySummaryResponse> myPostings() {
        return opportunityService.getMyPostings();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OpportunityDetailResponse create(@Valid @RequestBody OpportunityCreateRequest request) {
        return opportunityService.createOpportunity(request);
    }

    @PutMapping("/{id}")
    public OpportunityDetailResponse update(@PathVariable Long id, @Valid @RequestBody OpportunityCreateRequest request) {
        return opportunityService.updateOpportunity(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        opportunityService.deleteOpportunity(id);
    }

    @RequestMapping(value = "/{id}/status", method = {RequestMethod.PUT, RequestMethod.PATCH})
    public OpportunitySummaryResponse setStatus(@PathVariable Long id, @Valid @RequestBody OpportunityStatusUpdateRequest req) {
        return opportunityService.setOpportunityStatus(id, req.status());
    }

    @GetMapping("/{id}/applicants")
    public List<ApplicantListItem> getApplicants(@PathVariable Long id) {
        return applicationService.getApplicantsForOpportunity(id);
    }
}
