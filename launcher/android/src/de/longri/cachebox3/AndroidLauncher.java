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
import org.oscim.backend.CanvasAdapter;
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

        //initialize platform bitmap factory
        org.oscim.android.canvas.AndroidGraphics.init();

        //initialize platform connector
        PlatformConnector.init(new AndroidPlatformConnector(this));

        DisplayMetrics metrics = getResources().getDisplayMetrics();
        CanvasAdapter.dpi = (int) Math.max(metrics.xdpi, metrics.ydpi);

        AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
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
