package com.flowpowered.plugins.artifact.jobs;

import com.flowpowered.plugins.artifact.ArtifactException;
import com.flowpowered.plugins.artifact.ArtifactJobCallback;
import com.flowpowered.plugins.artifact.ArtifactJobContext;
import com.flowpowered.plugins.artifact.ArtifactState;
import com.flowpowered.plugins.artifact.WrongStateException;

public class ResolveJob extends AbstractJob<Void> {

    @Override
    public Void call(ArtifactJobContext ctx) throws ArtifactException {
        ArtifactState state = ctx.getArtifact().getState();

        switch (state) {
            case UNDEFINED: // Only experienced before an Artifact is located
            case LOADING: // Should stall the queue
                throw new IllegalStateException("Artifact state was " + state + " when ResolveJob was called");
            case UNLOADING:
                // TODO: Does the queue stall when unloading? If so, throw IllegalStateException like above
                throw new WrongStateException("Cannot resolve artifact", state);
            case RESOLVED:
                // Nothing to do
                return null;
            case LOCATED:
            case UNLOADED:
                ctx.load(step2);
                return null;
            case LOADED:
            case MISSING_DEPS:
            case WAITING_FOR_DEPS:
        }
        ctx.resolve();
        return null;
    }

    protected ArtifactJobCallback<Void> step2 = new ArtifactJobCallback<Void>() {
        @Override
        public Void call(ArtifactJobContext ctx) throws ArtifactException {
            return ResolveJob.this.call(ctx);
        }

    };
}
