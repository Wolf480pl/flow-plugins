package com.flowpowered.plugins.artifact.jobs;

import com.flowpowered.plugins.artifact.ArtifactJobContext;
import com.flowpowered.plugins.util.callback.ThrowingFunction;


public class CallbackJob<T> extends AbstractJob<T> {
    private final ThrowingFunction<ArtifactJobContext, T, ? extends Exception> callback;

    public CallbackJob(ThrowingFunction<ArtifactJobContext, T, ? extends Exception> callback) {
        this.callback = callback;
    }

    @Override
    protected T call(ArtifactJobContext ctx) throws Exception {
        return callback.call(ctx);
    }
}
