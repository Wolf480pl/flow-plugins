package com.flowpowered.plugins.artifact;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Future;

import com.flowpowered.plugins.artifact.jobs.LoadJob;
import com.flowpowered.plugins.artifact.jobs.LocateJob;
import com.flowpowered.plugins.artifact.jobs.RemoveJob;
import com.flowpowered.plugins.artifact.jobs.ResolveJob;
import com.flowpowered.plugins.artifact.jobs.UnloadJob;
import com.flowpowered.plugins.util.ProgressFutureImpl;

public class ArtifactManager {
    private ConcurrentMap<String, Artifact> byName = new ConcurrentHashMap<>();

    public Artifact getArtifact(String name) {
        return byName.get(name);
    }

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

    protected void locate(String artifactName, ArtifactJob<?>... jobs) {
        locate(artifactName, Arrays.asList(jobs));
    }

    protected void locate(String artifactName, List<? extends ArtifactJob<?>> jobs) {
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
        }
    }

    public <T> Future<T> submitJob(String artifactName, ArtifactJob<T> job) {
        Artifact artifact = byName.get(artifactName);
        if (artifact == null) {
            return null;
        }
        return submitJob(artifact, job);
    }

    public boolean submitJobs(String artifactName, List<? extends ArtifactJob<?>> jobs) {
        Artifact artifact = byName.get(artifactName);
        if (artifact == null) {
            return false;
        }
        return submitJobs(artifact, jobs);
    }

    public <T> Future<T> submitJob(Artifact artifact, ArtifactJob<T> job) {
        if (job == null) {
            throw new IllegalArgumentException("Job must not be null");
        }
        submitJobs(artifact, Collections.singletonList(job));
        return job.getFuture();
    }

    public boolean submitJobs(Artifact artifact, List<? extends ArtifactJob<?>> jobs) {
        if (jobs == null) {
            throw new IllegalArgumentException("Jobs must not be null");
        }
        if (jobs.isEmpty()) {
            return false;
        }
        if (jobs.contains(null)) {
            throw new IllegalArgumentException("Jobs must not contain null");
        }

        artifact.getJobQueue().addAll(jobs);

        if (artifact.isGone()) {
            if (jobs.get(0).getFuture().cancel(false)) {
                return false;
            }
        }
        return true;
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
        if (job != null && job.getFuture().startProgress()) {
            job.run(new JobContext(artifact, job));

            if (job instanceof RemoveJob) {
                byName.remove(artifactName);
                artifact.makeGone();

                Queue<ArtifactJob<?>> queue = artifact.getJobQueue();
                ArtifactJob<?> j;
                do {
                    j = queue.peek();
                    if (j instanceof LocateJob) {
                        locate(artifactName, new ArrayList<>(queue));
                        break;
                    }
                    // TODO: Make sure the cast below is safe
                    ((ProgressFutureImpl<?>) queue.remove().getFuture()).setThrowable(new ArtifactGoneException("Artifact has been removed before the job's execution."));
                } while (j != null);

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
