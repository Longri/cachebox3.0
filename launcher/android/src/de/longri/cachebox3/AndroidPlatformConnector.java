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
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.text.InputType;
import android.view.Gravity;
import android.widget.EditText;
import android.widget.TextView;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.async.AsyncTask;
import de.longri.cachebox3.callbacks.GenericCallBack;
import de.longri.cachebox3.translation.Translation;
import de.longri.cachebox3.utils.NamedRunnable;
import org.oscim.android.canvas.AndroidRealSvgBitmap;
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
    private final AndroidFlashLight flashLight;

    public AndroidPlatformConnector(AndroidLauncherfragment app) {
        this.application = app;
        this.context = app.getContext();
        platformConnector = this;
        this.handle = new Handler();
        this.flashLight = new AndroidFlashLight(this.context);
    }


    @Override
    protected boolean _isTorchAvailable() {
        return flashLight.available();
    }

    @Override
    protected boolean _isTorchOn() {
        return flashLight.isOn();
    }

    @Override
    protected void _switchTorch() {
        if (flashLight.available()) {
            if (flashLight.isOn()) {
                //switch off
                flashLight.switchOff();
            } else {
                //switch on
                flashLight.switchOn();
            }
        }
    }


    @Override
    public Bitmap getRealScaledSVG(String name, InputStream inputStream, PlatformConnector.SvgScaleType scaleType, float scaleValue) throws IOException {

        AndroidRealSvgBitmap bmp = new AndroidRealSvgBitmap(inputStream, scaleType, scaleValue);
        bmp.name = name;
        return bmp;
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
                if (descriptionView == null)
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
                CB.viewmanager.toast(Translation.get("Cann_not_open_cache_browser") + " (" + link + ")");
            }
        } catch (Exception exc) {
            log.error(Translation.get("Cann_not_open_cache_browser") + " (" + link + ")", exc);
            CB.viewmanager.toast(Translation.get("Cann_not_open_cache_browser") + " (" + link + ")");
        }
    }

    @Override
    public void _callQuit() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            application.getActivity().finishAndRemoveTask();
        } else {
            application.getActivity().finishAffinity();
        }
        Gdx.app.exit();
    }

    @Override
    protected void _postOnMainThread(NamedRunnable runnable) {
        Handler mainHandler = new Handler(context.getMainLooper());
        mainHandler.post(runnable);
    }

    @Override
    protected void _runOnBackGround(final Runnable backgroundTask) {
        CB.postAsync(new NamedRunnable("Run on Background") {
            @Override
            public void run() {
                backgroundTask.run();
            }
        });
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

//    public void removeLocationListener() {
//        application.runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    locationManager.removeUpdates(locationListener);
//                    locationManager = null;
//                    locationListener = null;
//                } catch (Exception e) {
//                    log.error("main.initialLocationManager()", e);
//                    e.printStackTrace();
//                }
//            }
//        });
//    }
}
