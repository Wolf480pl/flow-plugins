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
package com.flowpowered.plugins.artifact.jvm;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.googlecode.concurrentlinkedhashmap.ConcurrentLinkedHashMap;

public class PluginClassLoader extends URLClassLoader implements ArtifactClassLoader {
    // TODO: Rename it to something more appropriate and probably move to a different package, as not all ArtifactLoaders will necessarily use it.
    static {
        ClassLoader.registerAsParallelCapable();
    }

    private final ConcurrentMap<String, Class<?>> cache = new ConcurrentHashMap<>();
    private final Set<ClassLoader> dependencies = Collections.newSetFromMap(new ConcurrentLinkedHashMap.Builder<ClassLoader, Boolean>().build());

    public PluginClassLoader(URL[] urls, ClassLoader parent) {
        super(urls, parent);
    }

    @Override
    public boolean addDependency(ClassLoader dependency) {
        return dependencies.add(dependency);
    }

    @Override
    public boolean addDependencies(Collection<ClassLoader> deps) {
        return dependencies.addAll(deps);
    }

    @Override
    public Class<?> findClass(String name) throws ClassNotFoundException {
        // Parent has been already checked by the ClassLoader.loadClass(String, boolean)
        // 1st step: check cache
        Class<?> result = cache.get(name);
        if (result != null) {
            return result;
        }

        List<ClassNotFoundException> exceptions = new LinkedList<>();

        try {
            // 2st step: check this classloader
            result = findOurClass(name);
        } catch (ClassNotFoundException e) {
            exceptions.add(e);
        }

        if (result == null) {
            // 3rd step: check all the dependecies
            for (ClassLoader cl : dependencies) {
                try {
                    result = cl.loadClass(name);
                } catch (ClassNotFoundException e) {
                    exceptions.add(e);
                }
                if (result != null) {
                    break;
                }
            }
        }

        if (result == null) {
            // if we haven't found the class, put all the caught exceptions together and throw
            if (exceptions.isEmpty()) {
                throw new ClassNotFoundException();
            } else {
                ClassNotFoundException e = exceptions.remove(0);
                for (Exception e1 : exceptions) {
                    e.addSuppressed(e1);
                }
                throw e;
            }
        }

        cache.putIfAbsent(name, result);
        return result;
    }

    protected Class<?> findOurClass(String name) throws ClassNotFoundException {
        return super.findClass(name);
    }

    @Override
    public ClassLoader getClassLoader() {
        return this;
    }
}
