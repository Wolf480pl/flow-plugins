package com.flowpowered.plugins.artifact;

import java.net.URI;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.MapMaker;

public abstract class AbstractArtifact<T extends Artifact<T>> implements Artifact<T> {
    private final String name;
    private final URI uri;
    private final ArtifactLoader<T> loader;
    private ArtifactState state;
    private Set<String> dependencies;
    private Set<String> softDependencies;
    private Set<Artifact<T>> resolvedDependencies = new HashSet<>();
    private Set<Artifact<T>> dependents;

    public AbstractArtifact(String name, URI uri, ArtifactLoader<T> loader) {
        this.name = name;
        this.loader = loader;
        this.uri = uri;

        Map<Artifact<T>, Boolean> map = new MapMaker().weakKeys().makeMap();
        dependents = Collections.newSetFromMap(map);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public URI getURI() {
        return uri;
    }

    @Override
    public Set<String> getDependencies() {
        return Collections.unmodifiableSet(dependencies);
    }

    protected void setDependencies(Set<String> dependencies) {
        this.dependencies = dependencies;
    }

    @Override
    public Set<String> getSoftDependencies() {
        return Collections.unmodifiableSet(softDependencies);
    }

    protected void setSoftDependencies(Set<String> softDependencies) {
        this.softDependencies = softDependencies;
    }

    @Override
    public Set<Artifact<T>> getResolvedDependencies() {
        return Collections.unmodifiableSet(resolvedDependencies);
    }

    protected boolean addResolvedDependency(Artifact<T> artifact) {
        return resolvedDependencies.add(artifact);
    }

    @Override
    public Set<Artifact<T>> getDependents() {
        return Collections.unmodifiableSet(dependents);
    }

    protected boolean addDependent(Artifact<T> artifact) {
        return dependents.add(artifact);
    }

    protected boolean removeDependent(Artifact<T> artifact) {
        return dependents.remove(artifact);
    }

    @Override
    public ArtifactState getState() {
        return state;
    }

    protected void setState(ArtifactState state) {
        this.state = state;
    }

    @Override
    public ArtifactLoader<T> getLoader() {
        return loader;
    }

}
