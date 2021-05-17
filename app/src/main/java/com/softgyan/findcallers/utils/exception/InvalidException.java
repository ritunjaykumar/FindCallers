package com.softgyan.findcallers.utils.exception;

import androidx.annotation.Nullable;

public class InvalidException extends Exception {
    private final String errorMessage;

    public InvalidException(String errorException) {
        super(errorException);
        this.errorMessage = errorException;
    }

    @Nullable
    @Override
    public String getMessage() {
        return errorMessage;
    }
}
