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
import de.longri.cachebox3.locator.Coordinate;
import de.longri.cachebox3.locator.CoordinateGPS;
import de.longri.cachebox3.sqlite.Database;
import de.longri.cachebox3.types.Cache;
import de.longri.cachebox3.types.Waypoint;
import de.longri.cachebox3.utils.MathUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;

/**
 * Created by Longri on 23.03.2017.
 */
public class EventHandler implements SelectedCacheChangedListener, SelectedWayPointChangedListener, PositionChangedListener {

    static final Logger log = LoggerFactory.getLogger(EventHandler.class);


    static final private Class[] allListener = new Class[]{PositionChangedListener.class,
            SelectedCacheChangedListener.class, SelectedWayPointChangedListener.class, PositionChangedListener.class,
            DistanceChangedListener.class, SpeedChangedListener.class, OrientationChangedListener.class,
            SelectedCoordChangedListener.class};
    static final private ArrayMap<Class, Array<Object>> listenerMap = new ArrayMap<>();

    private static final EventHandler INSTANCE = new EventHandler();
    private static final AsyncExecutor asyncExecutor=new AsyncExecutor(20);

    public static void INIT() {
    }


    private static short lastID;

    public static short getId() {
        return lastID++;
    }

    public static void add(Object listener) {
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

    public static void remove(Object listener) {
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

    public static void fire(final AbstractEvent event) {

        final Array<Object> list = listenerMap.get(event.getListenerClass());
        if (list != null) {
            if (list.size > 0)
                log.debug("Fire {} event {} to {} listener: {}", event.getClass().getSimpleName(), event.ID, list.size, list.toString());


            asyncExecutor.submit(new AsyncTask<Void>() {
                @Override
                public Void call() throws Exception {
                    for (int i = 0, n = list.size; i < n; i++) {
                        try {
                            event.getListenerClass().getDeclaredMethods()[0].invoke(list.items[i], event);
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        } catch (InvocationTargetException e) {
                            log.error("Fire event to" + list.items[i].getClass().getSimpleName(), e.getCause());
                        }
                    }
                    return null;
                }
            });
        }
    }

    private EventHandler() {
        add(this);
    }

    Cache selectedCache;
    Waypoint selectedWayPoint;
    Coordinate selectedCoordinate;
    CoordinateGPS myPosition;
    private float heading;

    @Override
    public void selectedCacheChanged(SelectedCacheChangedEvent event) {
        if (selectedCache == null || !selectedCache.equals(event.cache)) {
            log.debug("Set Global selected Cache: {}", event.cache);
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
            synchronized (Database.Data.Query){
                if(selectedWayPoint!=null){
                    selectedCache = Database.Data.Query.GetCacheById(selectedWayPoint.CacheId);
                    fireSelectedCoordChanged(event.ID);
                }
            }
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
        if ((this.myPosition == null && event.pos != null) || !this.myPosition.equals(event.pos)) {
            this.myPosition = event.pos;
            fireDistanceChanged(event.ID);
        }
    }

    public static Cache getSelectedCache() {
        return INSTANCE.selectedCache;
    }

    public static boolean isSelectedCache(Cache cache) {
        return (INSTANCE.selectedCache != null && INSTANCE.selectedCache.equals(cache));
    }

    public static Waypoint getSelectedWaypoint() {
        return INSTANCE.selectedWayPoint;
    }

    public static Coordinate getSelectedCoord() {
        return INSTANCE.selectedCoordinate;
    }

    public String toString() {
        return "EventHandler";
    }


    public static CoordinateGPS getMyPosition() {
        return INSTANCE.myPosition;
    }

    public static float getHeading() {
        return INSTANCE.heading;
    }
}
