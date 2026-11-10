package com.skillforge.opportunity.controller;

import com.skillforge.opportunity.dto.ApplicationDtos.ApplicantListItem;
import com.skillforge.opportunity.dto.ApplicationDtos.ApplicationStatusUpdateRequest;
import com.skillforge.opportunity.service.ApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/applications")
@RequiredArgsConstructor
public class RecruiterApplicationController {

    private final ApplicationService applicationService;

    @RequestMapping(value = "/{id}/status", method = {RequestMethod.PUT, RequestMethod.PATCH})
    public ApplicantListItem updateStatus(@PathVariable Long id, @Valid @RequestBody ApplicationStatusUpdateRequest req) {
        return applicationService.updateApplicationStatus(id, req.status());
    }
}
