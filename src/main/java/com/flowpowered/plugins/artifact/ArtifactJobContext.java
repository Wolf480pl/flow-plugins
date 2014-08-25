package com.flowpowered.plugins.artifact;

import com.flowpowered.plugins.util.callback.ThrowingCatchingFunction;

public interface ArtifactJobContext extends ArtifactContext {

    void load() throws ArtifactException;

    void resolve() throws ArtifactException;

    void unload() throws ArtifactException;

    void unload(ThrowingCatchingFunction<Void, ArtifactException, ?, ? extends Exception> doneCallback) throws ArtifactException;

    void resolve(ThrowingCatchingFunction<Void, ArtifactException, ?, ? extends Exception> doneCallback) throws ArtifactException;

    void load(ThrowingCatchingFunction<Void, ArtifactException, ?, ? extends Exception> doneCallback) throws ArtifactException;
}
