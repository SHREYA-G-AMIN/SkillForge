package com.skillforge.opportunity.exception;

import com.skillforge.exception.DuplicateResourceException;

public class DuplicateApplicationException extends DuplicateResourceException {
    public DuplicateApplicationException(String message) {
        super(message);
    }
}
