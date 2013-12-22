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

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.Set;

import com.flowpowered.plugins.PluginDescriptionFile.ReloadingSupport;
import com.flowpowered.plugins.PluginLoaderInfo;
import com.flowpowered.plugins.PluginManager;

public class SimplePluginHandle extends AbstractPluginHandle {
    private Set<SimplePluginHandle> dependencies;
    private Set<SimplePluginHandle> softDependencies;
    private Set<WeakReference<SimplePluginHandle>> dependents;
    private Set<WeakReference<SimplePluginHandle>> softDependents;

    public SimplePluginHandle(PluginManager manager, String name) {
        super(manager, name);
    }

    @Override
    public Set<SimplePluginHandle> getDepencencies() {
        return Collections.unmodifiableSet(this.dependencies);
    }

    @Override
    public Set<SimplePluginHandle> getSoftDepencencies() {
        return Collections.unmodifiableSet(this.softDependencies);
    }

    public Set<WeakReference<SimplePluginHandle>> getDependents() {
        return Collections.unmodifiableSet(this.dependents);
    }

    public Set<WeakReference<SimplePluginHandle>> getSoftDependents() {
        return Collections.unmodifiableSet(this.softDependents);
    }

    @Override
    public boolean canReload() {
        ReloadingSupport support = getDescription().supportsReloading();
        if (support == ReloadingSupport.SOMETIMES) {
            PluginLoaderInfo loaderInfo = getPluginLoaderInfo();
            if (loaderInfo != null && loaderInfo.getPlugin() != null) {
                // TODO: Should we check the state before doing this?
                return loaderInfo.getPlugin().canReload();
            } else {
                // What now? Ok, I guess the plugin is being unloaded, so either it did allow reloading when the unloading started, or everything is shutting down
                return true;
            }
        }
        return support == ReloadingSupport.YES;
    }
}