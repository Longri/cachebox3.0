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
package de.longri.cachebox3.locator.manager;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import com.badlogic.gdx.utils.Array;
import de.longri.cachebox3.AndroidLauncher;
import de.longri.cachebox3.callbacks.GenericHandleCallBack;
import de.longri.cachebox3.events.location.LocationEvents;
import de.longri.cachebox3.locator.Region;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Longri on 06.03.18.
 */
public class Android_LocationManager extends LocationManager {

    private final static Logger log = LoggerFactory.getLogger(Android_LocationManager.class);
    private AndroidLocationListener locationListener;

    private LocationEvents handler;
    private android.location.LocationManager locationManager;
    private final Context context;
    private AndroidSensorListener sensorListener;
    private float distanceFilter = 0;
    private final boolean background;

    public Android_LocationManager(boolean backGround) {
        this.background = backGround;
        context = AndroidLauncher.androidLauncher.getApplicationContext();
    }

    @Override
    public void setDelegate(LocationEvents locationEvents) {
        this.handler = locationEvents;
    }

    @Override
    public void startUpdateLocation() {
        if (locationManager == null) {
            this.locationListener = new AndroidLocationListener();
            locationManager = (android.location.LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        }

        final int updateTime = 500; // 500ms

        try {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
                            != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            locationManager.requestLocationUpdates(android.location.LocationManager.GPS_PROVIDER, updateTime, distanceFilter, locationListener);
            if (!this.background)
                locationManager.requestLocationUpdates(android.location.LocationManager.NETWORK_PROVIDER, 10000, distanceFilter, locationListener);
        } catch (Exception e) {
            log.error("main.initialLocationManager()", e);
            e.printStackTrace();
        }
        locationListener.setDelegate(this.handler);
    }

    @Override
    public void startUpdateHeading() {
        if (sensorListener == null) {
            this.sensorListener = new AndroidSensorListener(context);
        }
        this.sensorListener.registerSensor();
        this.sensorListener.setDelegate(this.handler);
    }

    @Override
    public void stopUpdateLocation() {
        if (this.locationManager != null) this.locationManager.removeUpdates(locationListener);
    }

    @Override
    public void stopUpdateHeading() {
        if (this.sensorListener != null) this.sensorListener.unRegisterSensor();
    }

    @Override
    public void setDistanceFilter(float distance) {

        this.distanceFilter = distance;

        if (locationManager != null) {
            //first remove old updates
            locationManager.removeUpdates(locationListener);

            //register new updates
            locationManager.requestLocationUpdates(android.location.LocationManager.GPS_PROVIDER, 500, distance, locationListener);
        }

    }

    @Override
    public void dispose() {
        stopUpdateLocation();
        stopUpdateHeading();

        if (locationListener != null) locationListener.setDelegate(null);
        locationListener = null;

        handler = null;
        locationManager = null;

        if (sensorListener != null) sensorListener.setDelegate(null);
        sensorListener = null;
    }


    @Override
    public void stopMonitoring(Region region) {
        locationListener.stopMonitoring(region);
    }

    @Override
    public void startMonitoring(Region region) {
        locationListener.startMonitoring(region);
        startUpdateLocation();
    }

    @Override
    public void setCanCalibrateCallBack(GenericHandleCallBack<Boolean> canCalibrateCallBack) {
        // not needed
    }
}
