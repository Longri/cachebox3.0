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
package de.longri.cachebox3.events;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ArrayMap;
import com.badlogic.gdx.utils.async.AsyncExecutor;
import com.badlogic.gdx.utils.async.AsyncTask;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.events.location.OrientationChangedEvent;
import de.longri.cachebox3.events.location.OrientationChangedListener;
import de.longri.cachebox3.events.location.PositionChangedEvent;
import de.longri.cachebox3.events.location.PositionChangedListener;
import de.longri.cachebox3.events.location.SpeedChangedListener;
import de.longri.cachebox3.locator.Coordinate;
import de.longri.cachebox3.locator.CoordinateGPS;
import de.longri.cachebox3.settings.Config;
import de.longri.cachebox3.sqlite.Database;
import de.longri.cachebox3.sqlite.dao.DaoFactory;
import de.longri.cachebox3.types.AbstractCache;
import de.longri.cachebox3.types.AbstractWaypoint;
import de.longri.cachebox3.utils.MathUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;

/**
 * Created by Longri on 23.03.2017.
 */
public class EventHandler implements SelectedCacheChangedListener, SelectedWayPointChangedListener, de.longri.cachebox3.events.location.PositionChangedListener, de.longri.cachebox3.events.location.OrientationChangedListener {

    static final Logger log = LoggerFactory.getLogger(EventHandler.class);


    static final private Class[] allListener = new Class[]{de.longri.cachebox3.events.location.PositionChangedListener.class,
            SelectedCacheChangedListener.class, SelectedWayPointChangedListener.class, PositionChangedListener.class,
            DistanceChangedListener.class, SpeedChangedListener.class, OrientationChangedListener.class,
            SelectedCoordChangedListener.class, ImportProgressChangedListener.class, ApiCallLimitListener.class,
            IncrementProgressListener.class};
    static final private ArrayMap<Class, Array<Object>> listenerMap = new ArrayMap<>();

    private static final EventHandler INSTANCE = new EventHandler();
    private static final AsyncExecutor asyncExecutor = new AsyncExecutor(20);

    public static void INIT() {
    }


    private static short lastID;

    public static short getId() {
        return lastID++;
    }

    public static void add(Object listener) {
        synchronized (listenerMap) {
            for (Type type : listener.getClass().getGenericInterfaces()) {
                for (Class clazz : allListener) {
                    if (type == clazz) {
                        Array<Object> list = listenerMap.get(clazz);
                        if (list == null) {
                            list = new Array<>();
                            listenerMap.put(clazz, list);
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
        synchronized (listenerMap) {
            for (Type type : listener.getClass().getGenericInterfaces()) {
                for (Class clazz : allListener) {
                    if (type == clazz) {
                        Array<Object> list = listenerMap.get(clazz);
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

        synchronized (listenerMap) {
            final Array<Object> list = listenerMap.get(event.getListenerClass());
            if (list != null) {
                asyncExecutor.submit(new AsyncTask<Void>() {
                    @Override
                    public Void call() throws Exception {
                        for (int i = 0, n = list.size; i < n; i++) {
                            try {
                                event.getListenerClass().getDeclaredMethods()[0].invoke(list.items[i], event);
                            } catch (IllegalAccessException e) {
                                e.printStackTrace();
                            } catch (Exception e) {
                                log.error("Fire event to" + list.items[i].getClass().getSimpleName(), e.getCause());
                            }
                        }
                        return null;
                    }
                });
            }
        }
    }

    private EventHandler() {
        add(this);
    }

    AbstractCache selectedCache;
    AbstractWaypoint selectedWayPoint;
    Coordinate selectedCoordinate;
    Coordinate myPosition;
    private float heading;

    @Override
    public void selectedCacheChanged(SelectedCacheChangedEvent event) {
        if (selectedCache == null || !selectedCache.equals(event.cache)) {

            log.debug("Set Global selected Cache: {}", event.cache);
            load_unload_Cache_Waypoints(selectedCache, event.cache);
            selectedCache = event.cache;
            selectedWayPoint = null;
            fireSelectedCoordChanged(event.ID);
        }
    }

    @Override
    public void selectedWayPointChanged(SelectedWayPointChangedEvent event) {
        if (selectedWayPoint == null || !selectedWayPoint.equals(event.wayPoint)) {
            log.debug("Set Global selected Waypoint: {}", event.wayPoint);
            selectedWayPoint = event.wayPoint;
            if (selectedWayPoint != null) {
                AbstractCache newCache = Database.Data.Query.GetCacheById(selectedWayPoint.getCacheId());
                if (!newCache.equals(selectedCache)) {
                    load_unload_Cache_Waypoints(selectedCache, newCache);
                    selectedCache = newCache;
                }

                fireSelectedCoordChanged(event.ID);
            }
        }
    }

    private void load_unload_Cache_Waypoints(AbstractCache oldCache, AbstractCache newCache) {

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
            fireDistanceChanged(event.ID);
        }
    }

    private float lastDistance;

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

    public static AbstractCache getSelectedCache() {
        return INSTANCE.selectedCache;
    }

    public static boolean isSelectedCache(AbstractCache abstractCache) {
        return (INSTANCE.selectedCache != null && INSTANCE.selectedCache.equals(abstractCache));
    }

    public static AbstractWaypoint getSelectedWaypoint() {
        return INSTANCE.selectedWayPoint;
    }

    public static Coordinate getSelectedCoord() {
        return INSTANCE.selectedCoordinate;
    }

    public String toString() {
        return "EventHandler";
    }


    public static Coordinate getMyPosition() {

        if (INSTANCE.myPosition == null) {
            //return last stored Pos
            return new Coordinate(Config.MapInitLatitude.getValue(), Config.MapInitLongitude.getValue());
        }
        return INSTANCE.myPosition;
    }

    public static float getHeading() {
        return INSTANCE.heading;
    }

    @Override
    public void orientationChanged(OrientationChangedEvent event) {
        INSTANCE.heading = event.getOrientation();
    }

    public static void setSelectedWaypoint(AbstractCache cache, AbstractWaypoint wp) {
        if (cache == null || !cache.equals(getSelectedCache())) fire(new SelectedCacheChangedEvent(cache));
        if (wp == null || !wp.equals(getSelectedWaypoint())) fire(new SelectedWayPointChangedEvent(wp));
    }

    public static void updateSelectedCache(AbstractCache selectedCache) {
        if (INSTANCE.selectedCache.getId() != selectedCache.getId()) {
            log.warn("update Selected Cache with new Cache! Fire Change Event?");
        }
        INSTANCE.selectedCache = selectedCache;
    }
}
