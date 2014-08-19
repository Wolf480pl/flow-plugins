package com.flowpowered.plugins.artifact.jobs;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import com.flowpowered.plugins.artifact.ArtifactJob;
import com.flowpowered.plugins.artifact.ArtifactJobContext;

@Deprecated
// can go
public class CompoundJob<T> extends AbstractJob<T> {
    private final AbstractJob<?> first;
    private final ArtifactJob<T> last;

    public CompoundJob(AbstractJob<?> first, ArtifactJob<T> last) {
        this.first = first;
        this.last = last;
    }

    @Override
    protected T call(ArtifactJobContext ctx) throws Exception {

        first.call(ctx);
        last.run(ctx);

        Future<T> f = last.getFuture();
        if (!f.isDone()) {
            throw new IllegalStateException("Job " + last.getClass().getSimpleName() + " didn't get done after calling run(ctx)");
        }

        try {
            return last.getFuture().get(1, TimeUnit.MILLISECONDS);
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof Exception) {
                throw (Exception) cause;
            } else {
                throw e;
            }
        }
    }

}
