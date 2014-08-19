package com.flowpowered.plugins.artifact.jobs;

import com.flowpowered.plugins.artifact.ArtifactJobContext;


public class LoadJob extends AbstractJob<Void> {

    @Override
    public Void call(ArtifactJobContext ctx) {
        ctx.load();
        return null;
        /*
        switch (artifact.getState()) {
            case LOADING:
            case LOADED:
            case WAITING_FOR_DEPS:
            case MISSING_DEPS:
            case RESOLVED:
                // Nothing to do
                future.setResult(null);
                return;
            case UNLOADING:
                // TODO: What now? Should we somehow wait for it to get unloaded
            default:
        }
        artifact.setStateAndCurrentJob(ArtifactState.LOADING, this);
        // TODO Auto-generated method stub
        artifact.setStateAndCurrentJob(ArtifactState.LOADED, null);
        future.setResult(null);
         */
    }

}
