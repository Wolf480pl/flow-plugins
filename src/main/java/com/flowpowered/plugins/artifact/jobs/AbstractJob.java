package com.flowpowered.plugins.artifact.jobs;

import com.flowpowered.plugins.artifact.ArtifactJob;
import com.flowpowered.plugins.artifact.ArtifactJobContext;
import com.flowpowered.plugins.artifact.WillFinishLaterError;
import com.flowpowered.plugins.util.ProgressFutureImpl;
import com.flowpowered.plugins.util.SafeProgressFuture;

public abstract class AbstractJob<T> implements ArtifactJob<T> {
    private ProgressFutureImpl<T> future = new ProgressFutureImpl<>();

    @Override
    public SafeProgressFuture<T> getFuture() {
        return future;
    }

    protected abstract T call(ArtifactJobContext ctx) throws Exception;

    @Override
    public Result run(ArtifactJobContext ctx) {
        try {
            future.setResult(call(ctx));
        } catch (Exception e) {
            future.setThrowable(e);
            return Result.FAIL;
        } catch (WillFinishLaterError e) {
            return Result.STALL;
        }
        return Result.DONE;
    }

}