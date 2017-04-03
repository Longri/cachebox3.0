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

import org.oscim.layers.GroupLayer;
import org.oscim.layers.Layer;
import org.oscim.layers.tile.TileLayer;
import org.oscim.layers.tile.buildings.BuildingLayer;
import org.oscim.layers.tile.vector.VectorTileLayer;
import org.oscim.layers.tile.vector.labeling.LabelLayer;
import org.oscim.map.Map;
import org.oscim.tiling.TileSource;

/**
 * Created by Longri on 22.02.2017.
 */
public abstract class AbstractVectorLayer extends AbstractManagedMapLayer {
    private VectorTileLayer vectorTileLayer;

    public AbstractVectorLayer(String name, boolean isOverlay, boolean isOnline) {
        super(name, isOverlay, isOnline);
    }

    protected abstract VectorTileLayer createVectorTileLayer(Map map);

    @Override
    public boolean isVector() {
        return true;
    }

    @Override
    public TileLayer getTileLayer(Map map) {
        if (vectorTileLayer == null) vectorTileLayer = createVectorTileLayer(map);
        return vectorTileLayer;
    }


    @Override
    public Layer getLayer(Map map) {
        if (vectorTileLayer == null) vectorTileLayer = createVectorTileLayer(map);
        GroupLayer layer = new GroupLayer(map);
        layer.layers.add(new BuildingLayer(map, vectorTileLayer));
        layer.layers.add(new LabelLayer(map, vectorTileLayer));
        return layer;
    }

    public abstract TileSource getVectorTileSource();
}
