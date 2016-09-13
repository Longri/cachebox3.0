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

import android.os.Bundle;
import android.util.DisplayMetrics;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.badlogic.gdx.utils.SharedLibraryLoader;
import org.oscim.android.gl.AndroidGL;
import org.oscim.backend.CanvasAdapter;
import org.oscim.backend.GLAdapter;
import org.oscim.gdx.GdxAssets;
import org.sqldroid.SQLDroidDriver;

public class AndroidLauncher extends AndroidApplication {

    static {
        try {
            java.sql.DriverManager.registerDriver(new SQLDroidDriver());
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private final AndroidLocationListener locationListener = new AndroidLocationListener();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Don't change this LogLevel
        // Cachebox use the slf4j implematation for LibGdx as Log engine.
        // so set LogLevel on CB.class if you wont (USED_LOG_LEVEL)
        this.setLogLevel(LOG_DEBUG);

        //initialize platform bitmap factory
        org.oscim.android.canvas.AndroidGraphics.init();

        //initialize platform connector
        PlatformConnector.init(new AndroidPlatformConnector(this));

        DisplayMetrics metrics = getResources().getDisplayMetrics();
        CanvasAdapter.dpi = (int) Math.max(metrics.xdpi, metrics.ydpi);

        GdxAssets.init("");
        GLAdapter.init(new AndroidGL());

        AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
        config.stencil = 8;
        config.numSamples = 2;
        new SharedLibraryLoader().load("vtm-jni");
        initialize(new CacheboxMain(), config);
    }

    protected void onStart() {
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
}
