package com.flowpowered.plugins.artifact.jvm;

import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.flowpowered.plugins.artifact.Artifact;
import com.flowpowered.plugins.artifact.ArtifactLoader;
import com.flowpowered.plugins.artifact.ArtifactManager;
import com.flowpowered.plugins.artifact.ArtifactState;

public class JVMArtifactManager implements ArtifactManager {
    private Map<String, JVMArtifact> byName = new HashMap<>();
    private Map<URI, JVMArtifact> byURI = new HashMap<>();
    private List<JVMArtifactLoader> loaders = new LinkedList<>();
    private ConcurrentMap<String, Set<JVMArtifact>> watchList = new ConcurrentHashMap<>();

    public JVMArtifactManager() {
        // TODO Auto-generated constructor stub
    }

    @Override
    public JVMArtifact getArtifact(String name) {
        return byName.get(name);
    }

    @Override
    public JVMArtifact getArtifact(URI uri) {
        return byURI.get(uri);
    }

    @Override
    public JVMArtifact loadArtifact(String name) {
        return loadArtifact(name, null);
    }

    protected JVMArtifact loadArtifact(String name, ArtifactLoader<JVMArtifact> preferredLoader) {
        JVMArtifact a = locateArtifact(name, preferredLoader);
        loadArtifact(a);
        return a;
    }

    protected void loadArtifact(JVMArtifact artifact) {
        if (artifact.getState() == ArtifactState.LOADING) {
            return;
        }
        if (artifact.getState() != ArtifactState.LOCATED) {
            // TODO: Put some better exception here;
            throw new IllegalStateException("TODO");
        }
        artifact.setState(ArtifactState.LOADING);
        artifact.getLoader().load(artifact);
        byName.put(artifact.getName(), artifact);
        byURI.put(artifact.getURI(), artifact);
        artifact.setState(ArtifactState.LOADED);
        notifyWatchers(artifact.getName());
    }

    protected JVMArtifact locateArtifact(String name, ArtifactLoader<JVMArtifact> preferredLoader) {
        if (preferredLoader != null) {
            URI uri = preferredLoader.locate(name);
            if (uri != null) {
                return new JVMArtifact(name, uri, preferredLoader);
            }
        }
        for (JVMArtifactLoader loader : loaders) {
            URI uri = loader.locate(name);
            if (uri != null) {
                return new JVMArtifact(name, uri, preferredLoader);
            }
        }
        return null;
    }

    protected boolean enqueueLoad(String name, ArtifactLoader<JVMArtifact> preferredLoader) {
        JVMArtifact artifact = locateArtifact(name, preferredLoader);
        if (artifact == null) {
            return false;
        }
        enqueueLoad(artifact);
        return true;
    }

    protected void enqueueLoad(JVMArtifact artifact) {
        if (artifact.getState() != ArtifactState.LOCATED) {
            return;
        }
        // TODO: Actually enqueue.
        loadArtifact(artifact);
    }

    public void resolveArtifact(Artifact<?> artifact) {
        JVMArtifact ja = checkArtifact(artifact);
        resolveDependencies(ja);
        resolveSoftDependencies(ja);
    }

    @SuppressWarnings("incomplete-switch")
    protected void resolveDependencies(JVMArtifact artifact) {
        ArtifactState newState = ArtifactState.RESOLVED;
        for (String dep : artifact.getDependencies()) {
            switch (resolveDependency(artifact, dep)) {
            case RESOLVED:
                continue;
            case LOADING:
            case WAITING_FOR_DEPS:
                if (newState == ArtifactState.RESOLVED) {
                    newState = ArtifactState.WAITING_FOR_DEPS;
                }
                break;
            case MISSING_DEPS:
                newState = ArtifactState.MISSING_DEPS;
            }
        }
        artifact.setState(newState);
    }

    protected void resolveSoftDependencies(JVMArtifact artifact) {
        if (artifact.getState() != ArtifactState.RESOLVED) {
            return;
        }
        for (String dep : artifact.getSoftDependencies()) {
            resolveDependency(artifact, dep); // We don't care if we succeed or not.
        }
    }

    protected void enqueueResolve(Artifact<?> artifact) {
        // TODO: Actually enqueue.
        resolveArtifact(artifact);
    }

    protected ArtifactState resolveDependency(JVMArtifact artifact, String dependency) {
        JVMArtifact dep = getArtifact(dependency);
        if (dep == null) {
            startWatching(dependency, artifact);
            if (!enqueueLoad(dependency, artifact.getLoader())) {
                return ArtifactState.MISSING_DEPS;
            }
            return ArtifactState.LOADING;
        }
        switch (dep.getState()) {
        case LOCATED:
            enqueueLoad(dep);
            // Falls through
        case LOADING:
            startWatching(dependency, artifact);
            return ArtifactState.LOADING;
        case LOADED:
            enqueueResolve(dep);
            // Falls through
        case WAITING_FOR_DEPS:
            startWatching(dependency, artifact);
            return ArtifactState.WAITING_FOR_DEPS;
        case MISSING_DEPS:
            startWatching(dependency, artifact);
            return ArtifactState.MISSING_DEPS;
        case RESOLVED:
            stopWatching(dependency, artifact);
            artifact.addResolvedDependency(dep);
            dep.addDependent(artifact);
            artifact.getClassLoader().addDependency(dep.getClassLoader().getClassLoader());
            return ArtifactState.RESOLVED;
        case UNLOADING:
        case UNLOADED:
            startWatching(dependency, artifact);
            return ArtifactState.UNLOADED;
        }
        return null;
    }

    @Override
    public boolean unload(Artifact<?> artifact) {
        JVMArtifact jArtifact = checkArtifact(artifact);
        jArtifact.setState(ArtifactState.UNLOADING);

        boolean found = false;
        for (Artifact<JVMArtifact> dependent : jArtifact.getDependents()) {
            if (dependent == null) {
                continue;
            }
            found = true;
            if (dependent.getState() != ArtifactState.UNLOADING) {
                enqueueUnload(dependent);
                startWatching(dependent.getName(), jArtifact);
            }
        }

        // isEmpty will not always work - if we have weak references cleared by GC in the weak key hashmap, it won't count as empty.
        if (found && !jArtifact.getDependents().isEmpty()) {
            return false;
        }

        jArtifact.getLoader().unload(jArtifact);
        byName.remove(artifact.getName());
        byURI.remove(artifact.getURI());

        for (Artifact<JVMArtifact> dep : jArtifact.getResolvedDependencies()) {
            dep.cast().removeDependent(jArtifact);
        }

        jArtifact.setState(ArtifactState.UNLOADED);
        notifyWatchers(artifact.getName());
        return true;
    }

    protected void enqueueUnload(Artifact<?> artifact) {
        // TODO: Actually enqueue
        unload(artifact);
    }

    protected void startWatching(String watched, JVMArtifact watcher) {
        Set<JVMArtifact> set = watchList.get(watched);
        if (set == null) {
            set = Collections.newSetFromMap(new ConcurrentHashMap<JVMArtifact, Boolean>());
            Set<JVMArtifact> prev = watchList.putIfAbsent(watched, set);
            if (prev != null) {
                set = prev;
            }
        }
        set.add(watcher);
    }

    protected void stopWatching(String watched, JVMArtifact watcher) {
        Set<JVMArtifact> set = watchList.get(watched);
        if (set == null) {
            return;
        }
        set.remove(watcher);
    }

    protected void notifyWatchers(String watched) {
        Set<JVMArtifact> set = watchList.get(watched);
        if (set == null) {
            return;
        }
        for (JVMArtifact art : set) {
            notifyArtifact(art, watched);
        }
    }

    protected void notifyArtifact(JVMArtifact watcher, String watched) {
        switch (watcher.getState()) {
        case WAITING_FOR_DEPS:
        case MISSING_DEPS:
            // I know this isn't as efficient as it could be, but less code duplication ;)
            resolveArtifact(watcher);
            break;
        case RESOLVED:
            // Same here
            resolveSoftDependencies(watcher);
            break;
        default:
            // no-op
        }
    }

    protected JVMArtifact checkArtifact(Artifact<?> artifact) {
        if (artifact.getLoader().getArtifactManager() == this && artifact instanceof JVMArtifact && getArtifact(artifact.getName()) == artifact) {
            return (JVMArtifact) artifact;
        }
        throw new IllegalStateException("Not our artifact!");
    }
}
