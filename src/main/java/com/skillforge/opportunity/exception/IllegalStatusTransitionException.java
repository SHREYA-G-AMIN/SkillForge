package com.skillforge.opportunity.exception;

import com.skillforge.exception.BadRequestException;

public class IllegalStatusTransitionException extends BadRequestException {
    public IllegalStatusTransitionException(String message) {
        super(message);
    }
}
