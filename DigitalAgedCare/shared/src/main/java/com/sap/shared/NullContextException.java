package com.sap.shared;

/**
 *
 */
public class NullContextException extends Exception {
    private static final String MESSAGE = "Context is null";

    public NullContextException() {
        super(MESSAGE);
    }

    public NullContextException(String errorMessage) {
        super(errorMessage);
    }
}
