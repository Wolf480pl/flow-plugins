package com.flowpowered.plugins.artifact.jobs;

import com.flowpowered.plugins.artifact.ArtifactException;
import com.flowpowered.plugins.artifact.ArtifactJobContext;
import com.flowpowered.plugins.artifact.ArtifactState;

public class UnloadJob extends AbstractJob<Void> {

    @Override
    public Void call(ArtifactJobContext ctx) throws ArtifactException {
        ArtifactState state = ctx.getArtifact().getState();

        switch (state) {
            case UNDEFINED: // Only experienced before an Artifact is located
            case LOADING: // Should stall the queue
                throw new IllegalStateException("Artifact state was " + state + " when ResolveJob was called");
            case LOCATED:
            case UNLOADED:
                // Nothing to do
                return null;
            case UNLOADING:
                // TODO: Does the queue stall when unloading? If so, throw IllegalStateException. If not, what do we do now?
            case LOADED:
            case MISSING_DEPS:
            case WAITING_FOR_DEPS:
            case RESOLVED:
        }
        ctx.unload();
        return null;
    }
}
