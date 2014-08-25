package com.flowpowered.plugins.artifact.jobs;

import com.flowpowered.plugins.artifact.ArtifactException;
import com.flowpowered.plugins.artifact.ArtifactJobContext;
import com.flowpowered.plugins.artifact.ArtifactState;
import com.flowpowered.plugins.artifact.WrongStateException;
import com.flowpowered.plugins.util.ResultOrThrowable;
import com.flowpowered.plugins.util.callback.ThrowingCatchingFunction;

public class ResolveJob extends AbstractJob<Void> {

    @Override
    public Void call(final ArtifactJobContext ctx) throws ArtifactException {
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
                ctx.load(new ThrowingCatchingFunction<Void, ArtifactException, Void, ArtifactException>() {
                    @Override
                    public Void call(ResultOrThrowable<Void, ArtifactException> input) throws ArtifactException {
                        return ResolveJob.this.call(ctx);
                    }
                });
                return null;
            case LOADED:
            case MISSING_DEPS:
            case WAITING_FOR_DEPS:
        }
        ctx.resolve();
        return null;
    }
}
