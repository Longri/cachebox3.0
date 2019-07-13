/*
 * Copyright (C) 2016-2017 team-cachebox.de
 *
 * Licensed under the : GNU General Public License (GPL);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.gnu.org/licenses/gpl.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.longri.cachebox3.utils;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import de.longri.cachebox3.callbacks.GenericCallBack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.channels.FileChannel;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * @author Longri from => http://stackoverflow.com/questions/981578/how-to-unzip-files-recursively-in-java
 */
public class UnZip {

    private final Logger log = LoggerFactory.getLogger(UnZip.class);

    /**
     * Extract the given ZIP-File
     *
     * @param zipFile file to extract
     * @return Extracted Folder Path as String
     * @throws IOException with IO error
     */
    public FileHandle extractFolder(FileHandle zipFile) throws IOException {
        String path = zipFile.file().getAbsolutePath();
        String resultPath = extractFolder(path);
        return Gdx.files.absolute(resultPath);
    }

    /**
     * Extract the given ZIP-File
     *
     * @param zipFile file to extract
     * @return Extracted Folder Path as String
     * @throws IOException with IO error
     */
    public FileHandle extractFolder(FileHandle zipFile, GenericCallBack<Double> progressCallBack) throws IOException {
        String path = zipFile.file().getAbsolutePath();
        String resultPath = extractFolder(path, progressCallBack, new AtomicBoolean(false));
        return Gdx.files.absolute(resultPath);
    }

    /**
     * @param zipFile file to extract
     *                attention in ACB2 the default is here == true
     * @return Extracted Folder Path as String
     * @throws IOException with IO error
     */
    public String extractFolder(String zipFile) throws IOException {
        return extractFolder(zipFile, null, new AtomicBoolean(false));
    }

    public String extractFolder(FileHandle fileHandle, GenericCallBack<Double> progressCallBack, AtomicBoolean cancel) throws IOException {
        if (cancel == null) cancel = new AtomicBoolean(false);
        return extractFolder(fileHandle.file().getAbsolutePath(), progressCallBack, cancel);
    }


    /*
    https://stackoverflow.com/questions/40050270/java-unzip-and-progress-bar
     */
    private String extractFolder(String file, GenericCallBack<Double> progressCallBack, AtomicBoolean cancel) throws IOException {

        String newPath = file.substring(0, file.length() - 4);
        File folder = new File(newPath);
        File zipfile = new File(file);

        FileInputStream is = new FileInputStream(zipfile.getCanonicalFile());
        FileChannel channel = is.getChannel();
        ZipInputStream zis = new ZipInputStream(new BufferedInputStream(is));
        ZipEntry ze = null;
        try {
            while ((ze = zis.getNextEntry()) != null && !cancel.get()) {
                File f = new File(folder.getCanonicalPath(), ze.getName());
                if (ze.isDirectory()) {
                    f.mkdirs();
                    continue;
                }
                f.getParentFile().mkdirs();
                OutputStream fos = new BufferedOutputStream(new FileOutputStream(f));
                try {
                    try {
                        final byte[] buf = new byte[1024 * 1024];
                        int bytesRead;
                        long nread = 0L;
                        long length = zipfile.length();

                        while (-1 != (bytesRead = zis.read(buf)) && !cancel.get()) {
                            fos.write(buf, 0, bytesRead);
                            nread += bytesRead;
                            if (progressCallBack != null) {
                                progressCallBack.callBack(((double) length / (double) channel.position()) * 100.0);
                            }
                            //updateProgress(channel.position(), length);
                        }
                    } finally {
                        fos.close();
                    }
                } catch (final IOException ioe) {
                    f.delete();
                    throw ioe;
                }
            }
        } finally {
            zis.close();
        }
        return newPath;
    }
}
