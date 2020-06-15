/*
 * Copyright (C) 2020 team-cachebox.de
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
import de.longri.cachebox3.types.CacheTypes;

/**
 * Created by Longri on 03.04.2017.
 */
public class WayPointListItem extends ListViewItem implements Disposable {

    private final WayPointItem wayPointItem;

    WayPointListItem(int listIndex, CacheTypes type, CharSequence wayPointGcCode, CharSequence wayPointTitle, CharSequence description, CharSequence coord) {
        super(listIndex);
        WayPointListItemStyle wayPointListItemStyle = VisUI.getSkin().get(WayPointListItemStyle.class);
        wayPointItem = new WayPointItem(type, wayPointGcCode, wayPointTitle, description, coord, wayPointListItemStyle);
        add(wayPointItem).expand().fill();
    }

    public boolean update(float bearing, CharSequence distance) {
        if (!wayPointItem.distanceOrBearingChanged) return false;
        wayPointItem.arrowImage.setRotation(bearing);
        wayPointItem.distanceLabel.setText(distance);

        wayPointItem.arrowImage.layout();
        wayPointItem.distanceLabel.layout();
        wayPointItem.distanceOrBearingChanged = false;
        return true;
    }

    public void posOrBearingChanged() {
        wayPointItem.distanceOrBearingChanged = true;
    }

    @Override
    public synchronized void dispose() {
        wayPointItem.dispose();
    }

    public CharSequence getWaypointGcCode() {
        return wayPointItem.wayPointGcCode;
    }
}
