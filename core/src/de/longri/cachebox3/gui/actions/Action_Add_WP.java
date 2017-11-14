/*
 * Copyright (C) 2016 - 2017 team-cachebox.de
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

import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.callbacks.GenericCallBack;
import de.longri.cachebox3.events.EventHandler;
import de.longri.cachebox3.events.SelectedWayPointChangedEvent;
import de.longri.cachebox3.gui.activities.EditWaypoint;
import de.longri.cachebox3.gui.menu.MenuID;
import de.longri.cachebox3.gui.views.MapView;
import de.longri.cachebox3.gui.views.WaypointView;
import de.longri.cachebox3.locator.Coordinate;
import de.longri.cachebox3.sqlite.Database;
import de.longri.cachebox3.sqlite.dao.DaoFactory;
import de.longri.cachebox3.types.AbstractWaypoint;
import de.longri.cachebox3.types.CacheTypes;
import de.longri.cachebox3.types.MutableWaypoint;

/**
 * Created by Longri on 14.09.2016.
 */
public class Action_Add_WP extends AbstractAction {
    public Action_Add_WP() {
        super(IMPLEMENTED, "addWP", MenuID.AID_ADD_WP);
    }

    @Override
    public Drawable getIcon() {
        return CB.getSkin().getMenuIcon.addWp;
    }

    @Override
    public void execute() {
        if (CB.viewmanager.getActView() instanceof WaypointView) {
            // if wayPointView visible, create new waypoint with waypointViewFunction
            WaypointView wpv = (WaypointView) CB.viewmanager.getActView();
            wpv.addWp();
            return;
        }

        Coordinate coord = null;
        if (CB.viewmanager.getActView() instanceof MapView) {
            // if map view visible, create new waypoint at center of map
            MapView mv = (MapView) CB.viewmanager.getActView();
            coord = mv.getMapCenter();
        }

        String newGcCode = "";
        try {
            newGcCode = Database.createFreeGcCode(EventHandler.getSelectedCache().getGcCode().toString());
        } catch (Exception e) {
            return;
        }
        if (coord == null) {
            coord = EventHandler.getSelectedCoord();
            if (coord == null)
                coord = EventHandler.getMyPosition();
            if ((coord == null) || (!coord.isValid()))
                coord = EventHandler.getSelectedCache();
        }

        MutableWaypoint newWP = new MutableWaypoint(newGcCode, CacheTypes.ReferencePoint
                , coord.getLatitude(), coord.getLongitude(), EventHandler.getSelectedCache().getId(), "", newGcCode);


        EditWaypoint editWaypoint = new EditWaypoint(newWP, true, false, new GenericCallBack<AbstractWaypoint>() {
            @Override
            public void callBack(AbstractWaypoint value) {
                if (value != null) {
                    EventHandler.fire(new SelectedWayPointChangedEvent(value));
                    if (value.isStart()) {
                        //It must be ensured here that this waypoint is the only one of these Cache,
                        //which is defined as starting point !!!
                        DaoFactory.WAYPOINT_DAO.resetStartWaypoint(EventHandler.getSelectedCache(), value);
                    }
                    DaoFactory.WAYPOINT_DAO.writeToDatabase(Database.Data, value);

                    // add WP to Cache
                    EventHandler.getSelectedCache().getWaypoints().add(value);
                }
            }
        });
        editWaypoint.show();
    }
}
