package com.flowpowered.plugins.artifact;


public interface ArtifactContext {

    Artifact getArtifact();

    <T> void doAsync(ArtifactCallback<T, ArtifactContext, ?> a, ArtifactCallback<?, ArtifactContext, T> b);
}
