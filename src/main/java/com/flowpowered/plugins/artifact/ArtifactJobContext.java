package com.flowpowered.plugins.artifact;


public interface ArtifactJobContext extends ArtifactContext {

    void load() throws ArtifactException;

    void resolve() throws ArtifactException;

    void unload() throws ArtifactException;

    void unload(ArtifactCallback<?, ? super ArtifactJobContext, Void> doneCallback) throws ArtifactException;

    void resolve(ArtifactCallback<?, ? super ArtifactJobContext, Void> doneCallback) throws ArtifactException;

    void load(ArtifactCallback<?, ? super ArtifactJobContext, Void> doneCallback) throws ArtifactException;
}
