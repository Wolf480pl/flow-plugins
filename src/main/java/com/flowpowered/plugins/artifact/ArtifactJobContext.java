package com.flowpowered.plugins.artifact;


public interface ArtifactJobContext {

    void load() throws ArtifactException;

    void resolve() throws ArtifactException;

    void unload() throws ArtifactException;

    void unload(ArtifactJobCallback<?> doneCallback) throws ArtifactException;

    void resolve(ArtifactJobCallback<?> doneCallback) throws ArtifactException;

    void load(ArtifactJobCallback<?> doneCallback) throws ArtifactException;

    Artifact getArtifact();
}
