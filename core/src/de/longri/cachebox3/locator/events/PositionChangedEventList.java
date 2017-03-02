/*
 * Copyright (C) 2016-2017 team-cachebox.de
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
package de.longri.cachebox3.locator.events;

import de.longri.cachebox3.locator.Locator;
import org.oscim.event.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;


public class PositionChangedEventList {
    final static Logger log = LoggerFactory.getLogger(PositionChangedEventList.class);
    private static final ArrayList<PositionChangedEvent> list = new ArrayList<PositionChangedEvent>();

    public static void add(PositionChangedEvent event) {
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

    public static void remove(PositionChangedEvent event) {
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

    public static void positionChanged(Event event) {
        minPosEventTime = Math.min(minPosEventTime, System.currentTimeMillis() - lastPosTime);
        lastPosTime = System.currentTimeMillis();

        if (lastPositionChanged != 0 && lastPositionChanged > System.currentTimeMillis() - Locator.getMinUpdateTime())
            return;
        lastPositionChanged = System.currentTimeMillis();

        synchronized (list) {
            try {
                for (PositionChangedEvent listener : list) {
                    // If display is switched off fire only events with high priority!
                    if (Locator.isDisplayOff() && (listener.getPriority() != PositionChangedEvent.Priority.High))
                        continue;
                    try {
                        listener.positionChanged(event);
                    } catch (Exception e) {
                        log.error("Core.PositionEventList.call(location)" + listener.getReceiverName(), e);
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    public static void orientationChanged(final Event event) {

        if (Locator.isDisplayOff())
            return; // Hier braucht niemand ein OriantationChangedEvent

        minOrientationEventTime = Math.min(minOrientationEventTime, System.currentTimeMillis() - lastOrientTime);
        lastOrientTime = System.currentTimeMillis();

        if (lastOrintationChangedEvent != 0 && lastOrintationChangedEvent > System.currentTimeMillis() - Locator.getMinUpdateTime())
            return;
        lastOrintationChangedEvent = System.currentTimeMillis();

        synchronized (list) {
            for (PositionChangedEvent listener : list) {
                try {
                    listener.orientationChanged(event);
                } catch (Exception e) {
                    // TODO reactivate if possible Log.err(log, "Core.PositionEventList.call(heading)", event.getReceiverName(), e);
                    e.printStackTrace();
                }
            }
        }
    }

    public static void speedChanged(Event event) {

        if (Locator.isDisplayOff())
            return; // Hier braucht niemand ein SpeedChangedEvent

        synchronized (list) {
            for (PositionChangedEvent listener : list) {
                try {
                    listener.speedChanged(event);
                } catch (Exception e) {
                    // TODO reactivate if possible Log.err(log, "Core.PositionEventList.call(heading)", event.getReceiverName(), e);
                    e.printStackTrace();
                }
            }
        }

    }
}
