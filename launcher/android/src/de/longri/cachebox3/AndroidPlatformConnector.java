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


import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.files.FileHandle;
import de.longri.cachebox3.logging.Logger;
import de.longri.cachebox3.logging.LoggerFactory;
import org.oscim.backend.canvas.Bitmap;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Longri on 17.07.16.
 */
public class AndroidPlatformConnector extends PlatformConnector {
    final static Logger log = LoggerFactory.getLogger(AndroidPlatformConnector.class);
    private final AndroidApplication application;

    static {
        CB.platform = CB.Platform.ANDROID;
    }


    public AndroidPlatformConnector(AndroidApplication app) {
        this.application = app;
    }


    @Override
    protected boolean _isTorchAvailable() {
        return false;
    }

    @Override
    protected boolean _isTorchOn() {
        return false;
    }

    @Override
    protected void _switchTorch() {
//TODO implement tourch
    }

    @Override
    public Bitmap getRealScaledSVG(InputStream inputStream, PlatformConnector.SvgScaleType scaleType, float scaleValue) throws IOException {
        return new AndroidRealSvgBitmap(inputStream, scaleType, scaleValue);
    }


    LocationManager locationManager;
    AndroidLocationListener locationListener;

    @Override
    public void initialLocationReciver() {


        if (locationManager != null) {
            return;
        }

        locationListener = new AndroidLocationListener();

        // GPS
        // Get the location manager
        locationManager = (LocationManager) this.application.getSystemService(Context.LOCATION_SERVICE);

        final int updateTime = 500; //500ms

        //TODO get gps updateTime from settings
//            int updateTime = Config.gpsUpdateTime.getValue();
//
//            Config.gpsUpdateTime.addChangedEventListener(new IChanged() {
//
//                @Override
//                public void isChanged() {
//                    int updateTime = Config.gpsUpdateTime.getValue();
//                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, updateTime, 1, this);
//                }
//            });

        application.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, updateTime, 1, locationListener);
                    if (ActivityCompat.checkSelfPermission(AndroidPlatformConnector.this.application, Manifest.permission.ACCESS_FINE_LOCATION)
                            != PackageManager.PERMISSION_GRANTED &&
                            ActivityCompat.checkSelfPermission(AndroidPlatformConnector.this.application, Manifest.permission.ACCESS_COARSE_LOCATION)
                                    != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 10000, 300, locationListener);

                    locationManager.addNmeaListener(locationListener);
                    locationManager.addGpsStatusListener(locationListener);
                } catch (Exception e) {
                    log.error("main.initialLocationManager()", e);
                    e.printStackTrace();
                }
            }
        });

    }

    @Override
    public FileHandle _getSandBoxFileHandle(String fileName) {
        File dir = this.application.getFilesDir();
        File file = new File(dir, fileName);
        return Gdx.files.absolute(file.getAbsolutePath());
    }

    @Override
    protected String _getWorkPath() {
        // Internal SD Card
        return Environment.getExternalStorageDirectory().getAbsolutePath() + "/Cachebox3";
    }

}
