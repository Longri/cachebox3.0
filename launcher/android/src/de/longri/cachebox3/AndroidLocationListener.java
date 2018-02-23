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
package de.longri.cachebox3;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationProvider;
import android.os.Bundle;
import de.longri.cachebox3.events.location.GpsEventHelper;
import de.longri.cachebox3.events.location.GpsState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;

/**
 * Created by Longri on 25.07.16.
 */
public class AndroidLocationListener implements LocationListener, SensorEventListener {

    private static Logger log = LoggerFactory.getLogger(AndroidLocationListener.class);

    // Compass
    SensorManager mSensorManager;
    private Sensor accelerometer;
    private Sensor magnetometer;

    private final GpsEventHelper eventHelper = new GpsEventHelper();


    AndroidLocationListener(Context context) {
        mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        registerSensor();
    }


    @Override
    public void onLocationChanged(Location location) {
//        log.debug("onLocationChanged: {}", location);
        boolean isGpsProvided = false;
        if (location.getProvider().toLowerCase(new Locale("en")).contains("gps"))
            isGpsProvided = true;

        if (isGpsProvided) {
            eventHelper.newGpsPos(location.getLatitude(), location.getLongitude());
        } else {
            eventHelper.newNetworkPos(location.getLatitude(), location.getLongitude());
        }
        if (location.hasAltitude()) eventHelper.newAltitude(location.getAltitude());
        if (location.hasBearing()) eventHelper.newBearing((float) Math.toRadians(location.getBearing()), true);
        if (location.hasAccuracy()) eventHelper.newAccuracy(location.getAccuracy());
        if (location.hasSpeed()) eventHelper.newSpeed(location.getSpeed() * 3.6);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        if (provider.toLowerCase(new Locale("en")).contains("gps")) {

            switch (status) {
                case LocationProvider.OUT_OF_SERVICE:
                    eventHelper.gpsStateChanged(GpsState.OUT_OF_SERVICE);
                    break;
                case LocationProvider.AVAILABLE:
                    eventHelper.gpsStateChanged(GpsState.AVAILABLE);
                    break;
                case LocationProvider.TEMPORARILY_UNAVAILABLE:
                    eventHelper.gpsStateChanged(GpsState.TEMPORARILY_UNAVAILABLE);
                    break;

            }

        }
    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    private float[] gravity;
    private final float orientationValues[] = new float[3];
    private final float R[] = new float[9];
    private final float I[] = new float[9];

    @Override
    public void onSensorChanged(SensorEvent event) {
//        synchronized (CB.eventHelper) {
//            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
//                gravity = event.values;
//            if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
//                float[] geomagnetic = event.values;
//                if (gravity != null && geomagnetic != null) {
//                    if (SensorManager.getRotationMatrix(R, I, gravity, geomagnetic)) {
//                        SensorManager.getOrientation(R, orientationValues);
//                        CB.eventHelper.newBearing(orientationValues[0], false);
//                        CB.eventHelper.newPitch(orientationValues[1]);
//                        CB.eventHelper.newRoll(orientationValues[2]);
//                    }
//                }
//            }
//        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    public void registerSensor() {
        mSensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void unRegisterSensor() {
        mSensorManager.unregisterListener(this);
    }
}
