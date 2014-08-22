package com.flowpowered.plugins.artifact;

public class ArtifactException extends Exception {
    private static final long serialVersionUID = -8322435989885948569L;

    public ArtifactException() {
    }

    public ArtifactException(String message) {
        super(message);
    }

    public ArtifactException(Throwable cause) {
        super(cause);
    }

    public ArtifactException(String message, Throwable cause) {
        super(message, cause);
    }

    protected ArtifactException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
