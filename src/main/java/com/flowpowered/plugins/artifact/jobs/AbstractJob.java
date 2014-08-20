package com.flowpowered.plugins.artifact.jobs;

import com.flowpowered.plugins.artifact.ArtifactJob;
import com.flowpowered.plugins.artifact.ArtifactJobContext;
import com.flowpowered.plugins.util.ProgressFuture;
import com.flowpowered.plugins.util.ProgressFutureImpl;

public abstract class AbstractJob<T> implements ArtifactJob<T> {
    private ProgressFutureImpl<T> future = new ProgressFutureImpl<>();

    @Override
    public ProgressFuture<T> getFuture() {
        return future;
    }

    protected abstract T call(ArtifactJobContext ctx) throws Exception;

    @Override
    public void run(ArtifactJobContext ctx) {
        try {
            future.setResult(call(ctx));
        } catch (Exception e) {
            future.setThrowable(e);
        }
    }

}