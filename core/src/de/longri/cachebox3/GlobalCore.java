/* 
 * Copyright (C) 2014-2016 team-cachebox.de
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
import com.badlogic.gdx.files.FileHandle;
import de.longri.cachebox3.gui.events.SelectedCacheEventList;
import de.longri.cachebox3.locator.Coordinate;
import de.longri.cachebox3.settings.Config;
import de.longri.cachebox3.types.Cache;
import de.longri.cachebox3.types.Waypoint;
import org.slf4j.LoggerFactory;


/**
 * @author ging-buh
 * @author arbor95
 * @author longri
 */
public class GlobalCore {
    final static org.slf4j.Logger log = LoggerFactory.getLogger(GlobalCore.class);
    public static final int CurrentRevision = 20160727;

    public static final String CurrentVersion = "3.0.";
    public static final String VersionPrefix = "test";


    public static final String br = System.getProperty("line.separator");
    public static final String fs = System.getProperty("file.separator");

    public static final String AboutMsg = "Team Cachebox (2011-2016)" + br + "www.team-cachebox.de" + br + "Cache Icons Copyright 2009," + br + "Groundspeak Inc. Used with permission";
    public static final String splashMsg = AboutMsg + br + br + "POWERED BY:";


    private static boolean isTestVersionCheked = false;
    private static boolean isTestVersion = false;

    private static Cache selectedCache = null;
    private static Waypoint selectedWaypoint = null;
    private static Cache nearestCache = null;
    private static boolean autoResort;
    public static boolean switchToCompassCompleted = false;


    public static boolean isTestVersion() {
        if (isTestVersionCheked) return isTestVersion;

        isTestVersion = VersionPrefix.contains("Test") || VersionPrefix.contains("test");
        isTestVersionCheked = true;
        return isTestVersion;
    }

    public static String getVersionString() {
        final String ret = "Version: " + CurrentVersion + String.valueOf(CurrentRevision) + "  " + (VersionPrefix.equals("") ? "" : "(" + VersionPrefix + ")");
        return ret;
    }

    public static String cacheHistory = "";

    public static void setSelectedCache(Cache cache) {
        selectedCache = cache;
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
            log.info("[GlobalCore]setSelectedWaypoint: cache=null");
            selectedCache = null;
            selectedWaypoint = null;
            return;
        }

        // remove Detail Info from old selectedCache
        if ((selectedCache != cache) && (selectedCache != null) && (selectedCache.detail != null)) {
            selectedCache.deleteDetail(Config.ShowAllWaypoints.getValue());
        }
        selectedCache = cache;
        log.info("[GlobalCore]setSelectedWaypoint: cache=" + cache.getGcCode());
        selectedWaypoint = waypoint;

        // load Detail Info if not available
        if (selectedCache.detail == null) {
            selectedCache.loadDetail();
        }

        SelectedCacheEventList.Call(selectedCache, selectedWaypoint);

        if (changeAutoResort) {
            // switch off auto select
            GlobalCore.setAutoResort(false);
        }
    }


    public static boolean getAutoResort() {
        return autoResort;
    }

    public static void setAutoResort(boolean value) {
        GlobalCore.autoResort = value;
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

    public static void setNearestCache(Cache Cache) {
        nearestCache = Cache;
    }

    public static Coordinate getSelectedCoord() {
        Coordinate ret = null;

        if (selectedWaypoint != null) {
            ret = selectedWaypoint.Pos;
        } else if (selectedCache != null) {
            ret = selectedCache.Pos;
        }

        return ret;
    }

    public static Cache NearestCache() {
        return nearestCache;
    }

    public static Waypoint getSelectedWaypoint() {
        return selectedWaypoint;
    }

}
