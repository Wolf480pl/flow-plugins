package com.flowpowered.plugins.artifact;

public class WrongStateException extends ArtifactException {
    private static final long serialVersionUID = 689148316829068917L;
    private final ArtifactState state;

    public WrongStateException(ArtifactState state) {
        super("operation impossible for artifact state " + state);
        this.state = state;
    }

    public WrongStateException(String message) {
        super(message);
        this.state = null;
    }

    public WrongStateException(String message, ArtifactState state) {
        super(message + " - operation impossible for artifact state " + state);
        this.state = state;
    }

    public ArtifactState getState() {
        return state;
    }

}
