package com.germanfica.wsfe.exception;

/**
 * Exception thrown when an environment variable cannot be accessed due to security restrictions.
 */
public class EnvironmentAccessException extends RuntimeException {
    private final String variableName;

    public EnvironmentAccessException(String variableName, Throwable cause) {
        super("Unable to access environment variable '" + variableName + "': " + cause.getMessage(), cause);
        this.variableName = variableName;
    }

    public String getVariableName() {
        return variableName;
    }
}
