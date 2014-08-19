package com.flowpowered.plugins.artifact.jobs;

import com.flowpowered.plugins.artifact.ArtifactJobContext;

public class ResolveJob extends AbstractJob<Void> {

    @Override
    public Void call(ArtifactJobContext ctx) {
        ctx.resolve();
        return null;
    }
}
