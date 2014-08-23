package com.flowpowered.plugins.artifact;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Future;

import com.flowpowered.plugins.artifact.ArtifactJob.Result;
import com.flowpowered.plugins.util.ProgressFutureImpl;

public abstract class ArtifactTracker {
    private ConcurrentMap<String, Artifact> byName = new ConcurrentHashMap<>();
    private SchedulingProvider scheduler;

    public ArtifactTracker() {
        super();
    }

    public Artifact getArtifact(String name) {
        return byName.get(name);
    }

    protected SchedulingProvider getSchedulingProvider() {
        return scheduler;
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
            Result result = job.run(createContext(artifact, job));

            switch (result) {
                case STALL:
                    return;  // Don't requeue ourselves
                case DONE:
                    if (job instanceof RemoveJob) {
                        doRemove(artifactName, artifact, new ArtifactGoneException("Artifact has been removed before the job's execution."));
                        return; // Don't requeue ourselves;
                    }
                    break;
                case FAIL:
                    if (job instanceof LocateJob) {
                        doRemove(artifactName, artifact, new ArtifactGoneException("Could not locate artifact", job.getFuture().getThrowable()));
                    }
            }
        }
        enqueuePulse(artifactName);
    }

    protected void doRemove(String artifactName, Artifact artifact, Throwable throwable) {
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
            ((ProgressFutureImpl<?>) queue.remove().getFuture()).setThrowable(throwable);
        } while (j != null);
    }

    protected abstract ArtifactJobContext createContext(Artifact artifact, ArtifactJob<?> job);

    /**
     * Makes some thread call {@link #pulse(String)} for the given artifact soon.
     * The definition of "soon" is implementation-specific.
     */
    protected void enqueuePulse(String artifactName) {
        scheduler.enqueuePulse(artifactName);
    }

    protected interface LocateJob extends ArtifactJob<Void> {
    }

    protected interface RemoveJob extends ArtifactJob<Void> {
    };

}