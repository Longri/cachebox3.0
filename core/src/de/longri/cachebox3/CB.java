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
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.scenes.scene2d.ui.SvgSkin;
import com.kotcrab.vis.ui.VisUI;
import de.longri.cachebox3.gui.events.SelectedCacheEventList;
import de.longri.cachebox3.gui.skin.styles.ScaledSize;
import de.longri.cachebox3.gui.stages.ViewManager;
import de.longri.cachebox3.locator.Coordinate;
import de.longri.cachebox3.logging.Logger;
import de.longri.cachebox3.logging.LoggerFactory;
import de.longri.cachebox3.settings.Config;
import de.longri.cachebox3.types.Cache;
import de.longri.cachebox3.types.Categories;
import de.longri.cachebox3.types.Waypoint;
import de.longri.cachebox3.utils.ScaledSizes;
import de.longri.cachebox3.utils.SkinColor;

/**
 * Static class
 * Created by Longri on 20.07.2016.
 */
public class CB {
    final static Logger log = LoggerFactory.getLogger(CB.class);

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
    private static boolean displayOff = false;
    public static Categories Categories;
    public static float stateTime;


    final static float PPI_DEFAULT = 163;
    private static float globalScale = 1;
    public static ViewManager viewmanager;
    public static final String br = System.getProperty("line.separator");
    public static final String fs = System.getProperty("file.separator");

    public static final String AboutMsg = "Team Cachebox (2011-2016)" + br + "www.team-cachebox.de" + br + "Cache Icons Copyright 2009," + br + "Groundspeak Inc. Used with permission";
    public static final String splashMsg = AboutMsg + br + br + "POWERED BY:";

    private static Cache selectedCache = null;
    private static Waypoint selectedWaypoint = null;
    private static Cache nearestCache = null;
    private static boolean autoResort;
    public static boolean switchToCompassCompleted = false;
    public static String cacheHistory = "";


    /**
     * WorkPath is a String to the used work path.<br>
     * This Path is a absolute path.<br>
     * <br>
     * On iOS this is the path to the "SandBox". <br>
     * On Android this can a path to internal SD "/Cachebox3" <br>
     * or to the "SandBox" on the external SD
     */
    public static String WorkPath;
    private static SvgSkin actSkin;
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

    public static void setActSkin(SvgSkin skin) {
        if (actSkin != null) {
            VisUI.dispose();
        }
        actSkin = skin;
        VisUI.load(actSkin);

        // calculate scaled sizes
        float button_width = CB.getScaledFloat(actSkin.get("button_width", ScaledSize.class).value);
        float button_height = CB.getScaledFloat(actSkin.get("button_height", ScaledSize.class).value);
        float button_width_wide = CB.getScaledFloat(actSkin.get("button_width_wide", ScaledSize.class).value);
        float margin = CB.getScaledFloat(actSkin.get("margin", ScaledSize.class).value);
        float check_box_height = CB.getScaledFloat(actSkin.get("check_box_height", ScaledSize.class).value);
        float window_margin = CB.getScaledFloat(actSkin.get("check_box_height", ScaledSize.class).value);
        CB.scaledSizes = new ScaledSizes(button_width, button_height, button_width_wide, margin,
                check_box_height, window_margin);
    }

    public static SkinColor getColor(String name) {
        return actSkin.get(name, SkinColor.class);
    }

    public static SvgSkin getSkin() {
        return actSkin;
    }


    private static float scalefactor = 0;

    public static float getScaledFloat(float value) {
        if (scalefactor == 0)
            calcScaleFactor();
        return (value * scalefactor);
    }

    public static float getScaledFloat(int value) {
        if (scalefactor == 0)
            calcScaleFactor();
        return (value * scalefactor);
    }

    public static float getUnScaledFloat(float value) {
        if (scalefactor == 0)
            calcScaleFactor();
        return (value / scalefactor);
    }

    public static int getScaledInt(int i) {
        if (scalefactor == 0)
            calcScaleFactor();
        return (int) (i * scalefactor);
    }

    private static void calcScaleFactor() {
        scalefactor = (Math.max(Gdx.graphics.getPpiX(), Gdx.graphics.getPpiY()) / PPI_DEFAULT) * globalScale;
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

        if (CB.isSetSelectedCache()) {
            // speichere selektierten Cache, da nicht alles über die SelectedCacheEventList läuft
            Config.LastSelectedCache.setValue(CB.getSelectedCache().getGcCode());
            Config.AcceptChanges();
            log.debug("LastSelectedCache = " + CB.getSelectedCache().getGcCode());
        }

        Gdx.app.exit();
    }

    public static boolean isDisplayOff() {
        return displayOff;
    }

    public static boolean selectedCachehasSpoiler() {
        return false; //TODO
    }

    public enum Platform {
        ANDROID, IOS, DESKTOP
    }

    public static Platform platform;

    public static void requestRendering() {
        Gdx.graphics.requestRendering();
    }

    public static void setSelectedCache(Cache cache) {
        selectedCache = cache;

        //call selected cache changed event
        SelectedCacheEventList.Call(selectedCache, selectedWaypoint);

    }

    public static void setSelectedWaypoint(Cache cache, Waypoint waypoint) {
        if (cache == null)
            return;

        setSelectedWaypoint(cache, waypoint, true);
        if (waypoint == null) {
            cacheHistory = cache.getGcCode() + "," + cacheHistory;
            if (cacheHistory.length() > 120) {
                cacheHistory = cacheHistory.substring(0, cacheHistory.lastIndexOf(","));
            }
        }
    }

    /**
     * if changeAutoResort == false -> do not change state of autoResort Flag
     *
     * @param cache
     * @param waypoint
     * @param changeAutoResort
     */
    public static void setSelectedWaypoint(Cache cache, Waypoint waypoint, boolean changeAutoResort) {

        if (cache == null) {
            log.info("[CB]setSelectedWaypoint: cache=null");
            selectedCache = null;
            selectedWaypoint = null;
            return;
        }

        // remove Detail Info from old selectedCache
        if ((selectedCache != cache) && (selectedCache != null) && (selectedCache.detail != null)) {
            selectedCache.deleteDetail(Config.ShowAllWaypoints.getValue());
        }
        selectedCache = cache;
        log.info("[CB]setSelectedWaypoint: cache=" + cache.getGcCode());
        selectedWaypoint = waypoint;

        // load Detail Info if not available
        if (selectedCache.detail == null) {
            selectedCache.loadDetail();
        }

        SelectedCacheEventList.Call(selectedCache, selectedWaypoint);

        if (changeAutoResort) {
            // switch off auto select
            setAutoResort(false);
        }
    }


    public static boolean getAutoResort() {
        return autoResort;
    }

    public static void setAutoResort(boolean value) {
        autoResort = value;
    }

    /**
     * Returns true, if a Cache selected and this Cache object is valid.
     *
     * @return
     */
    public static boolean isSetSelectedCache() {
        if (selectedCache == null)
            return false;

        if (selectedCache.getGcCode().length() == 0)
            return false;

        return true;
    }

    public static Cache getSelectedCache() {
        return selectedCache;
    }

    public static boolean isSelectedCache(Cache cache) {
        if (selectedCache != null && selectedCache.equals(cache)) return true;
        return false;
    }

    public static void setNearestCache(Cache Cache) {
        nearestCache = Cache;
    }

    public static Coordinate getSelectedCoord() {
        Coordinate ret = null;

        if (selectedWaypoint != null) {
            ret = selectedWaypoint;
        } else if (selectedCache != null) {
            ret = selectedCache;
        }

        return ret;
    }

    public static Cache NearestCache() {
        return nearestCache;
    }

    public static Waypoint getSelectedWaypoint() {
        return selectedWaypoint;
    }


    // GL-Thread check
    private static Thread MAIN_THREAD;

    public static void assertMainThread() {
        if (MAIN_THREAD != Thread.currentThread()) {
            throw new RuntimeException("Access from non-GL thread!");
        }
    }

    public static boolean isMainThread() {
        return MAIN_THREAD == Thread.currentThread();
    }

    public static void initThreadCheck() {
        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                MAIN_THREAD = Thread.currentThread();
            }
        });
    }
}
