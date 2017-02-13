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
package de.longri.cachebox3.gui.events;


import de.longri.cachebox3.locator.events.GlobalLocationReceiver;
import de.longri.cachebox3.types.Cache;
import de.longri.cachebox3.types.Waypoint;

import java.util.ArrayList;

public class SelectedCacheEventList {
    public static ArrayList<SelectedCacheEvent> list = new ArrayList<SelectedCacheEvent>();

    public static void Add(SelectedCacheEvent event) {
        synchronized (list) {
            if (!list.contains(event))
                list.add(event);
        }
    }

    public static void Remove(SelectedCacheEvent event) {
        synchronized (list) {
            list.remove(event);
        }
    }

    private static Cache lastSelectedCache;
    private static Waypoint lastSelectedWayPoint;

    public static void Call(final Cache selectedCache, final Waypoint waypoint) {
        boolean change = true;

        if (lastSelectedCache != null) {
            if (lastSelectedCache.equals(selectedCache)) {
                if (lastSelectedWayPoint != null) {
                    if (lastSelectedWayPoint.equals(waypoint))
                        change = false;
                } else {
                    if (waypoint == null)
                        change = false;
                }
            }
        }

        if (change)
            GlobalLocationReceiver.resetApproach();

        if (selectChangeThread != null) {
            if (selectChangeThread.getState() != Thread.State.TERMINATED)
                return;
            else
                selectChangeThread = null;
        }

        if (selectedCache != null) {
            selectChangeThread = new Thread(new Runnable() {

                @Override
                public void run() {
                    synchronized (list) {
                        for (SelectedCacheEvent event : list) {
                            event.selectedCacheChanged(selectedCache, waypoint, lastSelectedCache, lastSelectedWayPoint);
                        }
                        lastSelectedCache = selectedCache;
                        lastSelectedWayPoint = waypoint;
                        // save last selected Cache in to DB
                        // nur beim Verlassen des Programms und DB-Wechsel
                        // Config.settings.LastSelectedCache.setValue(cache.GcCode);
                        // Config.AcceptChanges();
                    }
                }
            });

            selectChangeThread.start();
        }

    }

    static Thread selectChangeThread;
}
