package com.flowpowered.plugins.artifact;

import java.net.URI;

public abstract class AbstractArtifactLoader<T extends Artifact<T>> implements ArtifactLoader<T> {

    @Override
    public boolean has(String name) {
        return locate(name) != null;
    }

    @Override
    public T load(String name) {
        URI uri = locate(name);
        if (uri == null) {
            // TODO: Throw something maybe?
            return null;
        }
        return load(uri);
    }
}
