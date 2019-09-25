/*
 * Copyright (C) 2016 team-cachebox.de
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


import com.badlogic.gdx.utils.Array;
import de.longri.cachebox3.Utils;

import java.io.File;
import java.util.Comparator;

public class FileList extends Array<File> implements Comparator<File> {

    public FileList(String path, String extension) {
        ini(path, extension, false);
    }

    public FileList(String path, String extension, boolean AbsolutePath) {
        ini(path, extension, AbsolutePath);
    }

    private void ini(String path, String extension, boolean AbsolutePath) {
        File dir = new File(path);
        String[] files = dir.list();
        String absolutePath = AbsolutePath ? path + "/" : "";
        if (!(files == null)) {
            if (files.length > 0) {
                for (String file : files) {
                    if (Utils.getFileExtension(file).equalsIgnoreCase(extension)) {
                        File newfile = new File(absolutePath + file);
                        this.add(newfile);
                    }
                }
            }
        }
        Resort();
    }

    public void Resort() {
        //TODO resort
    }

    @Override
    public int compare(File object1, File object2) {
        if (object1.lastModified() > object2.lastModified())
            return 1;
        else if (object1.lastModified() < object2.lastModified())
            return -1;
        else
            return 0;
    }

}
