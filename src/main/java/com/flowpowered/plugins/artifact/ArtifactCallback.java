package com.flowpowered.plugins.artifact;

import com.flowpowered.plugins.util.ResultOrThrowable;

public interface ArtifactCallback<T, C, F> {
    T call(C ctx, ResultOrThrowable<F, ArtifactException> previous) throws Exception;
}
