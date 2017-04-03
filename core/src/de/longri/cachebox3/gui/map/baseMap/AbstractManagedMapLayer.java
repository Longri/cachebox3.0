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

import org.oscim.layers.Layer;
import org.oscim.layers.tile.TileLayer;
import org.oscim.map.Map;

/**
 * Created by Longri on 22.02.2017.
 */
public abstract class AbstractManagedMapLayer implements Comparable<AbstractManagedMapLayer> {
    public final boolean isOverlay;
    public final boolean isOnline;
    public final String name;

    public AbstractManagedMapLayer(String name, boolean isOverlay, boolean isOnline) {
        this.isOverlay = isOverlay;
        this.isOnline = isOnline;
        this.name = name;
    }

    @Override
    public int compareTo(AbstractManagedMapLayer o) {
        return this.name.compareTo(o.name);
    }

    public abstract boolean isVector();

    public abstract TileLayer getTileLayer(Map map);

    public abstract Layer getLayer(Map map);
}
