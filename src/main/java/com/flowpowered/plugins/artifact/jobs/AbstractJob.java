package com.flowpowered.plugins.artifact.jobs;

import com.flowpowered.commons.SimpleFuture;

import com.flowpowered.plugins.artifact.ArtifactJob;
import com.flowpowered.plugins.artifact.ArtifactJobContext;

public abstract class AbstractJob<T> implements ArtifactJob<T> {
    private SimpleFuture<T> future = new SimpleFuture<>();

    @Override
    public SimpleFuture<T> getFuture() {
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