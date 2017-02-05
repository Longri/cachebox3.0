/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package de.longri.cachebox3;

import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.GdxRuntimeException;
import org.robovm.apple.foundation.NSBundle;

import java.io.File;

public class LibgdxLoggerIosFileHandle extends FileHandle {
    protected LibgdxLoggerIosFileHandle(String fileName, FileType type) {
        super(fileName, type);
    }

    protected LibgdxLoggerIosFileHandle(File file, FileType type) {
        super(file, type);
    }

    @Override
    public FileHandle child(String name) {
        if (file.getPath().length() == 0) return new LibgdxLoggerIosFileHandle(new File(name), type);
        return new LibgdxLoggerIosFileHandle(new File(file, name), type);
    }

    @Override
    public FileHandle parent() {
        File parent = file.getParentFile();
        if (parent == null) {
            if (type == FileType.Absolute)
                parent = new File("/");
            else
                parent = new File("");
        }
        return new LibgdxLoggerIosFileHandle(parent, type);
    }

    @Override
    public FileHandle sibling(String name) {
        if (file.getPath().length() == 0) throw new GdxRuntimeException("Cannot get the sibling of the root.");
        return new LibgdxLoggerIosFileHandle(new File(file.getParent(), name), type);
    }

    static final String appDir = System.getenv("HOME");
    static final String externalPath = appDir + "/Documents/";
    static final String localPath = appDir + "/Library/local/";
    static final String internalPath = NSBundle.getMainBundle().getBundlePath();

    @Override
    public File file() {
        if (type == FileType.Internal) return new File(internalPath, file.getPath());
        if (type == FileType.External) return new File(externalPath, file.getPath());
        if (type == FileType.Local) return new File(localPath, file.getPath());
        return file;
    }

}
