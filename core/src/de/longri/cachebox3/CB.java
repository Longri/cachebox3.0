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

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.kotcrab.vis.ui.VisUI;
import de.longri.cachebox3.gui.stages.StageManager;
import de.longri.cachebox3.gui.stages.ViewManager;
import de.longri.cachebox3.utils.ScaledSizes;
import org.slf4j.LoggerFactory;
import org.slf4j.impl.LibgdxLoggerFactory;

/**
 * Static class
 * Created by Longri on 20.07.2016.
 */
public class CB {

    public static final int CurrentRevision = 20160806;
    public static final String CurrentVersion = "0.1.";
    public static final String VersionPrefix = "Test";


    //LogLevels

    public static final String LOG_LEVEL_INFO = "info";
    public static final String LOG_LEVEL_DEBUG = "debug";
    public static final String LOG_LEVEL_WARN = "warn";
    public static final String LOG_LEVEL_ERROR = "error";
    public static final String LOG_LEVEL_TRACE = "trace";

    public static final String USED_LOG_LEVEL = LOG_LEVEL_DEBUG;
    public static final float WINDOW_FADE_TIME = 0.3f;

    static {

        LibgdxLoggerFactory.EXCLUDE_LIST.add("Database.CacheBox");
        LibgdxLoggerFactory.EXCLUDE_LIST.add("Database.Settings");
        LibgdxLoggerFactory.EXCLUDE_LIST.add("de.longri.cachebox3.settings.Config");
        LibgdxLoggerFactory.EXCLUDE_LIST.add("com.badlogic.gdx.sqlite.desktop.DesktopDatabase");
        LibgdxLoggerFactory.EXCLUDE_LIST.add(StageManager.class.getName());
        //   LibgdxLoggerFactory.EXCLUDE_LIST.add("com.badlogic.gdx.scenes.scene2d.ui.SvgSkin");


        LibgdxLoggerFactory.INCLUDE_LIST.add(ViewManager.class.getName());

        ((LibgdxLoggerFactory) LoggerFactory.getILoggerFactory()).reset();
    }


    final static float PPI_DEFAULT = 163;
    private static float globalScale = 1;
    public static ViewManager viewmanager;


    /**
     * WorkPath is a String to the used work path.<br>
     * This Path is a absolute path.<br>
     * <br>
     * On iOS this is the path to the "SandBox". <br>
     * On Android this can a path to internal SD "/Cachebox3" <br>
     * or to the "SandBox" on the external SD
     */
    public static String WorkPath;
    private static Skin actSkin;
    public static Color backgroundColor = new Color(0, 1, 0, 1);
    public static ScaledSizes scaledSizes;

    private CB() {
    }


    private static boolean isTestVersionCheked = false;
    private static boolean isTestVersion = false;

    public static boolean isTestVersion() {
        if (isTestVersionCheked)
            return isTestVersion;

        isTestVersion = VersionPrefix.toLowerCase().contains("test");
        isTestVersionCheked = true;
        return isTestVersion;
    }

    public static void setActSkin(Skin skin) {
        if (actSkin != null) {
            VisUI.dispose();
        }
        actSkin = skin;
        VisUI.load(actSkin);
    }

    public static Color getColor(String name) {
        return actSkin.getColor(name);
    }

    public static Skin getSkin() {
        return actSkin;
    }


    private static float scalefactor = 0;

    public static float getScaledFloat(float i) {
        if (scalefactor == 0)
            calcScaleFactor();
        return (i * scalefactor);
    }

    public static int getScaledInt(int i) {
        if (scalefactor == 0)
            calcScaleFactor();
        return (int) (i * scalefactor);
    }

    private static void calcScaleFactor() {
        scalefactor = (Gdx.graphics.getPpiX() / PPI_DEFAULT) * globalScale;
    }

    public static void setGlobalScale(float scale) {
        globalScale = scale;
    }

    public float getGlobalScaleFactor() {
        return globalScale;
    }

    public static float getScalefactor() {
        if (scalefactor == 0)
            calcScaleFactor();
        return scalefactor;
    }

    public static boolean isLogLevel(String logLevel) {

        if (logLevelToInt(USED_LOG_LEVEL) >= logLevelToInt(logLevel)) return true;
        return false;
    }

    private static int logLevelToInt(String logLevel) {
        if (LOG_LEVEL_TRACE.equals(logLevel)) return 10;
        if (LOG_LEVEL_ERROR.equals(logLevel)) return 8;
        if (LOG_LEVEL_WARN.equals(logLevel)) return 6;
        if (LOG_LEVEL_DEBUG.equals(logLevel)) return 4;
        if (LOG_LEVEL_INFO.equals(logLevel)) return 2;
        return 0;
    }

    public static Sprite getSprite(String name) {
        return actSkin != null ? actSkin.getSprite(name) : null;
    }

    public static void callQuit() {
        //TODO save last selected cache
//        if (GlobalCore.isSetSelectedCache()) {
//            // speichere selektierten Cache, da nicht alles über die SelectedCacheEventList läuft
//            Config.LastSelectedCache.setValue(GlobalCore.getSelectedCache().getGcCode());
//            Config.AcceptChanges();
//            Log.debug(log, "LastSelectedCache = " + GlobalCore.getSelectedCache().getGcCode());
//        }

        Gdx.app.exit();
    }

    public enum Platform {
        ANDROID, IOS, DESKTOP
    }

    public static Platform platform;

    public static void requestRendering() {
        Gdx.graphics.requestRendering();
    }
}
