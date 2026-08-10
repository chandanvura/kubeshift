package com.kubeshift.remediate.exception;

public class RemediationException extends RuntimeException {
    public RemediationException(String message) {
        super(message);
    }

    public RemediationException(String message, Throwable cause) {
        super(message, cause);
    }
}
