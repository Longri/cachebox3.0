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
package de.longri.cachebox3.gui.views;

import com.badlogic.gdx.utils.Disposable;
import com.kotcrab.vis.ui.VisUI;
import de.longri.cachebox3.gui.skin.styles.WayPointListItemStyle;
import de.longri.cachebox3.gui.widgets.list_view.ListViewItem;
import de.longri.cachebox3.sqlite.Database;
import de.longri.cachebox3.types.AbstractWaypoint;
import de.longri.cachebox3.types.CacheTypes;

/**
 * Created by Longri on 03.04.2017.
 */
public class WayPointListItem extends ListViewItem implements Disposable {

    public static WayPointListItem getListItem(int listIndex, final AbstractWaypoint waypoint, float targetWidth) {
        WayPointListItem listViewItem = new WayPointListItem(listIndex, waypoint.getType(),
                waypoint.getGcCode().toString(), waypoint.getTitle().toString(), waypoint.getDescription(), waypoint.FormatCoordinate());
        listViewItem.setWidth(targetWidth);
        listViewItem.invalidate();
        listViewItem.pack();
        return listViewItem;
    }

    private final WayPointItem wayPointItem;

    private WayPointListItem(int listIndex, CacheTypes type, CharSequence wayPointGcCode, CharSequence wayPointTitle,
                             CharSequence description, CharSequence coord) {
        super(listIndex);
        WayPointListItemStyle style = VisUI.getSkin().get("WayPointListItems", WayPointListItemStyle.class);
        this.wayPointItem = new WayPointItem(type, wayPointGcCode, wayPointTitle, description, coord, style);
        this.add(wayPointItem).expand().fill();
    }


    public boolean update(float bearing, CharSequence distance) {
        if (!this.wayPointItem.distanceOrBearingChanged) return false;
        this.wayPointItem.arrowImage.setRotation(bearing);
        this.wayPointItem.distanceLabel.setText(distance);

        this.wayPointItem.arrowImage.layout();
        this.wayPointItem.distanceLabel.layout();
        this.wayPointItem.distanceOrBearingChanged = false;
        return true;
    }

    public void posOrBearingChanged() {
        this.wayPointItem.distanceOrBearingChanged = true;
    }

    @Override
    public synchronized void dispose() {
        this.wayPointItem.dispose();
    }

    public CharSequence getWaypointGcCode() {
        return this.wayPointItem.wayPointGcCode;
    }
}
