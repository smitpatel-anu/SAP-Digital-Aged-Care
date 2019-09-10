package com.sap.shared;

/**
 *
 */
public class NullSensorManagerException extends NullPointerException {
    private static final String MESSAGE = "SensorManager is null";

    public NullSensorManagerException() {
        super(MESSAGE);
    }

    public NullSensorManagerException(String errorMessage) {
        super(errorMessage);
    }
}
