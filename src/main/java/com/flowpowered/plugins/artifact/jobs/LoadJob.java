package com.flowpowered.plugins.artifact.jobs;

import com.flowpowered.plugins.artifact.ArtifactException;
import com.flowpowered.plugins.artifact.ArtifactJobContext;
import com.flowpowered.plugins.artifact.ArtifactState;


public class LoadJob extends AbstractJob<Void> {

    @Override
    public Void call(ArtifactJobContext ctx) throws ArtifactException {
        ArtifactState state = ctx.getArtifact().getState();

        switch (state) {
            case UNDEFINED:
                throw new IllegalStateException("Artifact state was UNDEFINED when LoadJob was called");
            case LOADING:
            case LOADED:
            case WAITING_FOR_DEPS:
            case MISSING_DEPS:
            case RESOLVED:
                // Nothing to do
                return null;
            case UNLOADING:
                // TODO: Does the queue stall when unloading? If so, throw IllegalStateException. If not, what do we do now?
                break;
            default:
        }
        ctx.load();
        return null;
    }

}
