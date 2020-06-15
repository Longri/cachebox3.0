/*
 * Copyright (C) 2020 - 2019 team-cachebox.de
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
package de.longri.cachebox3.events;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ArrayMap;
import com.badlogic.gdx.utils.async.AsyncExecutor;
import com.badlogic.gdx.utils.async.AsyncTask;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.Utils;
import de.longri.cachebox3.events.location.*;
import de.longri.cachebox3.gui.map.MapMode;
import de.longri.cachebox3.locator.Coordinate;
import de.longri.cachebox3.settings.Config;
import de.longri.cachebox3.settings.Settings;
import de.longri.cachebox3.sqlite.Database;
import de.longri.cachebox3.sqlite.dao.DaoFactory;
import de.longri.cachebox3.types.AbstractCache;
import de.longri.cachebox3.types.AbstractWaypoint;
import de.longri.cachebox3.types.ImageEntry;
import de.longri.cachebox3.utils.MathUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileFilter;
import java.lang.reflect.Type;
import java.util.Locale;

/**
 * Created by Longri on 23.03.2017.
 */
public class EventHandler implements SelectedCacheChangedListener, SelectedWayPointChangedListener, PositionChangedListener, OrientationChangedListener {

    private static final Logger log = LoggerFactory.getLogger(EventHandler.class);

    static final private Class[] allListener = new Class[]{
            SelectedCacheChangedListener.class, SelectedWayPointChangedListener.class, PositionChangedListener.class,
            DistanceChangedListener.class, SpeedChangedListener.class, OrientationChangedListener.class,
            SelectedCoordChangedListener.class, ImportProgressChangedListener.class, ApiCallLimitListener.class,
            IncrementProgressListener.class, AccuracyChangedListener.class, CacheListChangedListener.class};
    static final private ArrayMap<Class, Array<Object>> listeners = new ArrayMap<>();

    private static final EventHandler INSTANCE = new EventHandler();
    private static final AsyncExecutor asyncExecutor = new AsyncExecutor(20);
    private static short lastID;
    private final Array<ImageEntry> spoilerResources = new Array<>();
    private AbstractCache selectedCache;
    private AbstractWaypoint selectedWayPoint;
    private Coordinate selectedCoordinate;
    private Coordinate myPosition;
    private float heading;
    private boolean spoilerLoaded = false;
    private float lastDistance;

    private EventHandler() {
        add(this);
    }

    public static void INIT() {
    }

    public static short getId() {
        return lastID++;
    }

    public static void add(Object listener) {
        synchronized (listeners) {
            for (Type type : listener.getClass().getGenericInterfaces()) {
                for (Class clazz : allListener) {
                    if (type == clazz) {
                        Array<Object> list = listeners.get(clazz);
                        if (list == null) {
                            list = new Array<>();
                            listeners.put(clazz, list);
                        }
                        if (!list.contains(listener, true)) {
                            log.debug("Add {} Event listener: {}", clazz.getSimpleName(), listener.getClass().getSimpleName());
                            list.add(listener);
                        }
                    }
                }
            }
        }
    }

    public static void remove(Object listener) {
        synchronized (listeners) {
            for (Type type : listener.getClass().getGenericInterfaces()) {
                for (Class clazz : allListener) {
                    if (type == clazz) {
                        Array<Object> list = listeners.get(clazz);
                        if (list != null) {
                            log.debug("Remove {} Event listener: {}", clazz.getSimpleName(), listener.getClass().getSimpleName());
                            list.removeValue(listener, true);
                        }
                    }
                }
            }
        }
    }

    public static void fire(final AbstractEvent event) {

        //ignore events if we are on background
        if (CB.isBackground) return;

        synchronized (listeners) {
            if (event instanceof SelectedWayPointChangedEvent) {

            }
            final Array<Object> list = listeners.get(event.getListenerClass());
            if (list != null) {

                //call this EventHandler first
                final int myIndex = list.indexOf(INSTANCE, true);

                if (myIndex >= 0) {
                    try {
                        event.getListenerClass().getDeclaredMethods()[0].invoke(list.items[myIndex], event);
                    } catch (Exception e) {
                        String name;
                        try {
                            name = list.items[myIndex].getClass().getSimpleName();
                        } catch (Exception e1) {
                            name = "???";
                        }
                        log.error("Fire event to" + name, e);
                    }
                }

                asyncExecutor.submit((AsyncTask<Void>) () -> {
                    for (int i = 0, n = list.size; i < n; i++) {
                        if (myIndex >= 0 && i == myIndex) continue;

                        try {
                            event.getListenerClass().getDeclaredMethods()[0].invoke(list.items[i], event);
                        } catch (Exception e) {
                            CB.stageManager.indicateException(CB.EXCEPTION_COLOR_EVENT);
                            String name;
                            try {
                                name = list.items[i].getClass().getSimpleName();
                            } catch (Exception e1) {
                                name = "???";
                            }
                            log.error("Fire event to" + name, e);
                        }
                    }
                    return null;
                });
            }
        }
    }

    public static boolean actCacheHasSpoiler() {
        if (!INSTANCE.spoilerLoaded) INSTANCE.reloadCacheSpoiler();
        return INSTANCE.spoilerResources.size > 0;
    }

    public static void forceReloadSpoiler() {
        INSTANCE.spoilerLoaded = false;
    }

    public static Array<ImageEntry> getSelectedCacheSpoiler() {
        return actCacheHasSpoiler() ? INSTANCE.spoilerResources : null;
    }

    private static void loadSpoilerResourcesFromPath(final String directory, final AbstractCache cache, Array<ImageEntry> spoilerResources) {
        log.debug("LoadSpoilerResourcesFromPath from " + directory);

        FileHandle dir = Gdx.files.absolute(directory);

        if (!dir.isDirectory()) return;

        FileFilter filter = pathname -> {
            String filename = pathname.getName();
            filename = filename.toLowerCase(Locale.getDefault());
            if (filename.contains(cache.getGeoCacheCode().toString().toLowerCase(Locale.getDefault()))) {
                if (filename.endsWith(".jpg") || filename.endsWith(".jpeg") || filename.endsWith(".bmp") || filename.endsWith(".png") || filename.endsWith(".gif")) {
                    // don't load Thumbs
                    return !filename.startsWith(Utils.THUMB) && !filename.startsWith(Utils.THUMB_OVERVIEW + Utils.THUMB);
                }
            }
            return false;
        };
        FileHandle[] files = dir.list(filter);

        if (!(files == null)) {
            if (files.length > 0) {
                for (FileHandle file : files) {
                    String ext = file.extension();
                    if (ext.equalsIgnoreCase("jpg") || ext.equalsIgnoreCase("jpeg") || ext.equalsIgnoreCase("bmp") || ext.equalsIgnoreCase("png") || ext.equalsIgnoreCase("gif")) {
                        ImageEntry imageEntry = new ImageEntry();
                        imageEntry.LocalPath = file.file().getAbsolutePath();
                        imageEntry.Name = file.name();
                        log.debug(imageEntry.Name);
                        spoilerResources.add(imageEntry);
                    }
                }
            }
        }
    }

    public static AbstractCache getSelectedCache() {
        return INSTANCE.selectedCache;
    }

    public static boolean isSelectedCache(AbstractCache abstractCache) {
        return (INSTANCE.selectedCache != null && INSTANCE.selectedCache.equals(abstractCache));
    }

    public static boolean isSetSelectedCache() {
        return (INSTANCE.selectedCache != null);
    }

    public static AbstractWaypoint getSelectedWayPoint() {
        return INSTANCE.selectedWayPoint;
    }

    public static Coordinate getSelectedCoord() {
        return INSTANCE.selectedCoordinate;
    }

    public static Coordinate getMyPosition() {
        if (INSTANCE.myPosition == null) {
            //return last stored Pos
            Coordinate lastStoredPos = CB.lastMapState.getFreePosition();
            if (lastStoredPos == null) return null;
            return new Coordinate(lastStoredPos.getLatitude(), lastStoredPos.getLongitude());
        }
        return INSTANCE.myPosition;
    }

    public static float getHeading() {
        return INSTANCE.heading;
    }

    public static void fireSelectedWaypointChanged(AbstractCache cache, AbstractWaypoint wp) {
        if (cache == null || !cache.equals(getSelectedCache())) fire(new SelectedCacheChangedEvent(cache));
        if (wp == null || !wp.equals(getSelectedWayPoint())) fire(new SelectedWayPointChangedEvent(wp));
    }

    public static void updateSelectedCache(AbstractCache selectedCache) {
        if (INSTANCE.selectedCache.getId() != selectedCache.getId()) {
            log.warn("update Selected Cache with new Cache! Fire Change Event?");
        }
        INSTANCE.selectedCache = selectedCache;
    }

    private void reloadCacheSpoiler() {

        AbstractCache actCache = getSelectedCache();
        spoilerResources.clear();

        String directory;
        String gcCode = actCache.getGeoCacheCode().toString();
        if (gcCode.length() < 4)
            return; // don't load spoiler

        // from own Repository
        String path = Settings.SpoilerFolderLocal.getValue();
        log.debug("from SpoilerFolderLocal: " + path);
        try {
            if (path != null && path.length() > 0) {
                directory = path + "/" + gcCode.substring(0, 4);
                loadSpoilerResourcesFromPath(directory, actCache, spoilerResources);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // from Description own Repository
        try {
            path = Settings.DescriptionImageFolderLocal.getValue();
            log.debug("from DescriptionImageFolderLocal: " + path);
            directory = path + "/" + gcCode.substring(0, 4);
            loadSpoilerResourcesFromPath(directory, actCache, spoilerResources);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // from Description Global Repository
        try {
            path = Settings.DescriptionImageFolder.getValue();
            log.debug("from DescriptionImageFolder: " + path);
            directory = path + "/" + gcCode.substring(0, 4);
            loadSpoilerResourcesFromPath(directory, actCache, spoilerResources);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // from Spoiler Global Repository
        try {
            path = Settings.SpoilerFolder.getValue();
            log.debug("from SpoilerFolder: " + path);
            directory = path + "/" + gcCode.substring(0, 4);
            loadSpoilerResourcesFromPath(directory, actCache, spoilerResources);
        } catch (Exception e) {
            log.error("", e);
        }

        // Add own taken photo
        directory = Settings.UserImageFolder.getValue();
        if (directory != null) {
            try {
                loadSpoilerResourcesFromPath(directory, actCache, spoilerResources);
            } catch (Exception e) {
                log.error("", e);
            }
        }

        spoilerLoaded = true;
    }

    @Override
    public void selectedCacheChanged(SelectedCacheChangedEvent event) {
        if (event.cache != null) {
            log.debug("Set Global selected Cache: {}", event.cache + ": " + event.cache.getGeoCacheName().toString());
            load_unload_Cache_Waypoints(selectedCache, event.cache);
            selectedCache = event.cache;
            selectedWayPoint = null;
            fireSelectedCoordChanged(event.ID);
            // add cache to history
            if (!CB.cacheHistory.startsWith(selectedCache.getGeoCacheCode().toString())) {
                CB.cacheHistory = selectedCache.getGeoCacheCode() + (CB.cacheHistory.length() > 0 ? "," : "") + CB.cacheHistory.replace("," + selectedCache.getGeoCacheCode(), "");
                if (CB.cacheHistory.length() > 120) {
                    CB.cacheHistory = CB.cacheHistory.substring(0, CB.cacheHistory.lastIndexOf(","));
                }
            }
        }
    }

    @Override
    public void selectedWayPointChanged(SelectedWayPointChangedEvent event) {
        if (selectedWayPoint == null || !selectedWayPoint.equals(event.wayPoint)) {
            log.debug("Set Global selected Waypoint: {}", event.wayPoint);
            selectedWayPoint = event.wayPoint;
            if (selectedWayPoint != null) {
                AbstractCache newCache = Database.Data.cacheList.getCacheById(selectedWayPoint.getCacheId());
                if (!newCache.equals(selectedCache)) {
                    load_unload_Cache_Waypoints(selectedCache, newCache);
                    selectedCache = newCache;
                }
                fireSelectedCoordChanged(event.ID);
            }
        }
    }

    private void load_unload_Cache_Waypoints(AbstractCache oldCache, AbstractCache newCache) {
        // clear loaded spoiler resources
        spoilerLoaded = false;
        spoilerResources.clear();

        //with show all waypoints, must all waypoints loaded, so do nothing
        if (!Config.ShowAllWaypoints.getValue()) {

            //clear old
            if (oldCache != null && oldCache.getWaypoints() != null) {
                //dispose waypoints
                int n = oldCache.getWaypoints().size;
                while (n-- > 0) {
                    oldCache.getWaypoints().get(n).dispose();
                }
                oldCache.getWaypoints().clear();
            }

            //load new
            if (newCache != null)
                newCache.setWaypoints(DaoFactory.WAYPOINT_DAO.getWaypointsFromCacheID(Database.Data, newCache.getId(), true));
        }

        //remove loaded description
        if (oldCache != null) {
            oldCache.setShortDescription(null);
            oldCache.setLongDescription(null);
        }

    }

    private void fireSelectedCoordChanged(short id) {
        if (selectedCache == null) {
            fireCoordChanged(new SelectedCoordChangedEvent(null, id));
        } else if (selectedWayPoint == null) {
            fireCoordChanged(new SelectedCoordChangedEvent(new Coordinate(selectedCache.getLatitude(),
                    selectedCache.getLongitude()), id));
        } else {
            fireCoordChanged(new SelectedCoordChangedEvent(new Coordinate(selectedWayPoint.getLatitude(),
                    selectedWayPoint.getLongitude()), id));
        }
    }

    private void fireCoordChanged(SelectedCoordChangedEvent event) {
        if (this.selectedCoordinate == null || !this.selectedCoordinate.equals(event.coordinate)) {
            this.selectedCoordinate = event.coordinate;
            fire(event);
            CB.lastMapState.setMapMode(MapMode.WP);
            CB.lastMapState.setPosition(selectedCoordinate);
            fireDistanceChanged(event.ID);
        }
    }

    private void fireDistanceChanged(short id) {
        if (this.myPosition != null && this.selectedCoordinate != null) {
            float distance = this.myPosition.distance(this.selectedCoordinate, MathUtils.CalculationType.ACCURATE);
            if (lastDistance != distance) {
                lastDistance = distance;
                fire(new DistanceChangedEvent(distance, id));
            }
        }
    }

    @Override
    public void positionChanged(PositionChangedEvent event) {
        if (event.pos != null) {
            if (event.gpsProvided) {
                this.myPosition = event.pos;
                fireDistanceChanged(event.ID);
            }
        }
    }

    public String toString() {
        return "EventHandler";
    }

    @Override
    public void orientationChanged(OrientationChangedEvent event) {
        INSTANCE.heading = event.getOrientation();
    }
}
