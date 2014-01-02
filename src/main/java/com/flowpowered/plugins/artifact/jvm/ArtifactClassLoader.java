package com.flowpowered.plugins.artifact.jvm;

import java.util.Collection;

public interface ArtifactClassLoader {

    ClassLoader getClassLoader();

    boolean addDependency(ClassLoader loader);

    boolean addDependencies(Collection<ClassLoader> loaders);

}
