/*
javafxfilebrowsedemo - Demo application for browsing files in a JavaFX TreeView
Copyright (C) 2012 Hugues Johnson

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; version 2.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See
the GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software 
Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package de.longri.cachebox3.file_transfer;

import java.io.File;

import javafx.scene.control.TreeItem;
import javafx.scene.image.ImageView;

public class FilePathTreeItem extends TreeItem<String> {

    //this stores the full path to the file or directory
    private String fullPath;
    public final File file;

    public String getFullPath() {
        return (this.fullPath);
    }

    private boolean isDirectory;

    public boolean isDirectory() {
        return (this.isDirectory);
    }

    public FilePathTreeItem(File file) {
        super(file.toString());
        this.file = file;
        this.fullPath = file.getAbsolutePath();

        //test if this is a directory and set the icon
        if (file.isDirectory()) {
            this.isDirectory = true;
            this.setGraphic(new ImageView(FileIconUtils.getFileIcon(file)));
        } else {
            this.isDirectory = false;
            this.setGraphic(new ImageView(FileIconUtils.getFileIcon(file)));
            //if you want different icons for different file types this is where you'd do it
        }

        //set the value
        if (!fullPath.endsWith(File.separator)) {
            //set the value (which is what is displayed in the tree)
            String value = file.toString();
            int indexOf = value.lastIndexOf(File.separator);
            if (indexOf > 0) {
                this.setValue(value.substring(indexOf + 1));
            } else {
                this.setValue(value);
            }
        }

    }
}
