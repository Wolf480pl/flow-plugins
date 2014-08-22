package com.flowpowered.plugins.artifact.jobs;

import com.flowpowered.plugins.artifact.ArtifactJobContext;
import com.flowpowered.plugins.artifact.ArtifactState;
import com.flowpowered.plugins.artifact.WrongStateException;


public class RemoveJob extends AbstractJob<Void> {

    @Override
    public Void call(ArtifactJobContext ctx) throws WrongStateException {
        ArtifactState state = ctx.getArtifact().getState();
        switch (state) {
            case LOADING:
            case LOADED:
            case WAITING_FOR_DEPS:
            case MISSING_DEPS:
            case RESOLVED:
                throw new WrongStateException("Cannot remove artifact", state);
            default:
                return null; // ArtifactManager.pulse() will take care of the rest
        }
    }

}
