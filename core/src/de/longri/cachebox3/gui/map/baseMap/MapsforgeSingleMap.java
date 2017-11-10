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

import com.badlogic.gdx.files.FileHandle;
import org.oscim.layers.tile.vector.OsmTileLayer;
import org.oscim.layers.tile.vector.VectorTileLayer;
import org.oscim.map.Map;
import org.oscim.tiling.TileSource;
import org.oscim.tiling.source.mapfile.MapFileTileSource;
import org.oscim.tiling.source.mapfile.MapInfo;

/**
 * Created by Longri on 22.02.2017.
 */
public class MapsforgeSingleMap extends AbstractVectorLayer {
    final FileHandle mapFile;
    MapFileTileSource tileSource;

    public MapsforgeSingleMap(FileHandle mapFile) {
        super(mapFile.nameWithoutExtension(), false, false);
        this.mapFile = mapFile;
    }

    @Override
    protected VectorTileLayer createVectorTileLayer(Map map) {
        if (tileSource == null) {
            tileSource = new MapFileTileSource();
            tileSource.setMapFile(mapFile.path());
            tileSource.setPreferredLanguage("en");
        }
        VectorTileLayer vectorTileLayer = new OsmTileLayer(map);
        vectorTileLayer.setTileSource(tileSource);
        return vectorTileLayer;
    }

    @Override
    public TileSource getVectorTileSource() {
        if (tileSource == null) {
            tileSource = new MapFileTileSource();
            tileSource.setMapFile(mapFile.path());
            tileSource.setPreferredLanguage("en");
        }
        return tileSource;
    }

    public boolean isFreizeitKarte() {
        if (tileSource == null) {
            tileSource = new MapFileTileSource();
            tileSource.setMapFile(mapFile.path());
            tileSource.setPreferredLanguage("en");
        }

        //check type of mapsforge
        tileSource.open();
        MapInfo info = tileSource.getMapInfo();
        if (info != null && info.comment != null && info.comment.contains("FZK")) {
            tileSource.close();
            return true;
        }
        tileSource.close();
        return false;
    }
}
