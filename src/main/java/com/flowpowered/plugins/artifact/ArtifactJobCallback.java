package com.flowpowered.plugins.artifact;

public interface ArtifactJobCallback<T> {
    T call(ArtifactJobContext ctx) throws Exception;
}
