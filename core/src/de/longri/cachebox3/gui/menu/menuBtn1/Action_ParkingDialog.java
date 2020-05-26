/*
 * Copyright (C) 2014-2020 team-cachebox.de
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
package de.longri.cachebox3.gui.menu.menuBtn1;


import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.kotcrab.vis.ui.VisUI;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.events.CacheListChangedEvent;
import de.longri.cachebox3.events.EventHandler;
import de.longri.cachebox3.events.SelectedCacheChangedEvent;
import de.longri.cachebox3.gui.menu.MenuID;
import de.longri.cachebox3.gui.stages.AbstractAction;
import de.longri.cachebox3.gui.widgets.menu.Menu;
import de.longri.cachebox3.settings.Config;
import de.longri.cachebox3.sqlite.Database;
import de.longri.cachebox3.types.AbstractCache;
import de.longri.cachebox3.types.CacheTypes;
import de.longri.cachebox3.types.MutableCache;
import de.longri.cachebox3.utils.NamedRunnable;

import static de.longri.cachebox3.events.EventHandler.fire;

/**
 * Created by Longri on 14.09.2016.
 */
public class Action_ParkingDialog extends AbstractAction {
    private final static String myParkingGCCode = "CBPark";
    public Action_ParkingDialog() {
        super("MyParking", MenuID.AID_PARKING);
    }

    @Override
    public void execute() {
        boolean cbParkExists = (Database.Data.cacheList.getCacheByGcCode(myParkingGCCode) != null);
        Menu parkingMenu = new Menu("MyParking");

        parkingMenu.addMenuItem("My_Parking_Area_select", VisUI.getSkin().getDrawable("MyParking48"), () -> {
            CB.postOnGlThread(new NamedRunnable("Select my parking") {
                @Override
                public void run() {
                    AbstractCache myParking = Database.Data.cacheList.getCacheByGcCode(myParkingGCCode);
                    if (myParking != null) {
                        fire(new SelectedCacheChangedEvent(myParking)); // selected Cache not shown selected (yellow) in cachelist
                        fire(new CacheListChangedEvent()); // now its yellow
                    }
                }
            });
        }).setEnabled(cbParkExists);

        parkingMenu.addMenuItem("My_Parking_Area_Add", VisUI.getSkin().getDrawable("MyParkingAdd48"), () -> {
            Config.ParkingLatitude.setValue(EventHandler.getMyPosition().getLatitude());
            Config.ParkingLongitude.setValue(EventHandler.getMyPosition().getLongitude());
            Config.AcceptChanges();
            AbstractCache myParking = Database.Data.cacheList.getCacheByGcCode(myParkingGCCode);
            if (myParking == null) {
                myParking = new MutableCache(Database.Data,
                        Config.ParkingLatitude.getValue(),
                        Config.ParkingLongitude.getValue(),
                        "My Parking area",
                        CacheTypes.MyParking,
                        myParkingGCCode);
                Database.Data.cacheList.add(myParking);
            }
            else
                myParking.setLatLon(Config.ParkingLatitude.getValue(), Config.ParkingLongitude.getValue());
            fire(new CacheListChangedEvent());
        });

        parkingMenu.addMenuItem("My_Parking_Area_Del", VisUI.getSkin().getDrawable("MyParkingRemove48"), () -> {
            Config.ParkingLatitude.setValue(0.0);
            Config.ParkingLongitude.setValue(0.0);
            Config.AcceptChanges();
            AbstractCache myParking = Database.Data.cacheList.getCacheByGcCode(myParkingGCCode);
            if (myParking != null) {
                Database.Data.cacheList.removeValue(myParking, true);
                fire(new CacheListChangedEvent());
            }
        }).setEnabled(cbParkExists);

        parkingMenu.show();
    }

    @Override
    public Drawable getIcon() {
        return VisUI.getSkin().getDrawable("MyParking48");
    }
}
