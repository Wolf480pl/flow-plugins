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
package com.flowpowered.plugins;

import java.io.File;

public class InvalidPluginException extends PluginException {
    private File file;

    public InvalidPluginException(String message, File file) {
        super(file.getPath());
        this.file = file;
    }

    public InvalidPluginException(Throwable cause, File file) {
        super(cause);
        this.file = file;
    }

    public InvalidPluginException(String message, InvalidPluginException cause) {
        super(cause);
        file = cause.file;
    }

    public InvalidPluginException(String message, Throwable cause, File file) {
        super(file.getPath(), cause);
        this.file = file;
    }

    public File getFile() {
        return file;
    }

    @Override
    public String getMessage() {
        return super.getMessage() + " - " + file.getPath();
    }
}
