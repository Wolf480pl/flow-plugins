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
package com.flowpowered.plugins.java;

import java.lang.ref.PhantomReference;
import java.lang.ref.ReferenceQueue;

import com.flowpowered.plugins.PluginLoaderInfo;
import com.flowpowered.plugins.artifact.jvm.PluginClassLoader;

public class JavaPluginLoaderInfo implements PluginLoaderInfo {
    private final JavaPluginLoader loader;
    private final String name;
    private JavaPlugin plugin;
    private PluginClassLoader classLoader;
    private PhantomReference<PluginClassLoader> phantom;
    private ReferenceQueue<PluginClassLoader> refQueue;

    public JavaPluginLoaderInfo(JavaPluginLoader loader, String name) {
        this.loader = loader;
        this.name = name;
    }

    @Override
    public JavaPluginLoader getPluginLoader() {
        return loader;
    }

    @Override
    public JavaPlugin getPlugin() {
        return plugin;
    }

    public void setPlugin(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public PluginClassLoader getClassLoader() {
        return classLoader;
    }

    public void setClassLoader(PluginClassLoader loader) {
        classLoader = loader;
    }

    public PhantomReference<PluginClassLoader> getPhantom() {
        return phantom;
    }

    public void setPhantom(PhantomReference<PluginClassLoader> phantom) {
        this.phantom = phantom;
    }

    public ReferenceQueue<PluginClassLoader> getRefQueue() {
        return refQueue;
    }

    public void setRefQueue(ReferenceQueue<PluginClassLoader> refQueue) {
        this.refQueue = refQueue;
    }
}
