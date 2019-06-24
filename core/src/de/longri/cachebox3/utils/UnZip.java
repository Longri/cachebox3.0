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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * @author Longri from => http://stackoverflow.com/questions/981578/how-to-unzip-files-recursively-in-java
 */
public class UnZip {

    private final Logger log= LoggerFactory.getLogger(UnZip.class);

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
     *
     * @param zipFile file to extract
     *             attention in ACB2 the default is here == true
     * @return Extracted Folder Path as String
     * @throws IOException with IO error
     */
    public String extractFolder(String zipFile) throws IOException {
        return extractFolder(zipFile, false);
    }

    /**
     * Extract the given ZIP-File
     *
     * @param zipFile file to extract
     * @param here true: extract into the path where the zipfile is <br>
     *             false: extract into new path with the name of the zipfile (without extension)
     * @return Extracted Folder Path as String
     * @throws IOException with IO error
     */
    public String extractFolder(String zipFile, boolean here) throws IOException {
        log.debug("extract => " + zipFile);
        int BUFFER = 2048;
        File file = new File(zipFile);

        ZipFile zip = new ZipFile(file.getAbsolutePath());
        String newPath = zipFile.substring(0, zipFile.length() - 4);

        if (here) {
            newPath = file.getParent();
        }
        else {
            new File(newPath).mkdir();
        }
        Enumeration<?> zipFileEntries = zip.entries();

        // Process each entry
        while (zipFileEntries.hasMoreElements()) {
            // grab a zip file entry
            ZipEntry entry = (ZipEntry) zipFileEntries.nextElement();
            String currentEntry = entry.getName();
            File destFile = new File(newPath, currentEntry);
            // destFile = FileFactory.createFile(newPath, destFile.getName());
            File destinationParent = destFile.getParentFile();

            // create the parent directory structure if needed
            destinationParent.mkdirs();

            destinationParent.setLastModified(entry.getTime()); // set original Datetime to be able to import ordered oldest first

            if (!entry.isDirectory()) {
                BufferedInputStream is = new BufferedInputStream(zip.getInputStream(entry));
                int currentByte;
                // establish buffer for writing file
                byte data[] = new byte[BUFFER];

                // write the current file to disk
                FileOutputStream fos = new FileOutputStream(destFile);
                BufferedOutputStream dest = new BufferedOutputStream(fos, BUFFER);

                // read and write until last byte is encountered
                while ((currentByte = is.read(data, 0, BUFFER)) != -1) {
                    dest.write(data, 0, currentByte);
                }
                dest.flush();
                dest.close();
                is.close();
            }

            destFile.setLastModified(entry.getTime()); // set original Datetime to be able to import ordered oldest first

            if (currentEntry.endsWith(".zip")) {
                // found a zip file, try to open
                extractFolder(destFile.getAbsolutePath());
            }
        }
        zip.close();

        return newPath;
    }
}
