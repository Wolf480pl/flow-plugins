package com.flowpowered.plugins.artifact.jobs;

import com.flowpowered.commons.SimpleFuture;

import com.flowpowered.plugins.artifact.ArtifactJob;

public abstract class AbstractJob implements ArtifactJob {
    protected SimpleFuture<Object> future = new SimpleFuture<>();

    @Override
    public SimpleFuture<Object> getFuture() {
        return future;
    }

}