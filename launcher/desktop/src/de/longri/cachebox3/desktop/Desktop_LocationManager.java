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

import ch.fhnw.imvs.gpssimulator.data.GPSData;
import ch.fhnw.imvs.gpssimulator.data.GPSDataListener;
import de.longri.cachebox3.events.location.LocationEvents;
import de.longri.cachebox3.locator.manager.LocationManager;
import de.longri.cachebox3.utils.MathUtils;

/**
 * Created by Longri on 09.03.18.
 */
public class Desktop_LocationManager extends LocationManager {


    private LocationEvents locationEvents;
    private float distanceFilter = 100;
    private double lastLat, lastLon;
    private float[] distanceResult = new float[1];


    public Desktop_LocationManager(boolean background) {

    }

    @Override
    public void setDelegate(LocationEvents locationEvents) {
        this.locationEvents = locationEvents;
    }

    @Override
    public void startUpdateLocation() {
        GPSData.addChangeListener(listener);
    }

    @Override
    public void startUpdateHeading() {
        GPSData.addChangeListener(listener);
    }

    @Override
    public void stopUpdateLocation() {
        GPSData.removeChangeListener(listener);
    }

    @Override
    public void stopUpdateHeading() {
        GPSData.removeChangeListener(listener);
    }

    @Override
    public void setDistanceFilter(float distance) {
        this.distanceFilter = distance;
    }

    @Override
    public void dispose() {
        stopUpdateHeading();
        stopUpdateLocation();
        locationEvents = null;
        listener = null;
    }

    GPSDataListener listener = new GPSDataListener() {


        double lastcourse;

        @Override
        public void valueChanged() {
            if (lastcourse != GPSData.getCourse()) {
                locationEvents.newBearing((float) Math.toRadians(GPSData.getCourse()), true);
                lastcourse = GPSData.getCourse();
            } else {

                //check distance filter
                MathUtils.computeDistanceAndBearing(MathUtils.CalculationType.FAST, lastLat, lastLon, GPSData.getLatitude(), GPSData.getLongitude(), distanceResult);

                if (distanceResult[0] < distanceFilter) {
                    return;
                }

                lastLat = GPSData.getLatitude();
                lastLon = GPSData.getLongitude();

                if (locationEvents != null) locationEvents.newGpsPos(lastLat, lastLon, GPSData.getQuality());
                if (locationEvents != null)
                    locationEvents.newBearing((float) Math.toRadians(GPSData.getCourse()), true);
                if (locationEvents != null) locationEvents.newAltitude(GPSData.getAltitude());
                if (locationEvents != null) locationEvents.newSpeed(GPSData.getSpeed());
            }
        }
    };
}
