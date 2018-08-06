/*
 * Copyright (C) 2018 team-cachebox.de
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
package de.longri.cachebox3.file_transfer;

import de.longri.cachebox3.socket.filebrowser.ServerFile;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

/**
 * Created by Longri on 27.02.2018.
 */
public class FileIconUtils {

    private final static Logger log = LoggerFactory.getLogger(FileIconUtils.class);
    public final static boolean IS_WINDOWS = System.getProperty("os.name").toLowerCase().contains("win");
    public final static boolean IS_Mac = System.getProperty("os.name").toLowerCase().contains("mac");
    private final static HashMap<String, Image> mapOfFileExtToSmallIcon = new HashMap<>();
    private final static JFileChooser FILE_CHOOSER = new JFileChooser();

    public static Image getFileIcon(File file) {
        final String ext = getFileExt(file);

        Image fileIcon = mapOfFileExtToSmallIcon.get(ext);
        if (fileIcon == null) {

            javax.swing.Icon jswingIcon = null;

            if (file.exists()) {
                jswingIcon = getJSwingIconFromFileSystem(file);
            } else {
                File tempFile = null;
                try {
                    tempFile = File.createTempFile("icon", ext);
                    jswingIcon = getJSwingIconFromFileSystem(tempFile);
                } catch (IOException ignored) {
                    // Cannot create temporary file.
                } finally {
                    if (tempFile != null) {
                        if (!tempFile.delete()) {
                            log.warn("can't delete temp File");
                        }
                    }
                }
            }

            if (jswingIcon != null) {
                fileIcon = jswingIconToImage(jswingIcon);
                mapOfFileExtToSmallIcon.put(ext, fileIcon);
            }
        }
        return fileIcon;
    }

    public static Image getFileIcon(ServerFile item) {
        File file = new File(item.getName());

        final String ext = getFileExt(item, file);


        Image fileIcon = mapOfFileExtToSmallIcon.get(ext);
        if (fileIcon == null) {

            javax.swing.Icon jswingIcon = null;


            if (file.exists()) {
                jswingIcon = getJSwingIconFromFileSystem(file);
            } else {
                File tempFile = null;
                try {
                    tempFile = File.createTempFile("icon", ext);
                    if (ext.equals("%")) {
                        tempFile = tempFile.getParentFile();
                    }
                    jswingIcon = getJSwingIconFromFileSystem(tempFile);
                } catch (IOException ignored) {
                    // Cannot create temporary file.
                } finally {
                    if (tempFile != null) {
                        if (!tempFile.delete()) {
                            log.warn("can't delete temp File");
                        }
                    }
                }
            }

            if (jswingIcon != null) {
                fileIcon = jswingIconToImage(jswingIcon);
                mapOfFileExtToSmallIcon.put(ext, fileIcon);
            }
        }
        return fileIcon;
    }

    private static String getFileExt(ServerFile item, File file) {
        if (item.isDirectory()) {
            if (item.getParent().isEmpty()) {
                return "#"; //Disk root
            }
            return "%"; //folder
        }

        String name = item.getName();
        int pos = name.lastIndexOf('.');
        if (pos > 0) {
            return name.substring(pos).toLowerCase();
        }

        return item.getName();
    }

    private static String getFileExt(File file) {
        if (file.isDirectory()) {
            if (file.getParent() == null || (IS_Mac && file.getParent().equals("/Volumes"))) {
                return "#"; //Disk root
            }
            return "%"; //folder
        }
        return FILE_CHOOSER.getTypeDescription(file).toLowerCase();
    }

    private static javax.swing.Icon getJSwingIconFromFileSystem(File file) {
        javax.swing.Icon icon;
        if (IS_WINDOWS) {
            FileSystemView view = FileSystemView.getFileSystemView();
            icon = view.getSystemIcon(file);
        } else {
            icon = FILE_CHOOSER.getUI().getFileView(FILE_CHOOSER).getIcon(file);
        }
        return icon;
    }


    private static Image jswingIconToImage(javax.swing.Icon jswingIcon) {
        BufferedImage bufferedImage = new BufferedImage(jswingIcon.getIconWidth(), jswingIcon.getIconHeight(),
                BufferedImage.TYPE_INT_ARGB);
        jswingIcon.paintIcon(null, bufferedImage.getGraphics(), 0, 0);
        return SwingFXUtils.toFXImage(bufferedImage, null);
    }
}
