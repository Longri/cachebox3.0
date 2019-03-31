/*
 * Copyright (C) 2017 team-cachebox.de
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
package de.longri.cachebox3.socket.filebrowser;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import de.longri.serializable.Serializable;
import de.longri.serializable.StoreBase;
import javafx.scene.input.Dragboard;

import java.io.File;

/**
 * Created by longri on 30.10.17.
 */
public class ServerFile implements Serializable {

    private String name;
    private String parent;
    private Array<ServerFile> files = new Array<>();
    private boolean isDir = false;
    private Dragboard dragBoard;


    public ServerFile(String parent, String name, boolean isDir) {
        this.name = name;
        this.parent = parent;
        this.isDir = isDir;
    }

    public ServerFile() {

    }

    @Override
    public void serialize(StoreBase storeBase) {
        storeBase.write(name);
        storeBase.write(parent);
        storeBase.write(isDir);
        storeBase.write(files.size);
        for (int i = 0, n = files.size; i < n; i++) {
            ServerFile child = files.get(i);
            child.serialize(storeBase);
        }
    }

    @Override
    public void deserialize(StoreBase storeBase) {
        name = storeBase.readString();
        parent = storeBase.readString();
        isDir = storeBase.readBool();
        int childCount = storeBase.readInt();

        if (childCount > 0) {
            for (int i = 0; i < childCount; i++) {
                ServerFile child = new ServerFile();
                child.deserialize(storeBase);
                files.add(child);
            }
        }
    }

    public boolean isDirectory() {
        return isDir;
    }

    public void addFile(ServerFile file) {
        files.add(file);
    }

    public String getName() {
        return name;
    }

    public Array<ServerFile> getFiles() {
        return files;
    }


    public static ServerFile getDirectory(FileHandle fileHandle) {
        ServerFile root = getChilds(new ServerFile("", fileHandle.name(), fileHandle.isDirectory()), fileHandle);
        return root;
    }

    private static ServerFile getChilds(ServerFile root, FileHandle fileHandle) {
        FileHandle[] list = fileHandle.list();
        String parentPath = root.parent + "/" + root.name;
        for (int i = 0, n = list.length; i < n; i++) {
            FileHandle handle = list[i];
            if (handle.isDirectory()) {
                ServerFile dir = new ServerFile(parentPath, list[i].name(), list[i].isDirectory());
                root.addFile(getChilds(dir, handle));
            } else {
                root.addFile(new ServerFile(parentPath, handle.name(), handle.isDirectory()));
            }
        }
        return root;
    }

    @Override
    public int hashCode() {
        return getAbsolute().hashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) return true;
        if (other instanceof ServerFile) {
            return (this.getAbsolute().equals(((ServerFile) other).getAbsolute()));
        }
        return false;
    }

    @Override
    public String toString() {
        return name;
    }


    public String getTransferPath(ServerFile rootDir, File file) {
        return getAbsolute().replace(rootDir.getAbsolute(), "") + "/" + file.getName();
    }

    public ServerFile child(String name, boolean isDir) {
        return new ServerFile(this.getAbsolute(), name, isDir);
    }

    public void setDragBoard(Dragboard dragBoard) {
        this.dragBoard = dragBoard;
    }

    public Dragboard getDragBoard() {
        return dragBoard;
    }

    public String getParent() {
        return parent;
    }

    public ServerFile getChild(String name) {
        for (ServerFile file : files) {
            if (file.name.equals(name)) return file;
        }
        return null;
    }

    public String getAbsoluteWithoutRoot() {
        int pos = parent.indexOf("/", 1);
        String path = "";
        if (pos > 0) {
            path = parent.substring(pos);
        }
        return path + "/" + name;
    }

    public String getAbsolute() {
        return parent + "/" + name;
    }

}
