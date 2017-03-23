package de.longri.cachebox3.locator.events.newT;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ArrayMap;
import de.longri.cachebox3.gui.events.SelectedCacheEvent;
import de.longri.cachebox3.locator.Coordinate;
import de.longri.cachebox3.types.Cache;
import de.longri.cachebox3.types.Waypoint;
import de.longri.cachebox3.utils.MathUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;

/**
 * Created by Longri on 23.03.2017.
 */
public class EventHandler implements SelectedCacheChangedListener, SelectedWayPointChangedListener, PositionChangedListener {

    static final private Class[] allListener = new Class[]{PositionChangedListener.class,
            SelectedCacheChangedListener.class, SelectedWayPointChangedListener.class, PositionChangedListener.class,
            DistanceChangedListener.class};
    static final private ArrayMap<Class, Array<Object>> listenerMap = new ArrayMap<>();

    static final EventHandler INSTANCE = new EventHandler();
    public static void add(Object listener) {
        for (Type type : listener.getClass().getGenericInterfaces()) {
            for (Class clazz : allListener) {
                if (type == clazz) {
                    Array<Object> list = listenerMap.get(listener.getClass().getSuperclass());
                    if (list == null) {
                        list = new Array<>();
                        listenerMap.put(clazz, list);
                    }
                    list.add(listener);
                }
            }
        }
    }

    public static void remove(Object listener) {
        for (Type type : listener.getClass().getGenericInterfaces()) {
            for (Class clazz : allListener) {
                if (type == clazz) {
                    Array<Object> list = listenerMap.get(listener.getClass().getSuperclass());
                    if (list != null) {
                        list.removeValue(listener, true);
                    }
                }
            }
        }
    }

    public static void fire(AbstractEvent event) {
        Array<Object> list = listenerMap.get(event.getListenerClass());
        for (int i = 0, n = list.size; i < n; i++) {
            try {
                event.getListenerClass().getDeclaredMethods()[0].invoke(list.items[i], event);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }

    private EventHandler() {
        add(this);
    }

    Cache selectedCache;
    Waypoint selectedWayPoint;
    Coordinate selectedCoordinate, myPosition;

    @Override
    public void selectedCacheChanged(SelectedCacheChangedEvent event) {
        if (selectedCache != event.cache) {
            fireSelectedCoordChanged();
        }
        selectedCache = event.cache;
    }

    @Override
    public void selectedWayPointChanged(SelectedWayPointChangedEvent event) {
        if (selectedWayPoint != event.wayPoint) {
            fireSelectedCoordChanged();
        }
        selectedWayPoint = event.wayPoint;
    }

    private void fireSelectedCoordChanged() {
        if (selectedCache == null) {
            fireCoordChanged(new SelectedCoordChangedEvent(null));
        } else if (selectedWayPoint == null) {
            fireCoordChanged(new SelectedCoordChangedEvent(new Coordinate(selectedCache.getLatitude(), selectedCache.getLongitude())));
        } else {
            fireCoordChanged(new SelectedCoordChangedEvent(new Coordinate(selectedWayPoint.getLatitude(), selectedWayPoint.getLongitude())));
        }
    }


    private void fireCoordChanged(SelectedCoordChangedEvent event) {
        if (this.selectedCoordinate != event.coordinate) {
            fire(event);
            this.selectedCoordinate = event.coordinate;
            fireDistanceChanged();
        }
    }

    private float lastDistance;

    private void fireDistanceChanged() {
        if (this.myPosition != null && this.selectedCoordinate != null) {
            float distance = this.myPosition.Distance(this.selectedCoordinate, MathUtils.CalculationType.ACCURATE);
            if (lastDistance != distance) {
                fire(new DistanceChangedEvent(distance));
                lastDistance = distance;
            }
        }
    }

    @Override
    public void positionChanged(PositionChangedEvent event) {
        if (!this.myPosition.equals(event.pos)) {
            fireDistanceChanged();
        }
    }
}
