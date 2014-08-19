package com.flowpowered.plugins.artifact;

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
     * @return some Future, whose type will be specified once I figure out what I want it to be
     */
    public Future<Object> locate(String artifactName) {
        while (true) {
            LocateJob job = new LocateJob();

            Artifact newArtifact = new Artifact();
            newArtifact.getJobQueue().add(job);

            Artifact artifact = byName.putIfAbsent(artifactName, newArtifact);
            if (artifact == null) {
                enqueuePulse(artifactName);
            } else {
                artifact.getJobQueue().add(job);

                if (artifact.isGone()) {
                    // Our job might be never processed, so let's try again. Better have 2 jobs than none.
                    continue;
                }
            }
            return job.getFuture();
        }
    }

    public Future<Object> load(Artifact artifact) {
        LoadJob job = new LoadJob();
        artifact.getJobQueue().add(job);
        if (artifact.isGone()) {
            // TODO: Now what? Maybe we want all jobs use the same mechanism as for locate?
        }
        return job.getFuture();
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
        ArtifactJob job = artifact.getJobQueue().poll();
        if (job != null) {
            // TODO: Job merging
            job.run(artifact);

            if (job instanceof RemoveJob) {
                byName.remove(artifactName);
                artifact.makeGone();

                for (ArtifactJob j : artifact.getJobQueue()) {
                    if (j instanceof LocateJob) {
                        SimpleFuture<Object> f = (SimpleFuture<Object>) locate(artifactName);
                        j.getFuture().merge(f);
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
}
