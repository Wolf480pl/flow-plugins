package com.flowpowered.plugins.artifact.jobs;

import com.flowpowered.plugins.artifact.Artifact;
import com.flowpowered.plugins.artifact.ArtifactState;

public class LoadJob extends AbstractJob {

    @Override
    public void run(Artifact artifact) {
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
    }

}
