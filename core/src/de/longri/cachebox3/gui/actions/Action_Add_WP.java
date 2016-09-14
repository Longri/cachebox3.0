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
package de.longri.cachebox3.gui.actions;

import com.badlogic.gdx.graphics.g2d.Sprite;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.gui.menu.MenuID;

/**
 * Created by Longri on 14.09.2016.
 */
public class Action_Add_WP extends AbstractAction {
    public Action_Add_WP() {
        super("addWP", MenuID.AID_ADD_WP);
    }

    @Override
    public Sprite getIcon() {
        return CB.getSprite("add-wp");
    }

    @Override
    public void execute() {
        CB.viewmanager.toast("Add WP not implemented");

//        // wenn MapView sichtbar und im Modus Free, dann nehme Koordinaten vom Mittelpunkt der Karte
//        // ansonsten mit den aktuellen Koordinaten!
//        if (MapView.that != null && MapView.that.isVisible()) {
//            MapView.that.createWaypointAtCenter();
//            return;
//        }
//
//        if ((TabMainView.waypointView == null))
//            TabMainView.waypointView = new WaypointView(TabMainView.leftTab.getContentRec(), "WaypointView");
//        WaypointView.that.addWP();
    }
}
