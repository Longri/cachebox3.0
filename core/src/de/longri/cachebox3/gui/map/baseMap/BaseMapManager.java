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
package de.longri.cachebox3.gui.map.baseMap;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.Utils;
import de.longri.cachebox3.settings.Config;
import de.longri.cachebox3.utils.lists.CB_List;

import java.io.FileFilter;

/**
 * Created by Longri on 22.02.2017.
 */
public final class BaseMapManager extends CB_List<AbstractManagedMapLayer> {

    public final static BaseMapManager INSTANCE = new BaseMapManager();

    public BaseMapManager() {
    }

    public void refreshMaps() {
        refreshMaps(Gdx.files.absolute(CB.WorkPath));
    }

    public void refreshMaps(FileHandle workPath) {
        this.clear();
        // add map files from repository
        String repositoryMaps = addMapFiles(workPath.child("repository").child("maps"));

        // add map files from act selected repository
        String actRepositoryName = Config.DatabaseName.getValue().replace(".db3", "");
        addMapFiles(workPath.child("repositories").child(actRepositoryName).child("maps"));

        // add map files from user map folder
        FileHandle userMapFolder = Gdx.files.absolute(Config.MapPackFolder.getValue());
        if (!userMapFolder.path().equals(repositoryMaps))
            addMapFiles(userMapFolder);

        this.addStaticMaps();
        this.sort();
    }

    private String addMapFiles(FileHandle workPath) {
        FileFilter mapFileFilter = pathname -> Utils.getFileExtension(pathname.getAbsolutePath()).toLowerCase().equals("map");
        FileHandle[] mapFiles = workPath.list(mapFileFilter);

        for (FileHandle mapFile : mapFiles) {
            this.add(new MapsforgeSingleMap(mapFile));
        }
        return workPath.path();
    }

    private void addStaticMaps() {
        this.add(new HikeBike());
        this.add(new StamenWaterColor());
        this.add(new StamenToner());
        this.add(new OSciMap());
        this.add(new OpenStreetMapTransport());
        this.add(new OpenStreetMap());
        this.add(new ImagicoLandCover());
        this.add(new HikeBikeHillShade());
    }
}
