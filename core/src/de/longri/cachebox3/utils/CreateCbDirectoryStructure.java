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
package de.longri.cachebox3.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.settings.Config;

import java.io.File;
import java.io.IOException;

/**
 * Created by Longri on 10.11.2017.
 */
public class CreateCbDirectoryStructure {

    private final String workPath;
    private final boolean replaceWorkPath;

    public CreateCbDirectoryStructure(String workPath, boolean nomedia) {

        this.workPath = workPath;
        this.replaceWorkPath = !this.workPath.equals(CB.WorkPath);

        ini_Dir(CB.WorkPath + "/user", false);
        ini_Dir(Config.PocketQueryFolder.getDefaultValue(), false);
        ini_Dir(Config.TileCacheFolder.getDefaultValue(), nomedia);
        ini_Dir(Config.TrackFolder.getDefaultValue(), false);
        ini_Dir(Config.UserImageFolder.getDefaultValue(), nomedia);
        ini_Dir(CB.WorkPath + "/repository", nomedia);
        ini_Dir(CB.WorkPath + "/repositories", nomedia);
        ini_Dir(CB.WorkPath + "/data", nomedia);
        ini_Dir(CB.WorkPath + "/user/temp", nomedia);
        ini_Dir(Config.DescriptionImageFolder.getDefaultValue(), nomedia);
        ini_Dir(Config.MapPackFolder.getDefaultValue(), nomedia);
        ini_Dir(Config.SpoilerFolder.getDefaultValue(), nomedia);
    }

    private FileHandle ini_Dir(String folder, boolean withNoMedia) {

        if (replaceWorkPath) {
            folder = folder.replace(CB.WorkPath, workPath);
        }
        FileHandle ff = Gdx.files.absolute(folder);
        if (!ff.exists()) {
            ff.mkdirs();
        }


        if (!withNoMedia) return ff;
        // prevent mediascanner to parse all the images in this folder
        File nomedia = new File(ff.file(), ".nomedia");
        if (!nomedia.exists()) {
            try {
                nomedia.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return ff;
    }
}
