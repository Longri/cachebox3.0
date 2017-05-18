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
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.text.InputType;
import android.view.Gravity;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.backends.android.AndroidInput;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.sql.SQLiteGdxDatabaseFactory;
import com.badlogic.gdx.sqlite.android.AndroidDatabaseManager;
import de.longri.cachebox3.callbacks.GenericCallBack;
import de.longri.cachebox3.gui.stages.ViewManager;
import de.longri.cachebox3.translation.Translation;
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
    private final Handler handle;
    private final Context context;

    public AndroidPlatformConnector(AndroidLauncherfragment app) {
        this.application = app;
        this.context = app.getContext();
        platformConnector = this;
        SQLiteGdxDatabaseFactory.setDatabaseManager(new AndroidDatabaseManager());
        this.handle = new Handler();
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

            }
        });
    }

    @Override
    protected void descriptionViewToNull() {
        descriptionView = null;
    }

    @Override
    public void openUrlExtern(String link) {
        try {
            link = link.trim();
            if (link.startsWith("www.")) {
                link = "http://" + link;
            }
            Uri uri = Uri.parse(link);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addCategory(Intent.CATEGORY_BROWSABLE);
            intent.setDataAndType(uri, "text/html");
            if (intent.resolveActivity(application.getContext().getPackageManager()) != null) {
                log.info("Start activity for " + uri.toString());
                application.getActivity().startActivity(intent);
            } else {
                log.error("Activity for " + link + " not installed.");
                CB.viewmanager.toast(Translation.Get("Cann_not_open_cache_browser") + " (" + link + ")");
            }
        } catch (Exception exc) {
            log.error(Translation.Get("Cann_not_open_cache_browser") + " (" + link + ")", exc);
            CB.viewmanager.toast(Translation.Get("Cann_not_open_cache_browser") + " (" + link + ")");
        }
    }

    @Override
    public void _getMultilineTextInput(final Input.TextInputListener listener, final String title, final String text,
                                       final String hint) {
        this.handle.post(new Runnable() {
            public void run() {
                AlertDialog.Builder alert = new AlertDialog.Builder(AndroidPlatformConnector.this.context);

                //set custom title
                TextView myMsg = new TextView(context);
                myMsg.setText(title);
                myMsg.setGravity(Gravity.CENTER_HORIZONTAL);
                myMsg.setTextSize(16);
                alert.setCustomTitle(myMsg);


                final EditText input = new EditText(AndroidPlatformConnector.this.context);
                input.setHint(hint);
                input.setText(text);
                input.setInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE);
                input.setSingleLine(false);
                input.setLines(5);
                input.setMaxLines(8);
                input.setGravity(Gravity.LEFT | Gravity.TOP);
                alert.setView(input);
                alert.setPositiveButton(AndroidPlatformConnector.this.context.getString(17039370), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        Gdx.app.postRunnable(new Runnable() {
                            public void run() {
                                listener.input(input.getText().toString());
                            }
                        });
                    }
                });
                alert.setNegativeButton(AndroidPlatformConnector.this.context.getString(17039360), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        Gdx.app.postRunnable(new Runnable() {
                            public void run() {
                                listener.canceled();
                            }
                        });
                    }
                });
//                alert.setOnCancelListener(new DialogInterface.OnCancelListener() {
//                    public void onCancel(DialogInterface arg0) {
//                        Gdx.app.postRunnable(new Runnable() {
//                            public void run() {
//                                listener.canceled();
//                            }
//                        });
//                    }
//                });
                alert.show();
            }
        });
    }
}
