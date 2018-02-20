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
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.android.AndroidFragmentApplication;
import de.longri.cachebox3.utils.LowpassFilter;
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


    private AndroidLauncherfragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        fragment = new AndroidLauncherfragment();
        FragmentTransaction trans = getSupportFragmentManager().beginTransaction();
        trans.replace(android.R.id.content, fragment);
        trans.commit();

        androidLauncher = this;


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

        if (AndroidPlatformConnector.platformConnector != null &&
                AndroidPlatformConnector.platformConnector.locationListener != null &&
                AndroidPlatformConnector.platformConnector.locationListener.mSensorManager != null) {
            AndroidPlatformConnector.platformConnector.locationListener.registerSensor();
        }
    }

    @Override
    protected void onStop() {
        log.debug("onStop()");
        super.onStop();
        if (AndroidPlatformConnector.platformConnector != null &&
                AndroidPlatformConnector.platformConnector.locationListener != null &&
                AndroidPlatformConnector.platformConnector.locationListener.mSensorManager != null) {
            AndroidPlatformConnector.platformConnector.locationListener.unRegisterSensor();
        }
    }

    @Override
    protected void onDestroy() {
        AndroidPlatformConnector.platformConnector.removeLocationListener();
        super.onDestroy();
    }


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
