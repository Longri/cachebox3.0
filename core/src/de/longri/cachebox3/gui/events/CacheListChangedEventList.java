/*
 * Copyright (C) 2014 - 2017 team-cachebox.de
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


import de.longri.cachebox3.events.EventHandler;
import de.longri.cachebox3.settings.Config;
import de.longri.cachebox3.sqlite.Database;
import de.longri.cachebox3.types.AbstractCache;
import de.longri.cachebox3.types.CacheTypes;
import de.longri.cachebox3.types.MutableCache;

import java.util.ArrayList;

/**
 * @author Longri
 */
public class CacheListChangedEventList {
    public static ArrayList<CacheListChangedEventListener> list = new ArrayList<CacheListChangedEventListener>();

    public static void Add(CacheListChangedEventListener event) {
        synchronized (list) {
            if (!list.contains(event))
                list.add(event);
        }
    }

    public static void Remove(CacheListChangedEventListener event) {
        synchronized (list) {
            list.remove(event);
        }
    }

    private static Thread threadCall;

    public static void Call() {
        if (Database.Data == null | Database.Data.Query == null) return;
        AbstractCache abstractCache = Database.Data.Query.GetCacheByGcCode("CBPark");

        if (abstractCache != null)
            Database.Data.Query.removeValue(abstractCache, false);

        // add Parking Cache
        if (Config.ParkingLatitude.getValue() != 0) {
            abstractCache = new MutableCache(Config.ParkingLatitude.getValue(), Config.ParkingLongitude.getValue(), "My Parking area", CacheTypes.MyParking, "CBPark");
            Database.Data.Query.insert(0, abstractCache);
        }


        //if selected Cache not into Query, reset selected Cache
        AbstractCache selectedCache = EventHandler.getSelectedCache();
        if (selectedCache != null) {
            AbstractCache selectedInQuery = Database.Data.Query.GetCacheById(selectedCache.getId());
            if (selectedInQuery == null) {
                //reset
                EventHandler.setSelectedWaypoint(null, null);
            }
        }


        //TODO add Live Caches
//            // add all Live Caches
//            for (int i = 0; i < LiveMapQue.LiveCaches.getSize(); i++) {
//                if (FilterInstances.isLastFilterSet()) {
//                    Cache ca = LiveMapQue.LiveCaches.get(i);
//                    if (ca == null)
//                        continue;
//                    if (!Database.Data.Query.contains(ca)) {
//                        if (FilterInstances.getLastFilter().passed(ca)) {
//                            ca.setLive(true);
//                            Database.Data.Query.add(ca);
//                        }
//                    }
//                } else {
//                    Cache ca = LiveMapQue.LiveCaches.get(i);
//                    if (ca == null)
//                        continue;
//                    if (!Database.Data.Query.contains(ca)) {
//                        ca.setLive(true);
//                        Database.Data.Query.add(ca);
//                    }
//                }
//            }


        if (threadCall != null) {
            if (threadCall.getState() != Thread.State.TERMINATED)
                return;
            else
                threadCall = null;
        }

        if (threadCall == null)
            threadCall = new Thread(new Runnable() {

                @Override
                public void run() {
                    synchronized (list) {
                        for (CacheListChangedEventListener event : list) {
                            if (event == null)
                                continue;
                            event.CacheListChangedEvent();
                        }
                    }

                }
            });

        threadCall.start();
    }

}
