package com.flowpowered.plugins.artifact;


public interface ArtifactJobContext {

    void load();

    void resolve();

    void unload();

    Artifact getArtifact();
}
