/*
 * Copyright (C) 2016 - 2018 team-cachebox.de
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
package de.longri.cachebox3.locator.manager;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import com.badlogic.gdx.utils.Array;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.events.location.LocationEvents;
import de.longri.cachebox3.gui.stages.StageManager;
import de.longri.cachebox3.locator.LatLong;
import de.longri.cachebox3.locator.Region;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;

/**
 * Created by Longri on 25.07.16.
 */
public class AndroidLocationListener implements LocationListener {

    private final static Logger log = LoggerFactory.getLogger(AndroidLocationListener.class);
    private final static long MIN_UPDATE_TIME = 1000;

    private final Array<Region> regions = new Array<>();
    private final Array<Region> insideRegions = new Array<>();
    private final Array<Region> clearList = new Array<>();
    private LocationEvents handler;

    private long lastLocationUpdate;

    @Override
    public void onLocationChanged(Location location) {

        long now = System.currentTimeMillis();
        if (lastLocationUpdate + MIN_UPDATE_TIME > now) return;
        lastLocationUpdate = now;


        if (CB.sensoerIO.isPlay()) return;

        if (this.handler == null) return;

        try {//check region handler
            LatLong latLong = new LatLong(location.getLatitude(), location.getLongitude());
            clearList.clear();
            for (int i = 0; i < regions.size; i++) {
                Region region = regions.get(i);
                if (region.contains(latLong)) {
                    handler.didEnterRegion(region);
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
                    handler.didExitRegion(region);
                    clearList.add(region);
                }
            }
            for (int i = 0; i < clearList.size; i++) {
                Region region = clearList.get(i);
                insideRegions.removeValue(region, true);
                regions.add(region);
            }


            boolean isGpsProvided = false;
            if (location.getProvider().toLowerCase(new Locale("en")).contains("gps"))
                isGpsProvided = true;

            if (isGpsProvided) {
                float acc = location.hasAccuracy() ? location.getAccuracy() : 0;
                if (acc >= 0) {
                    this.handler.newGpsPos(location.getLatitude(), location.getLongitude(), acc);
                    if (location.hasAltitude()) this.handler.newAltitude(location.getAltitude());
                    if (location.hasBearing())
                        this.handler.newBearing((float) Math.toRadians(location.getBearing()), true);
                    if (location.hasSpeed()) this.handler.newSpeed(location.getSpeed() * 3.6);
                }
            } else {
                float acc = location.hasAccuracy() ? location.getAccuracy() : 0;
                if (acc >= 0)
                    this.handler.newNetworkPos(location.getLatitude(), location.getLongitude(), acc);
            }
        } catch (Exception e) {
            CB.stageManager.indicateException(CB.EXCEPTION_COLOR_LOCATION);
            log.error("onLocationChanged", e);
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onProviderDisabled(String provider) {
    }


    void setDelegate(LocationEvents handler) {
        this.handler = handler;
    }

    void startMonitoring(Region region) {
        if (regions.contains(region, false) || insideRegions.contains(region, false))
            return;
        regions.add(region);
    }

    void stopMonitoring(Region region) {
        regions.removeValue(region, false);
        insideRegions.removeValue(region, false);
    }
}
