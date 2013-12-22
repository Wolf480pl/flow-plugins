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

import java.io.File;
import java.lang.ref.PhantomReference;
import java.lang.ref.ReferenceQueue;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.flowpowered.plugins.InvalidPluginException;
import com.flowpowered.plugins.MutablePluginHandle;
import com.flowpowered.plugins.PluginDescriptionFile;
import com.flowpowered.plugins.PluginException;
import com.flowpowered.plugins.PluginHandle;
import com.flowpowered.plugins.PluginLoader;
import com.flowpowered.plugins.PluginLoaderInfo;
import com.flowpowered.plugins.PluginManager;
import com.flowpowered.plugins.PluginState;

public abstract class JavaPluginLoader implements PluginLoader {
    private final PluginManager manager;
    private final ClassLoader parent;
    private Map<String, JavaPluginLoaderInfo> infos = new HashMap<>();
    private List<MutablePluginHandle> unloaded = new LinkedList<>();

    public JavaPluginLoader(PluginManager manager, ClassLoader parent) {
        this.manager = manager;
        this.parent = parent;
    }

    @Override
    public PluginManager getPluginManager() {
        return manager;
    }

    @Override
    public void loadPlugin(MutablePluginHandle handle) throws PluginException {
        PluginDescriptionFile pdf = handle.getDescription();
        if (pdf == null) {
            throw new IllegalArgumentException("Handle's PluginDescriptionFile must not be null.");
        }
        File file = handle.getFile();
        if (file == null) {
            throw new IllegalArgumentException("Handle's File must not be null.");
        }
        if (!file.exists()) {
            throw new InvalidPluginException("File doesn't exist", file);
        }
        String name = pdf.getName();
        JavaPluginLoaderInfo info = new JavaPluginLoaderInfo(this, name);

        URL url;
        try {
            url = file.toURI().toURL();
        } catch (MalformedURLException e) {
            throw new InvalidPluginException(e, file);
        }
        PluginClassLoader cl = new PluginClassLoader(new URL[] { url }, parent);
        info.setClassLoader(cl);
        Class<?> main;
        try {
            main = cl.findClass(pdf.getMain());
        } catch (ClassNotFoundException e) {
            throw new InvalidPluginException("Main class not found.", e, file);
        }
        Class<JavaPlugin> jMain = checkPluginMain(handle, main);
        JavaPlugin plugin;
        Constructor<JavaPlugin> c;
        try {
            c = jMain.getConstructor();
        } catch (NoSuchMethodException e) {
            throw new InvalidPluginException("Cannot instatiate plugin " + name + ". It has no public zero-argument constructor.", e, file);
        }
        try {
            plugin = c.newInstance();
        } catch (IllegalAccessException | InstantiationException e) {
            throw new InvalidPluginException("Cannot instatiate plugin " + name + ". Does it have a public zero-argument constructor?", e, file);
        } catch (ExceptionInInitializerError e) {
            throw new PluginException("Exception in initializer of plugin: " + name, e.getException());
        } catch (InvocationTargetException e) {
            throw new PluginException("Exception in constructor of plugin: " + name, e);
        }
        info.setPlugin(plugin);
        infos.put(name, info);
        handle.setPluginLoaderInfo(info);
    }

    @Override
    public void unloadPlugin(MutablePluginHandle handle) {
        PluginLoaderInfo plInfo = handle.getPluginLoaderInfo();
        if (plInfo == null) {
            throw new IllegalArgumentException("Handle's PluginLoaderInfo must not be null.");
        }
        if (plInfo.getPluginLoader() != this || !(plInfo instanceof JavaPluginLoaderInfo)) {
            throw new IllegalArgumentException("This is not our handle!");
        }
        JavaPluginLoaderInfo info = (JavaPluginLoaderInfo) plInfo;

        infos.remove(info);
        info.setPlugin(null);

        ReferenceQueue<PluginClassLoader> queue = new ReferenceQueue<>();
        PhantomReference<PluginClassLoader> ref = new PhantomReference<PluginClassLoader>(info.getClassLoader(), queue);
        info.setRefQueue(queue);
        info.setPhantom(ref);
        info.setClassLoader(null);

        handle.setState(PluginState.UNLOADING);
        unloaded.add(handle);
    }

    @Override
    public void flushUnloaded() {
        Iterator<MutablePluginHandle> it = unloaded.iterator();
        while (it.hasNext()) {
            MutablePluginHandle handle = it.next();
            JavaPluginLoaderInfo info = (JavaPluginLoaderInfo) handle.getPluginLoaderInfo();
            if (info.getRefQueue().poll() == null) {
                continue;
            }
            info.setPhantom(null);
            info.setRefQueue(null);
            handle.setState(PluginState.UNLOADED);
            it.remove();
        }
    }

    @SuppressWarnings("unchecked")
    protected Class<JavaPlugin> checkPluginMain(PluginHandle handle, Class<?> main) throws InvalidPluginException {
        if (!JavaPlugin.class.isAssignableFrom(main)) {
            throw new InvalidPluginException("Main class of plugin " + handle.getName() + " does not implement JavaPlugin", handle.getFile());
        }
        manager.checkPluginMain(handle, main);
        return (Class<JavaPlugin>) main;
    }

}
