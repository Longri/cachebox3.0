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

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.events.location.LocationEvents;

/**
 * Created by Longri on 06.03.18.
 */
public class AndroidSensorListener implements SensorEventListener {

    // Compass
    SensorManager mSensorManager;
    private Sensor accelerometer;
    private Sensor magnetometer;


    private float[] gravity;
    private final float orientationValues[] = new float[3];
    private final float R[] = new float[9];
    private final float I[] = new float[9];
    private LocationEvents handler;


    public AndroidSensorListener(Context context) {
        mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        if (CB.sensoerIO.isPlay()) return;
        synchronized (this.handler) {
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
                gravity = event.values;
            if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                float[] geomagnetic = event.values;
                if (gravity != null && geomagnetic != null) {
                    if (SensorManager.getRotationMatrix(R, I, gravity, geomagnetic)) {
                        SensorManager.getOrientation(R, orientationValues);
                        this.handler.newBearing(orientationValues[0], false);
                        this.handler.newPitch(orientationValues[1]);
                        this.handler.newRoll(orientationValues[2]);
                    }
                }
            }
        }
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

    public void setDelegate(LocationEvents handler) {
        this.handler = handler;
    }
}
