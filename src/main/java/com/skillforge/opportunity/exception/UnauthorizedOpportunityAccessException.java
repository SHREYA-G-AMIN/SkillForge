package com.skillforge.opportunity.exception;

import org.springframework.security.access.AccessDeniedException;

public class UnauthorizedOpportunityAccessException extends AccessDeniedException {
    public UnauthorizedOpportunityAccessException(String message) {
        super(message);
    }
}
