package com.flowpowered.plugins.artifact.jobs;

import com.flowpowered.plugins.artifact.ArtifactJobCallback;
import com.flowpowered.plugins.artifact.ArtifactJobContext;


public class CallbackJob<T> extends AbstractJob<T> {
    private final ArtifactJobCallback<T> callback;

    public CallbackJob(ArtifactJobCallback<T> callback) {
        this.callback = callback;
    }

    @Override
    protected T call(ArtifactJobContext ctx) throws Exception {
        return callback.call(ctx);
    }
}
