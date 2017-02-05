package de.longri.cachebox3.locator.events;

import de.longri.cachebox3.locator.Locator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;


public class PositionChangedEventList {
    final static Logger log = LoggerFactory.getLogger(PositionChangedEventList.class);
    private static final ArrayList<PositionChangedEvent> list = new ArrayList<PositionChangedEvent>();

    public static void Add(PositionChangedEvent event) {
        synchronized (list) {
            if (!list.contains(event)) {
                list.add(event);

                Collections.sort(list, new Comparator<PositionChangedEvent>() {
                    @Override
                    public int compare(PositionChangedEvent arg0, PositionChangedEvent arg1) {
                        int o2 = arg0.getPriority().ordinal();
                        int o1 = arg1.getPriority().ordinal();
                        return (o1 < o2 ? -1 : (o1 == o2 ? 0 : 1));
                    }
                });

            }
        }

    }

    public static void Remove(PositionChangedEvent event) {
        synchronized (list) {
            list.remove(event);
        }
    }

    public static long minPosEventTime = Long.MAX_VALUE;
    public static long minOrientationEventTime = Long.MAX_VALUE;

    public static long lastPosTime = 0;
    public static long lastOrientTime = 0;

    private static long lastPositionChanged = 0;

    private static long lastOrintationChangedEvent = 0;

    public static void PositionChanged() {
        minPosEventTime = Math.min(minPosEventTime, System.currentTimeMillis() - lastPosTime);
        lastPosTime = System.currentTimeMillis();

        if (lastPositionChanged != 0 && lastPositionChanged > System.currentTimeMillis() - Locator.getMinUpdateTime())
            return;
        lastPositionChanged = System.currentTimeMillis();

        synchronized (list) {
            try {
                for (PositionChangedEvent event : list) {
                    // If display is switched off fire only events with high priority!
                    if (Locator.isDisplayOff() && (event.getPriority() != PositionChangedEvent.Priority.High))
                        continue;
                    try {
                        event.PositionChanged();
                    } catch (Exception e) {
                        log.error("Core.PositionEventList.Call(location)" + event.getReceiverName(), e);
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    public static void OrientationChanged() {

        if (Locator.isDisplayOff())
            return; // Hier braucht niemand ein OriantationChangedEvent

        minOrientationEventTime = Math.min(minOrientationEventTime, System.currentTimeMillis() - lastOrientTime);
        lastOrientTime = System.currentTimeMillis();

        if (lastOrintationChangedEvent != 0 && lastOrintationChangedEvent > System.currentTimeMillis() - Locator.getMinUpdateTime())
            return;
        lastOrintationChangedEvent = System.currentTimeMillis();

        synchronized (list) {
            for (PositionChangedEvent event : list) {
                try {
                    event.OrientationChanged();
                } catch (Exception e) {
                    // TODO reactivate if possible Log.err(log, "Core.PositionEventList.Call(heading)", event.getReceiverName(), e);
                    e.printStackTrace();
                }
            }
        }
    }

    public static void SpeedChanged() {

        if (Locator.isDisplayOff())
            return; // Hier braucht niemand ein SpeedChangedEvent

        synchronized (list) {
            for (PositionChangedEvent event : list) {
                try {
                    event.SpeedChanged();
                } catch (Exception e) {
                    // TODO reactivate if possible Log.err(log, "Core.PositionEventList.Call(heading)", event.getReceiverName(), e);
                    e.printStackTrace();
                }
            }
        }

    }
}
