package com.flowpowered.plugins.artifact;

import java.util.concurrent.Future;

public interface ArtifactJob<T> {

    Future<T> getFuture();

    void run(ArtifactJobContext ctx);
}
