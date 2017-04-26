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
import com.badlogic.gdx.backends.android.AndroidFragmentApplication;
import de.longri.cachebox3.events.EventHandler;
import de.longri.cachebox3.events.OrientationChangedEvent;
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
    private Sensor mSensor;
    private float[] mCompassValues;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 6. Finally, replace the AndroidLauncher activity content with the Libgdx Fragment.
        AndroidLauncherfragment fragment = new AndroidLauncherfragment();
        FragmentTransaction trans = getSupportFragmentManager().beginTransaction();
        trans.replace(android.R.id.content, fragment);
        trans.commit();

        androidLauncher = this;
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);

    }

    protected void onStart() {
        super.onStart();
        log.debug("onStart()");


        //initialize platform connector
        PlatformConnector.init(new AndroidPlatformConnector(this));


        if (android.os.Build.VERSION.SDK_INT >= 23) {
            AndroidPermissionCheck.checkNeededPermissions(this);
        }
    }


    @Override
    protected void onResume() {
        log.debug("onResume()");
        super.onResume();
        if (mSensorManager != null)
            mSensorManager.registerListener(mListener, mSensor, SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    protected void onStop() {
        log.debug("onStop()");
        super.onStop();

        if (mSensorManager != null)
            mSensorManager.unregisterListener(mListener);

    }

    private float compassHeading = -1;
    private float lastCompassHeading;

    private final SensorEventListener mListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            try {
                mCompassValues = event.values;
                compassHeading = mCompassValues[0];

                if (Math.abs(lastCompassHeading - compassHeading) < 1) {
                    return;
                }
                lastCompassHeading = compassHeading;
                EventHandler.fire(new OrientationChangedEvent(compassHeading));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };

    @Override
    public void exit() {

    }
}
