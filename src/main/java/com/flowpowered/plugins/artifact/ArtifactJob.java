package com.flowpowered.plugins.artifact;

import com.flowpowered.commons.SimpleFuture;

public interface ArtifactJob {

    SimpleFuture<Object> getFuture();

    void run(Artifact artifact);
}
