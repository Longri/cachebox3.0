/*
 * Copyright (C) 2018 team-cachebox.de
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
package de.longri.cachebox3.gui.widgets;

import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.VisTable;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.events.EventHandler;
import de.longri.cachebox3.gui.skin.styles.MapBubbleStyle;
import de.longri.cachebox3.gui.views.CacheItem;
import de.longri.cachebox3.gui.views.WayPointItem;
import de.longri.cachebox3.gui.widgets.catch_exception_widgets.Catch_Table;
import de.longri.cachebox3.types.AbstractCache;
import de.longri.cachebox3.types.AbstractWaypoint;
import de.longri.cachebox3.types.LogTypes;
import org.oscim.core.MercatorProjection;

/**
 * Created by Longri on 31.01.2018.
 */
public class MapBubble extends Catch_Table {

    private final MapBubbleStyle style;
    private final AbstractCache cache;
    private final AbstractWaypoint waypoint;

    public MapBubble(Object dataObject) {
        this(dataObject instanceof AbstractCache ? (AbstractCache) dataObject : null, dataObject instanceof AbstractWaypoint ? (AbstractWaypoint) dataObject : null);
    }

    private MapBubble(AbstractCache cache, AbstractWaypoint waypoint) {
        this.cache = cache;
        this.waypoint = waypoint;
        style = VisUI.getSkin().get("bubble", MapBubbleStyle.class);

        boolean isSelected = false;
        VisTable content;
        if (cache != null) {

            LogTypes left = null;
            LogTypes right = null;
            boolean isAvailable = true;
            if (cache.isFound()) {
                left = LogTypes.found;
            }

            if (!cache.isAvailable()) {
                right = LogTypes.temporarily_disabled;
                isAvailable = false;
            }

            if (cache.isArchived()) {
                right = LogTypes.archived;
                isAvailable = false;
            }

            content = new CacheItem(null, cache.getName(),
                    (int) (cache.getDifficulty() * 2), (int) (cache.getTerrain() * 2),
                    (int) Math.min(cache.getRating() * 2, 5 * 2), cache.getSize(),
                    cache.getSize().toShortString(), left, right, isAvailable, cache.getFavoritePoints(), style.cacheListItemStyle);
            isSelected = (EventHandler.getSelectedWaypoint() == null && cache == EventHandler.getSelectedCache());
        } else if (waypoint != null) {

            content = new WayPointItem(waypoint.getType(),
                    waypoint.getGcCode().toString(), waypoint.getTitle().toString(),
                    "", waypoint.FormatCoordinate(), style.wayPointListItemStyle);
            isSelected = waypoint == EventHandler.getSelectedWaypoint();

        } else {
            content = null;
        }

        this.add(content).expand().fill();
        this.setBackground(isSelected ? style.selectedBackground : style.background);

    }

    public float getOffsetX() {
        return CB.getScaledFloat(style.offsetX);
    }

    public float getOffsetY() {
        return CB.getScaledFloat(style.offsetY);
    }


    public float getMinWidth() {
        if (style.minWidth <= 0) return super.getWidth();
        return CB.getScaledFloat(style.minWidth);
    }

    public double getCoordX() {
        if (cache != null) {
            return MercatorProjection.longitudeToX(cache.longitude);
        }
        if (waypoint != null) {
            return MercatorProjection.longitudeToX(waypoint.longitude);
        }
        return 0;
    }

    public double getCoordY() {
        if (cache != null) {
            return MercatorProjection.latitudeToY(cache.latitude);
        }
        if (waypoint != null) {
            return MercatorProjection.latitudeToY(waypoint.latitude);
        }
        return 0;
    }
}
