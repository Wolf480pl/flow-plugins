package com.flowpowered.plugins.artifact;

public class ArtifactGoneException extends ArtifactException {
    private static final long serialVersionUID = 4663481841118454245L;

    public ArtifactGoneException() {
    }

    public ArtifactGoneException(String message) {
        super(message);
    }

    public ArtifactGoneException(Throwable cause) {
        super(cause);
    }

    public ArtifactGoneException(String message, Throwable cause) {
        super(message, cause);
    }

}
