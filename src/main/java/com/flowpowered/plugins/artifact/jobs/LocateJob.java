package com.flowpowered.plugins.artifact.jobs;

import com.flowpowered.plugins.artifact.Artifact;
import com.flowpowered.plugins.artifact.ArtifactState;

public class LocateJob extends AbstractJob {
    @Override
    public void run(Artifact artifact) {
        if (artifact.getState() != ArtifactState.UNDEFINED) {
            // Nothing to do
            future.setResult(null);
            return;
        }
        // TODO Auto-generated method stub
        artifact.setStateAndCurrentJob(ArtifactState.LOCATED, null);
        future.setResult(null);
    }

}
