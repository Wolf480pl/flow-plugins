package com.flowpowered.plugins.artifact;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

public class Artifact {
    private Queue<ArtifactJob> jobQueue = new ConcurrentLinkedQueue<>();
    private Pair<ArtifactState, ArtifactJob> stateAndJob = new ImmutablePair<>(ArtifactState.UNDEFINED, null);
    private volatile boolean gone = false;

    public ArtifactState getState() {
        return stateAndJob.getLeft();
    }

    public ArtifactJob getCurrentJob() {
        return stateAndJob.getRight();
    }

    public Pair<ArtifactState, ArtifactJob> getStateAndCurrentJob() {
        return stateAndJob;
    }

    /**
     * Should be only called from the thread that is currently executing {@link ArtifactManager#pulse(String)}
     */
    public void setStateAndCurrentJob(ArtifactState state, ArtifactJob job) {
        this.stateAndJob = new ImmutablePair(state, job);
    }

    public Queue<ArtifactJob> getJobQueue() {
        return jobQueue;
    }

    public boolean isGone() {
        return gone;
    }

    protected void makeGone() {
        this.gone = true;
    }
}
