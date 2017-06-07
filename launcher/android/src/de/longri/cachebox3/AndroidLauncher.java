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
package de.longri.cachebox3;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import com.badlogic.gdx.backends.android.AndroidFragmentApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sqldroid.SQLDroidDriver;

public class AndroidLauncher extends FragmentActivity implements AndroidFragmentApplication.Callbacks {
    private final static Logger log = LoggerFactory.getLogger(AndroidLauncher.class);
    public static AndroidLauncher androidLauncher;

    static {
        try {
            log.debug("initial SQLDroidDriver");
            java.sql.DriverManager.registerDriver(new SQLDroidDriver());
        } catch (Exception e) {
            log.error("With initial SQLDroidDriver", e);
        }

    }


    // Compass
    private SensorManager mSensorManager;
    private Sensor accelerometer;
    private Sensor magnetometer;

    private AndroidLauncherfragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        fragment = new AndroidLauncherfragment();
        FragmentTransaction trans = getSupportFragmentManager().beginTransaction();
        trans.replace(android.R.id.content, fragment);
        trans.commit();

        androidLauncher = this;
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
    }

    protected void onStart() {
        super.onStart();
        log.debug("onStart()");

        if (android.os.Build.VERSION.SDK_INT >= 23) {
            AndroidPermissionCheck.checkNeededPermissions(this);
        }
    }


    @Override
    protected void onResume() {
        log.debug("onResume()");
        super.onResume();
        if (mSensorManager != null) {
            mSensorManager.registerListener(mListener, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
            mSensorManager.registerListener(mListener, magnetometer, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    protected void onStop() {
        log.debug("onStop()");
        super.onStop();

        if (mSensorManager != null)
            mSensorManager.unregisterListener(mListener);
    }


    private final SensorEventListener mListener = new SensorEventListener() {
        float[] gravity;
        float[] geomagnetic;
        final float orientationValues[] = new float[3];
        final float R[] = new float[9];
        final float I[] = new float[9];
        final float minChange = 1f;
        private float orientation;
        private float lastOrientation;

        @Override
        public void onSensorChanged(SensorEvent event) {
            synchronized (CB.eventHelper) {
                if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
                    gravity = event.values;
                if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD){
                    geomagnetic = event.values;
                    if (gravity != null && geomagnetic != null) {
                        if (SensorManager.getRotationMatrix(R, I, gravity, geomagnetic)) {
                            SensorManager.getOrientation(R, orientationValues);
                            orientation = (float) Math.toDegrees(orientationValues[0]);
                            if (Math.abs(lastOrientation - orientation) > minChange) {
                                CB.eventHelper.setMagneticCompassHeading(orientation);
                                lastOrientation = orientation;
                            }
                        }
                    }
                }
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };

    @Override
    public void exit() {
        finish();
    }

    public void show(AndroidDescriptionView descriptionView) {
        ViewGroup.LayoutParams params = new RelativeLayout.LayoutParams(200, 200);
        if (descriptionView.getParent() != null)
            removeView(descriptionView);
        fragment.getActivity().addContentView(descriptionView, params);
    }

    public void removeView(AndroidDescriptionView descriptionView) {
        fragment.removeView(descriptionView);
    }
}
