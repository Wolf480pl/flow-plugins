package com.flowpowered.plugins.artifact;

import java.net.URI;
import java.util.Set;

public interface Artifact<T extends Artifact<T>> {

    String getName();

    URI getURI();

    Set<String> getDependencies();

    Set<String> getSoftDependencies();

    Set<Artifact<T>> getResolvedDependencies();

    Set<Artifact<T>> getDependents();

    ArtifactState getState();

    ArtifactLoader<T> getLoader();

    T cast();
}
