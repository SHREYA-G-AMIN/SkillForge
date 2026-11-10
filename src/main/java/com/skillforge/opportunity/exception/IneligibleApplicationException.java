package com.skillforge.opportunity.exception;

import com.skillforge.exception.BadRequestException;

public class IneligibleApplicationException extends BadRequestException {
    public IneligibleApplicationException(String message) {
        super(message);
    }
}
