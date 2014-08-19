package com.flowpowered.plugins.artifact.jobs;

import com.flowpowered.plugins.artifact.ArtifactJobContext;

public class UnloadJob extends AbstractJob<Void> {

    @Override
    public Void call(ArtifactJobContext ctx) {
        ctx.unload();
        return null;
    }
}
