package com.skillforge.opportunity.model;

import java.util.Collections;
import java.util.List;

public enum ApplicationStatus {
    APPLIED,
    SHORTLISTED,
    INTERVIEW,
    SELECTED,
    REJECTED;

    public List<ApplicationStatus> legalNextStatuses() {
        return switch (this) {
            case APPLIED -> List.of(SHORTLISTED, REJECTED);
            case SHORTLISTED -> List.of(INTERVIEW, REJECTED);
            case INTERVIEW -> List.of(SELECTED, REJECTED);
            case SELECTED, REJECTED -> Collections.emptyList();
        };
    }

    public boolean canTransitionTo(ApplicationStatus next) {
        return legalNextStatuses().contains(next);
    }
}
