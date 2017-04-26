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


import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.sql.SQLiteGdxDatabaseFactory;
import com.badlogic.gdx.sqlite.android.AndroidDatabaseManager;
import de.longri.cachebox3.callbacks.GenericCallBack;
import org.oscim.backend.canvas.Bitmap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Longri on 17.07.16.
 */
public class AndroidPlatformConnector extends PlatformConnector {
    final static Logger log = LoggerFactory.getLogger(AndroidPlatformConnector.class);
    private static final int REQUEST_CODE_GET_API_KEY = 987;
    private final AndroidLauncherfragment application;
    public static AndroidPlatformConnector platformConnector;

    public AndroidPlatformConnector(AndroidLauncherfragment app) {
        this.application = app;
        platformConnector = this;
        SQLiteGdxDatabaseFactory.setDatabaseManager(new AndroidDatabaseManager());
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
    public Bitmap getRealScaledSVG(String name, InputStream inputStream, PlatformConnector.SvgScaleType scaleType, float scaleValue) throws IOException {

        AndroidRealSvgBitmap bmp = new AndroidRealSvgBitmap(inputStream, scaleType, scaleValue);
        bmp.name = name;
        return bmp;
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
        locationManager = (LocationManager) this.application.getContext().getSystemService(Context.LOCATION_SERVICE);

        final int updateTime = 1000; // 1s

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
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, updateTime, 5, locationListener);
                    if (ActivityCompat.checkSelfPermission(AndroidPlatformConnector.this.application.getContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                            != PackageManager.PERMISSION_GRANTED &&
                            ActivityCompat.checkSelfPermission(AndroidPlatformConnector.this.application.getContext(), Manifest.permission.ACCESS_COARSE_LOCATION)
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
        File dir = this.application.getContext().getFilesDir();
        File file = new File(dir, fileName);
        return Gdx.files.absolute(file.getAbsolutePath());
    }

    @Override
    protected String _getWorkPath() {
        // Internal SD Card
        return Environment.getExternalStorageDirectory().getAbsolutePath() + "/Cachebox3";
    }

    public GenericCallBack<String> callBack;

    @Override
    protected void generateApiKey(GenericCallBack<String> callBack) {
        this.callBack = callBack;
        Intent intent = new Intent().setClass(application.getContext(), GenerateApiKeyWebView.class);
        if (intent.resolveActivity(application.getContext().getPackageManager()) != null) {
            application.startActivityForResult(intent, REQUEST_CODE_GET_API_KEY);
        } else {
            log.error(intent.getAction() + " not installed.");
        }

    }

    private AndroidDescriptionView descriptionView;

    @Override
    protected void getPlatformDescriptionView(final GenericCallBack<PlatformDescriptionView> callBack) {

        this.application.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                descriptionView = new AndroidDescriptionView(AndroidPlatformConnector.this.application.getContext());
                callBack.callBack(descriptionView);
                AndroidPlatformConnector.this.application.show(descriptionView);
            }
        });
    }

    @Override
    protected void descriptionViewToNull() {
        if (descriptionView != null) {
            AndroidPlatformConnector.this.application.removeView(descriptionView);
        }
        descriptionView = null;
    }
}
