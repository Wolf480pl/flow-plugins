package com.flowpowered.plugins.artifact;

import java.net.URI;


public interface ArtifactLoader<T extends Artifact<T>> {

    ArtifactManager getArtifactManager();

    boolean has(String name);

    URI locate(String name);

    T load(String name);

    T load(URI uri);

    void load(T artifact);

    void unload(T artifact);

}
