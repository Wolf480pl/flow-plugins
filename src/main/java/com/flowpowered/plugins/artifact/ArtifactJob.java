package com.flowpowered.plugins.artifact;

import com.flowpowered.plugins.util.SafeProgressFuture;

public interface ArtifactJob<T> {

    SafeProgressFuture<T> getFuture();

    Result run(ArtifactJobContext ctx);

    public static enum Result {
        DONE, STALL, FAIL
    }
}
