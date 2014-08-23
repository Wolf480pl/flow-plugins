package com.flowpowered.plugins.artifact;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

import org.slf4j.Logger;

import com.flowpowered.plugins.artifact.jobs.AbstractJob;
import com.flowpowered.plugins.artifact.jobs.LoadJob;
import com.flowpowered.plugins.artifact.jobs.ResolveJob;
import com.flowpowered.plugins.artifact.jobs.UnloadJob;
import com.flowpowered.plugins.util.ResultOrThrowable;

public class ArtifactManager extends ArtifactTracker {
    private Logger logger;

    /**
     * Makes the Manager try to find the artifact and start tracking it.
     * @return a Future&ltVoid&gt (not really sure about the &ltVoid&gt part)
     */
    public Future<Void> locate(String artifactName) {
        LocateJob ljob = new LocateJob();
        locate(artifactName, ljob);
        return ljob.getFuture();
    }

    public Future<Void> locateAndSubmitJobs(String artifactName, List<? extends ArtifactJob<?>> jobs) {
        if (jobs == null) {
            throw new IllegalArgumentException("Jobs must not be null!");
        }
        LocateJob ljob = new LocateJob();
        List<ArtifactJob<?>> allJobs = new ArrayList<>(jobs.size() + 1);
        allJobs.add(ljob);
        allJobs.addAll(jobs);
        locate(artifactName, allJobs);
        return ljob.getFuture();
    }

    public <T> Future<T> locateAndSubmitJob(String artifactName, ArtifactJob<T> job) {
        if (job == null) {
            throw new IllegalArgumentException("Job must not be null!");
        }
        LocateJob ljob = new LocateJob();
        locate(artifactName, ljob, job);
        return job.getFuture(); // TODO: are we sure this is the right future to return?
    }

    public Future<Void> load(Artifact artifact) {
        return submitJob(artifact, new LoadJob());
    }

    public Future<Void> resolve(Artifact artifact) {
        return submitJob(artifact, new ResolveJob());
    }

    public Future<Void> unload(Artifact artifact) {
        return submitJob(artifact, new UnloadJob());
    }

    protected void doLocate(ArtifactJobContext ctx) {
        Artifact artifact = ctx.getArtifact();
        // TODO: logic goes here
        artifact.setStateAndCurrentJob(ArtifactState.LOCATED, null);
    }

    protected void doLoad(JobContext ctx, ArtifactCallback<?, ? super ArtifactJobContext, Void> doneCallback) throws ArtifactException {
        Artifact artifact = ctx.getArtifact();
        ArtifactState state = artifact.getState();
        if (!(state == ArtifactState.LOCATED || state == ArtifactState.UNLOADED)) {
            throw new WrongStateException("Could not load artifact", state);
        }

        artifact.setStateAndCurrentJob(ArtifactState.LOADING, ctx.job);
        // TODO: logic goes here
        artifact.setStateAndCurrentJob(ArtifactState.LOADED, null);

        if (doneCallback != null) {
            try {
                ResultOrThrowable<Void, ArtifactException> result = ResultOrThrowable.result(null);
                doneCallback.call(ctx, result);
            } catch (Exception e) {
                throw new ArtifactException("Exception in callback", e);
            }
        }
    }

    protected void doResolve(JobContext ctx, ArtifactCallback<?, ? super ArtifactJobContext, Void> doneCallback) throws ArtifactException {
        Artifact artifact = ctx.getArtifact();
        ArtifactState state = artifact.getState();
        if (!(state == ArtifactState.LOADED || state == ArtifactState.WAITING_FOR_DEPS || state == ArtifactState.MISSING_DEPS)) {
            throw new WrongStateException("Could not resolve artifact", state);
        }

        // TODO: logic goes here
        artifact.setStateAndCurrentJob(ArtifactState.RESOLVED, null);
        if (doneCallback != null) {
            try {
                ResultOrThrowable<Void, ArtifactException> result = ResultOrThrowable.result(null);
                doneCallback.call(ctx, result);
            } catch (Exception e) {
                throw new ArtifactException("Exception in callback", e);
            }
        }
    }

    protected void doUnload(JobContext ctx, ArtifactCallback<?, ? super ArtifactJobContext, Void> doneCallback) throws ArtifactException {
        Artifact artifact = ctx.getArtifact();
        ArtifactState state = artifact.getState();
        if (!(state == ArtifactState.LOADED || state == ArtifactState.WAITING_FOR_DEPS || state == ArtifactState.MISSING_DEPS || state == ArtifactState.RESOLVED)) {
            throw new WrongStateException("Could not unload artifact", state);
        }

        artifact.setStateAndCurrentJob(ArtifactState.UNLOADING, ctx.job);
        // TODO: logic goes here
        artifact.setStateAndCurrentJob(ArtifactState.UNLOADED, null);
        if (doneCallback != null) {
            try {
                ResultOrThrowable<Void, ArtifactException> result = ResultOrThrowable.result(null);
                doneCallback.call(ctx, result);
            } catch (Exception e) {
                throw new ArtifactException("Exception in callback", e);
            }
        }
    }

    @Override
    protected ArtifactJobContext createContext(Artifact artifact, ArtifactJob<?> job) {
        return new JobContext(artifact, job);
    }

    protected class JobContext implements ArtifactJobContext {
        private final Artifact artifact;
        private final ArtifactJob<?> job;

        public JobContext(Artifact artifact, ArtifactJob<?> job) {
            this.artifact = artifact;
            this.job = job;
        }

        @Override
        public void load() throws ArtifactException {
            load(null);
        }

        @Override
        public void load(ArtifactCallback<?, ? super ArtifactJobContext, Void> doneCallback) throws ArtifactException {
            doLoad(this, doneCallback);
        }

        @Override
        public void resolve() throws ArtifactException {
            resolve(null);
        }

        @Override
        public void resolve(ArtifactCallback<?, ? super ArtifactJobContext, Void> doneCallback) throws ArtifactException {
            doResolve(this, doneCallback);
        }

        @Override
        public void unload() throws ArtifactException {
            unload(null);
        }

        @Override
        public void unload(ArtifactCallback<?, ? super ArtifactJobContext, Void> doneCallback) throws ArtifactException {
            doUnload(this, doneCallback);
        }

        @Override
        public Artifact getArtifact() {
            return artifact;
        }

        @Override
        public <T> void doAsync(final ArtifactCallback<T, ArtifactContext, ?> a, final ArtifactCallback<?, ArtifactContext, T> b) {
            getSchedulingProvider().runAsync(new Runnable() {
                @Override
                public void run() {
                    /*
                    ResultOrThrowable<T, Exception> rot;
                    try {
                        T result = a.call(JobContext.this, null);
                        rot = ResultOrThrowable.result(result);
                    } catch (Exception e) {
                        rot = ResultOrThrowable.throwable(e);
                    }
                    b.call(JobContext.this, rot);
                     */
                }
            });

        }

    }

    protected class LocateJob extends AbstractJob<Void> implements ArtifactTracker.LocateJob {
        @Override
        public Void call(ArtifactJobContext ctx) {
            Artifact artifact = ctx.getArtifact();
            if (artifact.getState() != ArtifactState.UNDEFINED) {
                return null;
            }
            doLocate(ctx);
            return null;
        }
    }

    public class RemoveJob extends AbstractJob<Void> implements ArtifactTracker.RemoveJob {
        @Override
        public Void call(ArtifactJobContext ctx) throws WrongStateException {
            ArtifactState state = ctx.getArtifact().getState();
            switch (state) {
                case LOADING:
                case LOADED:
                case WAITING_FOR_DEPS:
                case MISSING_DEPS:
                case RESOLVED:
                    throw new WrongStateException("Cannot remove artifact", state);
                default:
                    return null; // ArtifactManager.pulse() will take care of the rest
            }
        }
    }
}
