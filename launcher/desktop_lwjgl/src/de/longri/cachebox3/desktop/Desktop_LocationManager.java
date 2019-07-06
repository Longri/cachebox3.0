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
import com.badlogic.gdx.utils.Array;
import de.longri.cachebox3.callbacks.GenericHandleCallBack;
import de.longri.cachebox3.events.location.LocationEvents;
import de.longri.cachebox3.locator.LatLong;
import de.longri.cachebox3.locator.Region;
import de.longri.cachebox3.locator.manager.LocationManager;
import de.longri.cachebox3.utils.MathUtils;

import static org.oscim.map.Map.POSITION_EVENT;

/**
 * Created by Longri on 09.03.18.
 */
public class Desktop_LocationManager extends LocationManager {


    private LocationEvents locationEvents;
    private float distanceFilter = 100;
    private double lastLat, lastLon;
    private final Array<Region> regions = new Array<>();
    private final Array<Region> insideRegions = new Array<>();
    private final Array<Region> clearList = new Array<>();
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

    @Override
    public void startMonitoring(Region region) {
        GPSData.addChangeListener(listener);
        if (regions.contains(region, false) || insideRegions.contains(region, false))
            return;
        regions.add(region);
    }

    @Override
    public void stopMonitoring(Region region) {
        regions.removeValue(region, false);
        insideRegions.removeValue(region, false);
    }

    GPSDataListener listener = new GPSDataListener() {


        double lastcourse;
        double lastTilt;

        @Override
        public void valueChanged() {
            {//check region handler
                LatLong latLong = new LatLong(GPSData.getLatitude(), GPSData.getLongitude());
                clearList.clear();
                for (int i = 0; i < regions.size; i++) {
                    Region region = regions.get(i);
                    if (region.contains(latLong)) {
                        locationEvents.didEnterRegion(region);
                        clearList.add(region);
                    }
                }
                for (int i = 0; i < clearList.size; i++) {
                    Region region = clearList.get(i);
                    regions.removeValue(region, true);
                    insideRegions.add(region);
                }
                clearList.clear();
                for (int i = 0; i < insideRegions.size; i++) {
                    Region region = insideRegions.get(i);
                    if (!region.contains(latLong)) {
                        locationEvents.didExitRegion(region);
                        clearList.add(region);
                    }
                }
                for (int i = 0; i < clearList.size; i++) {
                    Region region = clearList.get(i);
                    insideRegions.removeValue(region, true);
                    regions.add(region);
                }
            }


            if (lastcourse != GPSData.getCourse()) {
                locationEvents.newBearing((float) Math.toRadians(GPSData.getCourse()), true);
                locationEvents.newBearing((float) Math.toRadians(GPSData.getCourse()), false);
                lastcourse = GPSData.getCourse();
            } else if(lastTilt!=GPSData.getTilt()){
                locationEvents.newTilt(GPSData.getTilt());
                lastTilt= GPSData.getTilt();
            }else {

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

    @Override
    public void setCanCalibrateCallBack(GenericHandleCallBack<Boolean> canCalibrateCallBack) {
        // not needed
    }
}
