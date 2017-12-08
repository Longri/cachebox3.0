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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.DisplayMetrics;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.android.AndroidFragmentApplication;
import de.longri.cachebox3.utils.RingBufferFloat;
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

        Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);
        int resId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resId > 0) {
            CB.androidStatusbarHeight = getResources().getDimensionPixelSize(resId);
        } else {
            CB.androidStatusbarHeight = bm.getHeight() / 2;
        }
    }

    protected void onStart() {
        super.onStart();
        log.debug("onStart()");

        if (android.os.Build.VERSION.SDK_INT >= 23) {
            AndroidPermissionCheck.checkNeededPermissions(this);
        }

        Gdx.input.setCatchBackKey(true);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        // permission changed, reinitialize PlatformConnector
        PlatformConnector.init(new AndroidPlatformConnector(fragment));
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

    @Override
    protected void onDestroy() {
        AndroidPlatformConnector.platformConnector.removeLocationListener();
        super.onDestroy();
    }

    private final SensorEventListener mListener = new SensorEventListener() {
        private float[] gravity;
        private float[] geomagnetic;
        private final float orientationValues[] = new float[3];
        private final float R[] = new float[9];
        private final float I[] = new float[9];
        private final float minChange = 2f;
        private final long updateTime = 150;
        private long lastUpdateTime = 0;
        private final RingBufferFloat ringBuffer = new RingBufferFloat(30);
        private float orientation;
        private float lastOrientation;

        @Override
        public void onSensorChanged(SensorEvent event) {
            synchronized (CB.eventHelper) {
                if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
                    gravity = event.values;
                long now = System.currentTimeMillis();
                if (lastUpdateTime == 0 || lastUpdateTime + updateTime < now) {
                    if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                        geomagnetic = event.values;
                        if (gravity != null && geomagnetic != null) {
                            if (SensorManager.getRotationMatrix(R, I, gravity, geomagnetic)) {
                                SensorManager.getOrientation(R, orientationValues);
                                orientation = ringBuffer.add((float) Math.toDegrees(orientationValues[0]));
                                if (Math.abs(lastOrientation - orientation) > minChange) {
                                    CB.eventHelper.setMagneticCompassHeading(orientation);
                                    log.trace("orientation: {}", orientation);
                                    lastOrientation = orientation;
                                }
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
