package com.flowpowered.plugins.artifact;

import com.flowpowered.plugins.util.ProgressFuture;

public interface ArtifactJob<T> {

    ProgressFuture<T> getFuture();

    void run(ArtifactJobContext ctx);
}
