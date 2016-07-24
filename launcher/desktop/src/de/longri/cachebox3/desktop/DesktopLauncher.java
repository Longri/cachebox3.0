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
package de.longri.cachebox3.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import de.longri.cachebox3.AwtPlatformConnector;
import de.longri.cachebox3.CacheboxMain;
import de.longri.cachebox3.PlatformConnector;
import org.oscim.awt.AwtGraphics;

public class DesktopLauncher {
    public static void main(String[] arg) {

        //initialize platform bitmap factory
        AwtGraphics.init();

        //initialize platform connector
        PlatformConnector.init(new AwtPlatformConnector());

        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        config.width = 223;
        config.height = 397;
        config.title = "Cachebox 3.0";
        new LwjglApplication(new CacheboxMain(), config);
    }
}
