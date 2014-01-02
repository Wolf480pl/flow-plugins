package com.flowpowered.plugins.artifact;

import java.net.URI;

public interface ArtifactManager {

    Artifact<?> getArtifact(String name);

    Artifact<?> getArtifact(URI uri);

    Artifact<?> loadArtifact(String name);

    boolean unload(Artifact<?> artifact);

}
