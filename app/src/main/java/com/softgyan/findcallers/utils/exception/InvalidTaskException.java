package com.softgyan.findcallers.utils.exception;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class InvalidTaskException extends Exception {

    private final String errorMessage;

    public InvalidTaskException(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    @NonNull
    @Override
    public String toString() {
        return errorMessage;
    }

    @Nullable
    @Override
    public String getMessage() {
        return errorMessage;
    }


}
