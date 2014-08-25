package com.flowpowered.plugins.artifact;

import com.flowpowered.plugins.util.callback.Function;

public interface ArtifactContext {

    Artifact getArtifact();

    <T> void doAsync(Function<Void, ?> function);
}
