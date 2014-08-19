package com.flowpowered.plugins.artifact;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Future;

import com.flowpowered.commons.SimpleFuture;

import com.flowpowered.plugins.artifact.jobs.LoadJob;
import com.flowpowered.plugins.artifact.jobs.LocateJob;
import com.flowpowered.plugins.artifact.jobs.RemoveJob;

public class ArtifactManager {
    private ConcurrentMap<String, Artifact> byName = new ConcurrentHashMap<>();


    /**
     * Makes the Manager try to find the artifact and start tracking it.
     * @return a Future&ltVoid&gt (not really sure about the &ltVoid&gt part)
     */
    public Future<Void> locate(String artifactName) {
        return locate(artifactName, null);
    }

    public <T> Future<T> locateAndSubmitJob(String artifactName, ArtifactJob<T> job) {
        locate(artifactName, job);
        return job.getFuture(); // TODO: are we sure this is the right future to return?
    }

    protected Future<Void> locate(String artifactName, ArtifactJob<?> job) {
        LocateJob ljob = new LocateJob();
        List<ArtifactJob<?>> jobs = new ArrayList<>(2);
        jobs.add(ljob);
        if (job != null) {
            jobs.add(job);
        }

        while (true) {
            Artifact newArtifact = new Artifact();
            newArtifact.getJobQueue().addAll(jobs);

            Artifact artifact = byName.putIfAbsent(artifactName, newArtifact);
            if (artifact == null) {
                enqueuePulse(artifactName);
            } else {
                artifact.getJobQueue().addAll(jobs);

                if (artifact.isGone()) {
                    // Our job might be never processed, so let's try again. Better enqueue the jobs twice than not at all,
                    // as they won't be ran twice because isDone() is checked
                    continue;
                }
            }
            return ljob.getFuture();
        }
    }

    public <T> Future<T> submitJob(String artifactName, ArtifactJob<T> job) {
        Artifact artifact = byName.get(artifactName);
        if (artifact == null) {
            // TODO: Now what?
            return null;
        }
        return submitJob(artifact, job);
    }

    public <T> Future<T> submitJob(Artifact artifact, ArtifactJob<T> job) {
        artifact.getJobQueue().add(job);

        if (artifact.isGone()) {
            job.getFuture().cancel(false); // FIXME: Race condition with pulse()? Should we be doing this at all?
            if (job.getFuture().isCancelled()) {
                return null;
            }
        }
        return job.getFuture();
    }

    public Future<Void> load(Artifact artifact) {
        return submitJob(artifact, new LoadJob());
    }

    protected void doLoad(Artifact artifact, ArtifactJob<?> job) {
        // TODO: logic goes here
    }

    protected void doResolve(Artifact artifact, ArtifactJob<?> job) {
        // TODO: logic goes here
    }

    protected void doUnload(Artifact artifact, ArtifactJob<?> job) {
        // TODO: logic goes here
    }

    /**
     * In any given moment this method can be running at most once per artifact
     */
    public void pulse(String artifactName) {
        Artifact artifact = byName.get(artifactName);
        if (artifact == null) {
            // Should not happen, only we're allowed to set it to null and wouldn't pulse after that
            throw new IllegalStateException("pulsed on nonexistent artifact");
        }
        ArtifactJob<?> job = artifact.getJobQueue().poll();
        if (job != null && !job.getFuture().isDone()) {
            job.run(new JobContext(artifact, job));

            if (job instanceof RemoveJob) {
                byName.remove(artifactName);
                artifact.makeGone();

                for (ArtifactJob<?> j : artifact.getJobQueue()) {
                    if (j instanceof LocateJob && !j.getFuture().isDone()) {
                        SimpleFuture<Void> f = (SimpleFuture<Void>) locate(artifactName);
                        ((LocateJob) j).getFuture().merge(f);
                        // TODO: add the rest of the queue to the new artifact?
                        break;
                    }
                }

                return; // Don't requeue ourselves;
            }
        }
        enqueuePulse(artifactName);
    }

    /**
     * Makes some thread call {@link #pulse(String)} for the given artifact soon.
     * The definition of "soon" is implementation-specific.
     */
    protected void enqueuePulse(String artifactName) {

    }

    protected class JobContext implements ArtifactJobContext {
        private final Artifact artifact;
        private final ArtifactJob<?> job;

        public JobContext(Artifact artifact, ArtifactJob<?> job) {
            this.artifact = artifact;
            this.job = job;
        }

        @Override
        public void load() {
            doLoad(artifact, job);
        }

        @Override
        public void resolve() {
            doResolve(artifact, job);
        }

        @Override
        public void unload() {
            doUnload(artifact, job);
        }

        @Override
        public Artifact getArtifact() {
            return artifact;
        }

    }
}
