/*
 * Copyright (C) 2017 team-cachebox.de
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

import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.badlogic.gdx.backends.android.AndroidFragmentApplication;
import com.badlogic.gdx.utils.SharedLibraryLoader;
import org.oscim.android.gl.AndroidGL;
import org.oscim.backend.GLAdapter;
import org.oscim.gdx.GdxAssets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Longri on 26.04.2017.
 */
public class AndroidLauncherfragment extends AndroidFragmentApplication implements ActivityCompat.OnRequestPermissionsResultCallback {

    final static Logger log = LoggerFactory.getLogger(AndroidLauncherfragment.class);

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.setLogLevel(100);

        //initialize platform bitmap factory
        org.oscim.android.canvas.AndroidGraphics.init();

        GdxAssets.init("");
        GLAdapter.init(new AndroidGL());

        AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
        config.stencil = 8;
        config.numSamples = 2;
        new SharedLibraryLoader().load("vtm-jni");

        View view = initializeForView(new CacheboxMain(), config);

        //initialize platform connector
        PlatformConnector.init(new AndroidPlatformConnector(this));

        return view;
    }

    public void show(AndroidDescriptionView descriptionView) {
        ViewGroup.LayoutParams params = new RelativeLayout.LayoutParams(Gdx.graphics.getWidth(), Gdx.graphics.getHeight() / 2);
        descriptionView.setX(0);
        descriptionView.setY(Gdx.graphics.getHeight() / 4);
        this.getApplicationWindow().addContentView(descriptionView, params);
        log.debug("add description view to application window");
    }

    public void removeView(final AndroidDescriptionView descriptionView) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ViewGroup parent = ((ViewGroup) descriptionView.getParent());
                if (parent != null) {
                    log.debug("remove description view to application window");
                    parent.removeView(descriptionView);
                } else {
                    log.error("description view has no parent, so can't remove from application window");
                }
            }
        });
    }


    //#####################################################################
    // override Log methods to pipe the log output from Locat to Console
    //#####################################################################

    @Override
    public void debug (String tag, String message) {
        if (logLevel >= LOG_DEBUG) {
            //Log.d(tag, message);
            System.out.println(message);
        }
    }

    @Override
    public void debug (String tag, String message, Throwable exception) {
        if (logLevel >= LOG_DEBUG) {
            //Log.d(tag, message, exception);
            System.out.println(message);
        }
    }

    @Override
    public void log (String tag, String message) {
        if (logLevel >= LOG_INFO){
            //Log.i(tag, message);
            System.out.println(message);
        }

    }

    @Override
    public void log (String tag, String message, Throwable exception) {
        if (logLevel >= LOG_INFO) {
            //Log.i(tag, message, exception);
            System.out.println(message);
        }

    }

    @Override
    public void error (String tag, String message) {
        if (logLevel >= LOG_ERROR) {
            //Log.e(tag, message);
            System.out.println(message);
        }
    }

    @Override
    public void error (String tag, String message, Throwable exception) {
        if (logLevel >= LOG_ERROR){
            //Log.e(tag, message, exception);
            System.out.println(message);
        }
    }


}
