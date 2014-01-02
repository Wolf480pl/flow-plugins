package com.flowpowered.plugins.artifact.jvm;

import java.net.URI;
import java.util.Set;

import com.flowpowered.plugins.artifact.AbstractArtifact;
import com.flowpowered.plugins.artifact.Artifact;
import com.flowpowered.plugins.artifact.ArtifactLoader;
import com.flowpowered.plugins.artifact.ArtifactState;

public class JVMArtifact extends AbstractArtifact<JVMArtifact> {
    private ArtifactClassLoader cl;

    public JVMArtifact(String name, URI uri, ArtifactLoader<JVMArtifact> loader) {
        super(name, uri, loader);
        setState(ArtifactState.LOCATED);
    }

    public void setClassLoader(ArtifactClassLoader classLoader) {
        cl = classLoader;
    }

    public ArtifactClassLoader getClassLoader() {
        return cl;
    }

    @Override
    public void setDependencies(Set<String> dependencies) {
        super.setDependencies(dependencies);
    }

    @Override
    public void setSoftDependencies(Set<String> softDependencies) {
        super.setSoftDependencies(softDependencies);
    }

    @Override
    public boolean addResolvedDependency(Artifact<JVMArtifact> artifact) {
        return super.addResolvedDependency(artifact);
    }

    @Override
    public boolean addDependent(Artifact<JVMArtifact> artifact) {
        return super.addDependent(artifact);
    }

    @Override
    public boolean removeDependent(Artifact<JVMArtifact> artifact) {
        return super.removeDependent(artifact);
    }

    @Override
    public void setState(ArtifactState state) {
        super.setState(state);
    }

    @Override
    public JVMArtifact cast() {
        return this;
    }

}
