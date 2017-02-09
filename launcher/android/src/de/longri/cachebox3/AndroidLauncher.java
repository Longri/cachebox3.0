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
import android.os.Bundle;
import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.SharedLibraryLoader;
import de.longri.cachebox3.locator.Locator;
import org.oscim.android.gl.AndroidGL;
import org.oscim.backend.GLAdapter;
import org.oscim.gdx.GdxAssets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.impl.LibgdxLogger;
import org.sqldroid.SQLDroidDriver;

import java.io.File;

public class AndroidLauncher extends AndroidApplication {
    private final static Logger log = LoggerFactory.getLogger(AndroidLauncher.class);

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
        log.debug("onStart()");
        super.onCreate(savedInstanceState);


        // Don't change this LogLevel
        // Cachebox use the slf4j implematation for LibGdx as Log engine.
        // so set LogLevel on CB.class if you wont (USED_LOG_LEVEL)
        this.setLogLevel(LOG_DEBUG);

        //initialize platform bitmap factory
        org.oscim.android.canvas.AndroidGraphics.init();

        //initialize platform connector
        PlatformConnector.init(new AndroidPlatformConnector(this));

        GdxAssets.init("");
        GLAdapter.init(new AndroidGL());

        AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
        config.stencil = 8;
        config.numSamples = 2;
        new SharedLibraryLoader().load("vtm-jni");
        initialize(new CacheboxMain(), config);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);

        setApplicationLogger(new Android_ApplicationLogger());

        //    ## INITIAL LOGGER ##                ################################
        File filesDir = getExternalFilesDir(null);
        FileHandle loggerConfig = Gdx.files.absolute(filesDir.getAbsolutePath())
                .child(LibgdxLogger.CONFIGURATION_FILE_XML);
        LibgdxLogger.initial(loggerConfig);

        //create private path for Log and properties file
        // => /data/user/0/de.longri.cachebox3/files
        loggerConfig.parent().mkdirs();
    }

    protected void onStart() {
        log.debug("onStart()");
        super.onStart();

        if (android.os.Build.VERSION.SDK_INT >= 23) {
            AndroidPermissionCheck.checkNeededPermissions(this);
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // TODO Auto-generated method stub

            }
        });
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

    private final SensorEventListener mListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            try {
                mCompassValues = event.values;
                compassHeading = mCompassValues[0];
                Locator.setHeading(compassHeading, Locator.CompassType.Magnetic);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };

}
