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

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;

import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;

import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.GdxRuntimeException;

/** @author mzechner
 * @author Nathan Sweet */
public class LibgdxLoggerAndroidFileHandle extends FileHandle {
    // The asset manager, or null if this is not an internal file.

//    LibgdxLoggerAndroidFileHandle( String fileName, FileType type) {
//        super(fileName.replace('\\', '/'), type);
//    }

    LibgdxLoggerAndroidFileHandle( File file, FileType type) {
        super(file, type);

    }

    public FileHandle child (String name) {
        name = name.replace('\\', '/');
        if (file.getPath().length() == 0) return new LibgdxLoggerAndroidFileHandle( new File(name), type);
        return new LibgdxLoggerAndroidFileHandle(new File(file, name), type);
    }

    public FileHandle sibling (String name) {
        name = name.replace('\\', '/');
        if (file.getPath().length() == 0) throw new GdxRuntimeException("Cannot get the sibling of the root.");
        return Gdx.files.getFileHandle(new File(file.getParent(), name).getPath(), type); //this way we can find the sibling even if it's inside the obb
    }

    public FileHandle parent () {
        File parent = file.getParentFile();
        if (parent == null) {
            if (type == FileType.Absolute)
                parent = new File("/");
            else
                parent = new File("");
        }
        return new LibgdxLoggerAndroidFileHandle( parent, type);
    }

    public InputStream read () {
        return super.read();
    }

    public FileHandle[] list () {
        return super.list();
    }

    public FileHandle[] list (FileFilter filter) {
        return super.list(filter);
    }

    public FileHandle[] list (FilenameFilter filter) {
        return super.list(filter);
    }

    public FileHandle[] list (String suffix) {
        return super.list(suffix);
    }

    public boolean isDirectory () {
        return super.isDirectory();
    }

    public boolean exists () {
        return super.exists();
    }

    public long length () {
        return super.length();
    }

    public long lastModified () {
        return super.lastModified();
    }

    public File file () {
        if (type == FileType.Local) return new File(Gdx.files.getLocalStoragePath(), file.getPath());
        return super.file();
    }

}
