package com.flowpowered.plugins.artifact.jobs;

import com.flowpowered.plugins.artifact.Artifact;
import com.flowpowered.plugins.artifact.ArtifactJobContext;
import com.flowpowered.plugins.artifact.ArtifactState;

public class LocateJob extends AbstractJob<Void> {
    @Override
    public Void call(ArtifactJobContext ctx) {
        Artifact artifact = ctx.getArtifact();
        if (artifact.getState() != ArtifactState.UNDEFINED) {
            return null;
        }
        // TODO Auto-generated method stub
        artifact.setStateAndCurrentJob(ArtifactState.LOCATED, null);
        return null;
    }

}
