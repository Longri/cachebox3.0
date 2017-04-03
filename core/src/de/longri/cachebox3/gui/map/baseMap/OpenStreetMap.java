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

import org.oscim.layers.tile.TileLayer;
import org.oscim.layers.tile.bitmap.BitmapTileLayer;
import org.oscim.map.Map;
import org.oscim.tiling.source.bitmap.DefaultSources;

/**
 * Created by Longri on 22.02.2017.
 */
public class OpenStreetMap extends AbstractBitmapLayer {
    public OpenStreetMap() {
        super("Open Street Map", false, true);
    }

    @Override
    public TileLayer getTileLayer(Map map) {
        return new BitmapTileLayer(map, DefaultSources.OPENSTREETMAP.build());
    }
}
