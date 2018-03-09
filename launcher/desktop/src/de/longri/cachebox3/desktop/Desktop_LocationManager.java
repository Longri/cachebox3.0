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
package de.longri.cachebox3.desktop;

import ch.fhnw.imvs.gpssimulator.SimulatorMain;
import de.longri.cachebox3.events.location.LocationEvents;
import de.longri.cachebox3.locator.manager.LocationManager;

/**
 * Created by Longri on 09.03.18.
 */
public class Desktop_LocationManager extends LocationManager {


    private LocationEvents locationEvents;


    public Desktop_LocationManager(boolean background) {

    }

    @Override
    public void setDelegate(LocationEvents locationEvents) {
        this.locationEvents = locationEvents;
    }

    @Override
    public void startUpdateLocation() {
        SimulatorMain.locationEventHandler.add(locationEvents);
    }

    @Override
    public void startUpdateHeading() {
        SimulatorMain.headingEventHandler.add(locationEvents);
    }

    @Override
    public void stopUpdateLocation() {
        SimulatorMain.locationEventHandler.removeValue(locationEvents, true);
    }

    @Override
    public void stopUpdateHeading() {
        SimulatorMain.headingEventHandler.removeValue(locationEvents, true);
    }
}
