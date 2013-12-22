/*
 * This file is part of Flow Plugins, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2013 Spout LLC <http://www.spout.org/>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.flowpowered.plugins.impl;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

import org.apache.logging.log4j.Logger;

import com.flowpowered.plugins.*;

public class SimplePluginManager implements PluginManager {
    private Map<String, SimplePluginHandle> handles = new HashMap<>();
    private List<PluginLoader> loaders = new LinkedList<>();
    private final Logger logger;

    public SimplePluginManager(Logger logger) {
        this.logger = logger;
    }

    @Override
    public PluginHandle getHandle(String name) {
        return this.handles.get(name);
    }

    @Override
    public Map<String, ? extends PluginHandle> getHandles() {
        return Collections.unmodifiableMap(this.handles);
    }

    @Override
    public PluginHandle loadPlugin(File file) throws PluginException {
        PluginLoader loader = null;
        for (PluginLoader candidate : this.loaders) {
            if (candidate.canHandle(file)) {
                loader = candidate;
                break;
            }
        }
        if (loader == null) {
            throw new InvalidPluginException("None of PluginLoaders can load the plugin", file);
        }
        PluginDescriptionFile pdf = loader.getDescription(file);
        SimplePluginHandle handle = new SimplePluginHandle(this, pdf.getName());
        handle.setFile(file);
        handle.setDescription(pdf);

        // TODO process dependencies

        loader.loadPlugin(handle);
        this.handles.put(pdf.getName(), handle);
        handle.setState(PluginState.DISABLED);
        return handle;
    }

    @Override
    public Future<Void> enablePlugin(PluginHandle handle) throws PluginException {
        SimplePluginHandle sHandle = checkHandle(handle);
        PluginState state = sHandle.getState();
        if (state != PluginState.DISABLED) {
            throw new IllegalActionException("Cannot enable plugin when its state is " + state);
        }

        // TODO: Check thread and enqueue if can't do it now. Also, we should return some kind of future in this case.

        sHandle.setState(PluginState.ENABLING);

        for (SimplePluginHandle dep : sHandle.getDepencencies()) {
            try {
                if (dep.getState() != PluginState.ENABLED) {
                    // TODO: If it's ENABLING, wait for it
                    // TODO: What if it's UNLOADING or UNLOADED?
                    enablePlugin(dep);
                }
            } catch (PluginException e) {
                sHandle.setState(PluginState.DISABLED);
                throw new UnsatisfiedDependencyException("Could not enable dependency: " + handle.getName(), e);
            }
        }

        // TODO: Process soft dependencies

        Plugin plugin = sHandle.getPlugin();
        try {
            plugin.onEnable();
        } catch (Throwable t) {
            // TODO: Set the state
            throw new PluginException("Exception in onEnable of plugin " + sHandle.getName(), t);
        }
        sHandle.setState(PluginState.ENABLED);
        return null;
    }

    @Override
    public Future<Void> disablePlugin(PluginHandle handle) throws PluginException {
        SimplePluginHandle sHandle = checkHandle(handle);
        PluginState state = sHandle.getState();
        if (state != PluginState.ENABLED) {
            throw new IllegalActionException("Cannot disable plugin when its state is " + state);
        }

        // TODO: Check thread and enqueue if can't do it now. Also, we should return some kind of future in this case.

        sHandle.setState(PluginState.DISABLING);

        sHandle.setState(PluginState.ENABLING);

        for (WeakReference<SimplePluginHandle> dep : sHandle.getDependents()) {
            try {
                SimplePluginHandle depHandle = dep.get();
                if (depHandle == null) {
                    continue;
                }
                if (depHandle.getState() == PluginState.ENABLED || depHandle.getState() == PluginState.ENABLING) {
                    disablePlugin(depHandle);
                }
                // TODO: If it's DISABLING, wait for it
            } catch (PluginException e) {
                sHandle.setState(PluginState.DISABLED);
                throw new UnsatisfiedDependencyException("Could not enable dependency: " + handle.getName(), e);
            }
        }

        // TODO: Process soft dependencies

        Plugin plugin = sHandle.getPlugin();
        try {
            plugin.onDisable();
        } catch (Throwable t) {
            throw new PluginException("Exception in onDisable of plugin " + sHandle.getName(), t);
        }
        sHandle.setState(PluginState.DISABLED);
        return null;
    }

    @Override
    public void unloadPlugin(PluginHandle handle) throws PluginException {
        SimplePluginHandle sHandle = checkHandle(handle);
        PluginState state = sHandle.getState();
        if (state != PluginState.DISABLED) {
            throw new IllegalActionException("Cannot unload plugin when its state is " + state);
        }

        for (WeakReference<SimplePluginHandle> dep : sHandle.getDependents()) {
            SimplePluginHandle depHandle = dep.get();
            if (depHandle == null) {
                continue;
            }
            if (depHandle.getState() != PluginState.UNLOADING && depHandle.getState() != PluginState.UNLOADED) {
                unloadPlugin(depHandle);
            }
        }

        sHandle.getPluginLoaderInfo().getPluginLoader().unloadPlugin(sHandle);
    }

    @Override
    public void flushUnloaded() {
        for (PluginLoader loader : this.loaders) {
            loader.flushUnloaded();
        }
        Iterator<Map.Entry<String, SimplePluginHandle>> it = this.handles.entrySet().iterator();
        while (it.hasNext()) {
            SimplePluginHandle handle = it.next().getValue();
            if (handle.getState() == PluginState.UNLOADED) {
                it.remove();
            }
        }
    }

    protected SimplePluginHandle checkHandle(PluginHandle handle) throws PluginException {
        if (handle.getPluginManager() != this || !(handle instanceof SimplePluginHandle)) {
            throw new IllegalArgumentException("This is not our handle");
        }
        return (SimplePluginHandle) handle;
    }

    @Override
    public void checkPluginMain(PluginHandle handle, Class<?> main) throws InvalidPluginException {
        if (!Plugin.class.isAssignableFrom(main)) {
            throw new InvalidPluginException("Main class of plugin " + handle.getName() + " does not implement Plugin", handle.getFile());
        }
    }

}
