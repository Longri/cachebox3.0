/*
 * Copyright (C) 2014 - 2020 team-cachebox.de
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
package de.longri.cachebox3.types;


import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Predicate;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.events.CacheListChangedEvent;
import de.longri.cachebox3.events.DistanceChangedEvent;
import de.longri.cachebox3.events.DistanceChangedListener;
import de.longri.cachebox3.events.EventHandler;
import de.longri.cachebox3.locator.Coordinate;
import de.longri.cachebox3.utils.MathUtils;
import de.longri.cachebox3.utils.NamedRunnable;

import java.util.Calendar;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.Iterator;

public class CacheList extends Array<AbstractCache> implements DistanceChangedListener {

    public boolean resortAtWork = false;
    private int unFilteredSize;

    public AbstractCache getCacheByGcCode(String GcCode) {
        synchronized ((Object) items) { //must cast to Object otherwise it gives a classcastexception at runtime
            for (int i = 0, n = size; i < n; i++) {
                AbstractCache cache = get(i);
                if (cache.getGeoCacheCode().toString().equalsIgnoreCase(GcCode))
                    return cache;
            }
            return null;
        }
    }

    public AbstractCache getCacheById(long id) {
        synchronized ((Object) items) { //must cast to Object otherwise it gives a classcastexception at runtime
            for (int i = 0, n = size; i < n; i++) {
                AbstractCache cache = get(i);
                if (cache.getId() == id) return cache;
            }
            return null;
        }
    }

    /**
     * @param selectedCoord
     *            CB.getSelectedCoord()
     * @param selected
     *            new CacheWithWp(CB.getSelectedCache(),CB.getSelectedWP())
     * @param userName
     *            Config.settings.GcLogin.getValue()
     * @param ParkingLatitude
     *            Config.settings.ParkingLatitude.getValue()
     * @param ParkingLongitude
     *            Config.settings.ParkingLongitude.getValue()
     * @param DisplayOff
     *            Energy.DisplayOff()
     * @return CacheWithWP [null posible] set To<br>
     *         CB.setSelectedWaypoint(nextCache, waypoint, false);<br>
     *         CB.NearestCache(nextCache);
     */

    /**
     * @param selectedCoord ?
     * @param selectedCache ?
     */
    public void resort(Coordinate selectedCoord, CacheWithWP selectedCache) {
        synchronized (this) {
            if (resortAtWork) return;
            resortAtWork = true;
            CacheWithWP retValue = null;
            if (selectedCoord != null) {
                if (selectedCache == null) return;
                // sort by distance from selected Cache
                Coordinate fromPos = selectedCoord;
                // avoid "illegal waypoint"
                if (fromPos.getLatitude() == 0 && fromPos.getLongitude() == 0) {
                    if (selectedCache.getCache() == null) {
                        resortAtWork = false;
                        return;
                    }
                    fromPos = selectedCache.getCache();
                }
                if (fromPos == null) {
                    resortAtWork = false;
                    return;
                }
                for (int i = 0, n = size; i < n; i++) {
                    AbstractCache abstractCache = get(i);
                    abstractCache.distance(MathUtils.CalculationType.FAST, true, fromPos);
                }
            } else {
                Coordinate myPos = EventHandler.getMyPosition();
                // refresh all distances
                if (myPos != null) {
                    int n = size;
                    while (n-- > 0) {
                        get(n).distance(MathUtils.CalculationType.FAST, true, myPos);
                    }
                }
            }

            sort();

            // Nächsten Cache auswählen
            if (size > 0) {
                AbstractCache nextCache = null;
                for (int i = 0; i < size; i++) {
                    nextCache = get(i);
                    if (!nextCache.isArchived()) {
                        if (nextCache.isAvailable()) {
                            if (!nextCache.isFound()) {
                                if (!nextCache.iAmTheOwner()) {
                                    if (nextCache.isEvent()) {
                                        if (nextCache.getDateHidden() != null) {
                                            Calendar dateHidden = GregorianCalendar.getInstance();
                                            Calendar today = GregorianCalendar.getInstance();
                                            dateHidden.setTime(nextCache.getDateHidden());
                                            if (("" + today.get(Calendar.DAY_OF_MONTH) + today.get(Calendar.MONTH) + today.get(Calendar.YEAR))
                                                    .equals("" + dateHidden.get(Calendar.DAY_OF_MONTH) + dateHidden.get(Calendar.MONTH) + dateHidden.get(Calendar.YEAR))) {
                                                break;
                                            }
                                        }
                                    } else {
                                        if (nextCache.getType() == CacheTypes.Mystery) {
                                            if (nextCache.hasCorrectedCoordinatesOrHasCorrectedFinal()) {
                                                break;
                                            }
                                        } else {
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                // When the next Cache is a mystery with final waypoint -> activate the final waypoint!!!
                AbstractWaypoint waypoint = nextCache.getFinalWaypoint();
                if (waypoint == null) {
                    // wenn ein Cache keinen Final Waypoint hat dann wird überprüft, ob dieser einen Startpunkt definiert hat
                    // Wenn ein Cache einen Startpunkt definiert hat dann wird beim Selektieren dieses Caches gleich dieser Startpunkt
                    // selektiert
                    waypoint = nextCache.getStartWaypoint();
                }
                retValue = new CacheWithWP(nextCache, waypoint);
            }
            // remembered parking place is shown first in list
            AbstractCache park = getCacheByGcCode("CBPark");
            if (park != null) {
                int parkIndex = indexOf(park, false);
                AbstractCache parkCache = get(parkIndex);
                removeIndex(parkIndex);
                insert(0, parkCache);
            }
            resortAtWork = false;
            if (retValue != null) {
                final CacheWithWP newSelected = retValue;
                CB.postOnGlThread(new NamedRunnable("CacheList:Fire changed event") {
                    @Override
                    public void run() {
                        EventHandler.fireSelectedWaypointChanged(newSelected.getCache(), newSelected.getWaypoint());
                        CB.setNearestCache(newSelected.getCache());
                        EventHandler.fire(new CacheListChangedEvent());
                    }
                });
            }
        }
    }

    /**
     * Removes all of the elements from this list. The list will be empty after this call returns.<br>
     * All Cache objects are disposed
     */
    @Override
    public void clear() {
        synchronized ((Object) items) { //must cast to Object otherwise it gives a classcastexception at runtime
            for (int i = 0, n = size; i < n; i++) {
                AbstractCache cache = get(i);
                cache.dispose();
            }
            super.clear();
        }
    }

    public void clear(int newCapacity) {
        clear();
        resize(newCapacity);
    }

    public Array<String> getGcCodes() {
        synchronized ((Object) items) { //must cast to Object otherwise it gives a classcastexception at runtime
            Array<String> list = new Array<>(size);
            for (int i = 0, n = size; i < n; i++) {
                list.add(get(i).getGeoCacheCode().toString());
            }
            return list;
        }
    }

    public void add(AbstractCache ca, boolean withoutLiveReplaceCheck) {
        synchronized ((Object) items) {
            if (withoutLiveReplaceCheck) {
                super.add(ca);
            } else {
                add(ca);
            }
        }
    }

    public void add(AbstractCache ca) {
        synchronized ((Object) items) { //must cast to Object otherwise it gives a classcastexception at runtime
            if (ca == null)
                return;

            int index = -1;
            for (int i = 0, n = size; i < n; i++) {

                AbstractCache abstractCache = get(i);
                if (abstractCache.getId() == ca.getId()) {
                    index = i;
                }
            }

            if (index > -1) {
                // Replace LiveCache with Cache
                if (get(index).isLive()) {
                    if (!ca.isLive()) {
                        set(index, ca);
                        return;
                    }
                }
            }
            super.add(ca);
        }
    }


    //################## synchronised overrides ################################

//    public void MoveItemsLeft() {
//        synchronized ((Object) items) { //must cast to Object otherwise it gives a classcastexception at runtime
//            super.MoveItemsLeft();
//        }
//    }
//
//    public void MoveItemsRight() {
//        synchronized ((Object) items) { //must cast to Object otherwise it gives a classcastexception at runtime
//            super.MoveItemsRight();
//        }
//    }
//
//    public void MoveItemFirst(int index) {
//        synchronized ((Object) items) { //must cast to Object otherwise it gives a classcastexception at runtime
//            super.MoveItemFirst(index);
//        }
//
//    }
//
//    public void MoveItemLast(int index) {
//        synchronized ((Object) items) { //must cast to Object otherwise it gives a classcastexception at runtime
//            super.MoveItemLast(index);
//        }
//    }
//
//    public int MoveItem(int index, int Step) {
//        synchronized ((Object) items) { //must cast to Object otherwise it gives a classcastexception at runtime
//            return super.MoveItem(index, Step);
//        }
//    }
//
//    public void MoveItem(int index) {
//        synchronized ((Object) items) { //must cast to Object otherwise it gives a classcastexception at runtime
//            super.MoveItem(index);
//        }
//    }
//
//    public AbstractCache remove(int index) {
//        synchronized ((Object) items) { //must cast to Object otherwise it gives a classcastexception at runtime
//            return super.remove(index);
//        }
//    }

    public void addAll(Array<? extends AbstractCache> array) {
        synchronized ((Object) items) { //must cast to Object otherwise it gives a classcastexception at runtime
            super.addAll(array);
        }
    }

    public void addAll(Array<? extends AbstractCache> array, int start, int count) {
        synchronized ((Object) items) { //must cast to Object otherwise it gives a classcastexception at runtime
            super.addAll(array, start, count);
        }
    }

    public void addAll(AbstractCache... array) {
        synchronized ((Object) items) { //must cast to Object otherwise it gives a classcastexception at runtime
            super.addAll(array);
        }
    }

    public void addAll(AbstractCache[] array, int start, int count) {
        synchronized ((Object) items) { //must cast to Object otherwise it gives a classcastexception at runtime
            super.addAll(array, start, count);
        }
    }

    public AbstractCache get(int index) {
        synchronized ((Object) items) { //must cast to Object otherwise it gives a classcastexception at runtime
            return super.get(index);
        }
    }

    public void set(int index, AbstractCache value) {
        synchronized ((Object) items) { //must cast to Object otherwise it gives a classcastexception at runtime
            super.set(index, value);
        }
    }

    public void insert(int index, AbstractCache value) {
        synchronized ((Object) items) { //must cast to Object otherwise it gives a classcastexception at runtime
            super.insert(index, value);
        }
    }

    public void swap(int first, int second) {
        synchronized ((Object) items) { //must cast to Object otherwise it gives a classcastexception at runtime
            super.swap(first, second);
        }
    }

    public boolean contains(AbstractCache value, boolean identity) {
        synchronized ((Object) items) { //must cast to Object otherwise it gives a classcastexception at runtime
            return super.contains(value, identity);
        }
    }

    public int indexOf(AbstractCache value, boolean identity) {
        synchronized ((Object) items) { //must cast to Object otherwise it gives a classcastexception at runtime
            return super.indexOf(value, identity);
        }
    }

    public int lastIndexOf(AbstractCache value, boolean identity) {
        synchronized ((Object) items) { //must cast to Object otherwise it gives a classcastexception at runtime
            return super.lastIndexOf(value, identity);
        }
    }

    public boolean removeValue(AbstractCache value, boolean identity) {
        synchronized ((Object) items) { //must cast to Object otherwise it gives a classcastexception at runtime
            return super.removeValue(value, identity);
        }
    }

    public AbstractCache removeIndex(int index) {
        synchronized ((Object) items) { //must cast to Object otherwise it gives a classcastexception at runtime
            return super.removeIndex(index);
        }
    }

    public void removeRange(int start, int end) {
        synchronized ((Object) items) { //must cast to Object otherwise it gives a classcastexception at runtime
            super.removeRange(start, end);
        }
    }

    public boolean removeAll(Array<? extends AbstractCache> array, boolean identity) {
        synchronized ((Object) items) { //must cast to Object otherwise it gives a classcastexception at runtime
            return super.removeAll(array, identity);
        }
    }

    public AbstractCache pop() {
        synchronized ((Object) items) { //must cast to Object otherwise it gives a classcastexception at runtime
            return super.pop();
        }
    }

    public AbstractCache peek() {
        synchronized ((Object) items) { //must cast to Object otherwise it gives a classcastexception at runtime
            return super.peek();
        }
    }

    public AbstractCache first() {
        synchronized ((Object) items) { //must cast to Object otherwise it gives a classcastexception at runtime
            return super.first();
        }
    }


    public void sort() {
        synchronized ((Object) items) { //must cast to Object otherwise it gives a classcastexception at runtime
            super.sort(new Comparator<AbstractCache>() {
                @Override
                public int compare(AbstractCache o1, AbstractCache o2) {
                    return o1.getCachedDistance() - o2.getCachedDistance();
                }
            });
        }
    }

    public void sort(Comparator<? super AbstractCache> comparator) {
        synchronized ((Object) items) { //must cast to Object otherwise it gives a classcastexception at runtime
            super.sort(comparator);
        }
    }

    public AbstractCache selectRanked(Comparator<AbstractCache> comparator, int kthLowest) {
        synchronized ((Object) items) { //must cast to Object otherwise it gives a classcastexception at runtime
            return super.selectRanked(comparator, kthLowest);
        }
    }

    public int selectRankedIndex(Comparator<AbstractCache> comparator, int kthLowest) {
        synchronized ((Object) items) { //must cast to Object otherwise it gives a classcastexception at runtime
            return super.selectRankedIndex(comparator, kthLowest);
        }
    }

    public void reverse() {
        synchronized ((Object) items) { //must cast to Object otherwise it gives a classcastexception at runtime
            super.reverse();
        }
    }

    public void shuffle() {
        synchronized ((Object) items) { //must cast to Object otherwise it gives a classcastexception at runtime
            super.shuffle();
        }
    }

    public Iterator<AbstractCache> iterator() {
        synchronized ((Object) items) { //must cast to Object otherwise it gives a classcastexception at runtime
            return super.iterator();
        }
    }

    public Iterable<AbstractCache> select(Predicate<AbstractCache> predicate) {
        synchronized ((Object) items) { //must cast to Object otherwise it gives a classcastexception at runtime
            return super.select(predicate);
        }
    }

    public void truncate(int newSize) {
        synchronized ((Object) items) { //must cast to Object otherwise it gives a classcastexception at runtime
            super.truncate(newSize);
        }
    }

    public AbstractCache random() {
        synchronized ((Object) items) { //must cast to Object otherwise it gives a classcastexception at runtime
            return super.random();
        }
    }

    public AbstractCache[] toArray() {
        synchronized ((Object) items) { //must cast to Object otherwise it gives a classcastexception at runtime
            return super.toArray();
        }
    }

    public int getSize() {
        synchronized ((Object) items) { //must cast to Object otherwise it gives a classcastexception at runtime
            return size;
        }
    }

    public void setUnfilteredSize(int count) {
        unFilteredSize = count;
    }

    public int getUnFilteredSize() {
        return unFilteredSize;
    }

    @Override
    public void distanceChanged(DistanceChangedEvent event) {
        resort(null, null);
    }
}
