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
import de.longri.serializable.NotImplementedException;
import de.longri.serializable.Serializable;
import de.longri.serializable.StoreBase;

/**
 * Created by longri on 30.10.17.
 */
public class ServerFile implements Serializable {

    private String name;
    private String parent;
    private Array<ServerFile> files = new Array<>();


    public ServerFile(String parent, String name) {
        this.name = name;
        this.parent = parent;
    }

    public ServerFile() {

    }

    @Override
    public void serialize(StoreBase storeBase) throws NotImplementedException {
        storeBase.write(name);
        storeBase.write(parent);
        storeBase.write(files.size);
        for (int i = 0, n = files.size; i < n; i++) {
            ServerFile child = files.get(i);
            child.serialize(storeBase);
        }
    }

    @Override
    public void deserialize(StoreBase storeBase) throws NotImplementedException {
        name = storeBase.readString();
        parent = storeBase.readString();
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
        return files.size > 0;
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

    public String toString() {
        return (isDirectory() ? "DIR:" : "") + name;
    }


    public static ServerFile getDirectory(FileHandle fileHandle) {
        ServerFile root = getChilds(new ServerFile("", fileHandle.name()), fileHandle);
        return root;
    }

    private static ServerFile getChilds(ServerFile root, FileHandle fileHandle) {
        FileHandle[] list = fileHandle.list();
        String parentPath = root.parent + "/" + root.name;
        for (int i = 0, n = list.length; i < n; i++) {
            FileHandle handle = list[i];
            if (handle.isDirectory()) {
                ServerFile dir = new ServerFile(parentPath, list[i].name());
                root.addFile(getChilds(dir, handle));
            } else {
                root.addFile(new ServerFile(parentPath, handle.name()));
            }
        }
        return root;
    }


    public String getAbsolute() {
        return parent + "/" + name;
    }
}
