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

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.logging.log4j.Logger;

import com.flowpowered.plugins.MutablePluginHandle;
import com.flowpowered.plugins.Plugin;
import com.flowpowered.plugins.PluginDescriptionFile;
import com.flowpowered.plugins.PluginException;
import com.flowpowered.plugins.PluginLoaderInfo;
import com.flowpowered.plugins.PluginManager;
import com.flowpowered.plugins.PluginState;

public abstract class AbstractPluginHandle implements MutablePluginHandle {

    private final PluginManager manager;
    private final String name;
    private PluginLoaderInfo loaderInfo;
    private PluginDescriptionFile description;
    private File file;
    private Logger logger;
    private PluginState state;

    public AbstractPluginHandle(PluginManager manager, String name) {
        this.manager = manager;
        this.name = name;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public PluginManager getPluginManager() {
        return this.manager;
    }

    @Override
    public PluginLoaderInfo getPluginLoaderInfo() {
        return this.loaderInfo;
    }

    @Override
    public void setPluginLoaderInfo(PluginLoaderInfo info) {
        this.loaderInfo = info;
    }

    @Override
    public Plugin getPlugin() {
        return this.loaderInfo.getPlugin();
    }

    @Override
    public PluginDescriptionFile getDescription() {
        return this.description;
    }

    public void setDescription(PluginDescriptionFile description) {
        this.description = description;
    }

    @Override
    public File getFile() {
        return this.file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    @Override
    public Logger getLogger() {
        return this.logger;
    }

    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    @Override
    public PluginState getState() {
        return this.state;
    }

    @Override
    public void setState(PluginState state) {
        this.state = state;
    }

    @Override
    public void enable() throws PluginException {
        this.manager.enablePlugin(this);
    }

    @Override
    public void disable() throws PluginException {
        this.manager.disablePlugin(this);
    }

    @Override
    public void unload() throws PluginException {
        this.manager.unloadPlugin(this);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(3, 17).append(this.name).toHashCode();
    }

}