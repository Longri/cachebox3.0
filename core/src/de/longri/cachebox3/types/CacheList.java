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
package de.longri.cachebox3.types;


import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Predicate;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.events.EventHandler;
import de.longri.cachebox3.locator.Coordinate;
import de.longri.cachebox3.sqlite.Database;
import de.longri.cachebox3.utils.MathUtils;
import de.longri.cachebox3.utils.NamedRunnable;

import java.util.Calendar;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.Iterator;

public class CacheList extends Array<AbstractCache> {

    public boolean ResortAtWork = false;
    private int unFilteredSize;

    public AbstractCache GetCacheByGcCode(String GcCode) {
        synchronized ((Object) this.items) { //must cast to Object otherwise it gives a classcastexception at runtime
            for (int i = 0, n = this.size; i < n; i++) {
                AbstractCache cache = this.get(i);
                if (cache.getGcCode().toString().equalsIgnoreCase(GcCode))
                    return cache;
            }
            return null;
        }
    }

    public AbstractCache GetCacheById(long cacheId) {
        synchronized ((Object) this.items) { //must cast to Object otherwise it gives a classcastexception at runtime  
            for (int i = 0, n = this.size; i < n; i++) {
                AbstractCache cache = this.get(i);
                if (cache.getId() == cacheId)
                    return cache;
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
     * @param selectedCoord
     * @param selected
     * @return
     */
    public CacheWithWP resort(Coordinate selectedCoord, CacheWithWP selected) {
        synchronized ((Object) this.items) { //must cast to Object otherwise it gives a classcastexception at runtime  
            if (selected == null) return null;
            CacheWithWP retValue = null;

            this.ResortAtWork = true;

            if (selectedCoord != null) {
                // sort after distance from selected Cache
                Coordinate fromPos = selectedCoord;
                // avoid "illegal waypoint"
                if (fromPos == null || (fromPos.getLatitude() == 0 && fromPos.getLongitude() == 0)) {
                    if (selected.getCache() == null) {
                        this.ResortAtWork = false;
                        return null;
                    }
                    fromPos = selected.getCache();
                }
                if (fromPos == null) {
                    this.ResortAtWork = false;
                    return retValue;
                }
                for (int i = 0, n = this.size; i < n; i++) {
                    AbstractCache abstractCache = this.get(i);
                    abstractCache.distance(MathUtils.CalculationType.FAST, true, fromPos);
                }
            } else {
                Coordinate myPos = EventHandler.getMyPosition();
                // refresh all distances
                if (myPos != null) {
                    int n = this.size;
                    while (n-- > 0) {
                        this.get(n).distance(MathUtils.CalculationType.FAST, true, myPos);
                    }
                }
            }

            this.sort();

            // Nächsten Cache auswählen
            if (this.size > 0) {
                AbstractCache nextAbstractCache = this.get(0); // or null ...
                for (int i = 0; i < this.size; i++) {
                    nextAbstractCache = this.get(i);
                    if (!nextAbstractCache.isArchived()) {
                        if (nextAbstractCache.isAvailable()) {
                            if (!nextAbstractCache.isFound()) {
                                if (!nextAbstractCache.ImTheOwner()) {
                                    if ((nextAbstractCache.getType() == CacheTypes.Event) || (nextAbstractCache.getType() == CacheTypes.MegaEvent) || (nextAbstractCache.getType() == CacheTypes.CITO) || (nextAbstractCache.getType() == CacheTypes.Giga)) {
                                        if (nextAbstractCache.getDateHidden() != null) {
                                            Calendar dateHidden = GregorianCalendar.getInstance();
                                            Calendar today = GregorianCalendar.getInstance();
                                            dateHidden.setTime(nextAbstractCache.getDateHidden());
                                            if (("" + today.get(Calendar.DAY_OF_MONTH) + today.get(Calendar.MONTH) + today.get(Calendar.YEAR))
                                                    .equals("" + dateHidden.get(Calendar.DAY_OF_MONTH) + dateHidden.get(Calendar.MONTH) + dateHidden.get(Calendar.YEAR))) {
                                                break;
                                            }
                                        }
                                    } else {
                                        if (nextAbstractCache.getType() != CacheTypes.Mystery) {
                                            break;
                                        } else {
                                            if (nextAbstractCache.hasCorrectedCoordinates()) {
                                                break;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                // Wenn der nachste Cache ein Mystery mit Final Waypoint ist
                // -> gleich den Final Waypoint auswahlen!!!
                // When the next Cache is a mystery with final waypoint
                // -> activate the final waypoint!!!
                AbstractWaypoint waypoint = nextAbstractCache.GetFinalWaypoint();
                if (waypoint == null) {
                    // wenn ein Cache keinen Final Waypoint hat dann wird überprüft, ob dieser einen Startpunkt definiert hat
                    // Wenn ein Cache einen Startpunkt definiert hat dann wird beim Selektieren dieses Caches gleich dieser Startpunkt
                    // selektiert
                    waypoint = nextAbstractCache.GetStartWaypoint();
                }

                retValue = new CacheWithWP(nextAbstractCache, waypoint);
            }
            // vorhandenen Parkplatz Cache nach oben schieben
            AbstractCache park = this.GetCacheByGcCode("CBPark");
            if (park != null) {
                int parkIndex = this.indexOf(park, false);
                AbstractCache parkCache = this.get(parkIndex);
                this.removeIndex(parkIndex);
                this.insert(0, parkCache);
            }

            // Cursor.Current = Cursors.Default;
            this.ResortAtWork = false;

            CB.postOnGlThread(new NamedRunnable("CacheList:Fire changed event") {
                @Override
                public void run() {
//             TODO   CacheListChangedEventList.Call();
                }
            });
            return retValue;
        }
    }

    /**
     * Removes all of the elements from this list. The list will be empty after this call returns.<br>
     * All Cache objects are disposed
     */
    @Override
    public void clear() {
        synchronized ((Object) this.items) { //must cast to Object otherwise it gives a classcastexception at runtime  
            for (int i = 0, n = this.size; i < n; i++) {
                AbstractCache cache = this.get(i);
                cache.dispose();
            }
            super.clear();
        }
    }

    public void clear(int newCapacity) {
        this.clear();
        this.resize(newCapacity);
    }

    public Array<String> getGcCodes() {
        synchronized ((Object) this.items) { //must cast to Object otherwise it gives a classcastexception at runtime  
            Array<String> list = new Array<>(this.size);
            for (int i = 0, n = this.size; i < n; i++) {
                list.add(this.get(i).getGcCode().toString());
            }
            return list;
        }
    }

    public void add(AbstractCache ca, boolean withoutLiveReplaceCheck) {
        synchronized ((Object) this.items) {
            if (withoutLiveReplaceCheck) {
                super.add(ca);
            } else {
                this.add(ca);
            }
        }
    }

    public void add(AbstractCache ca) {
        synchronized ((Object) this.items) { //must cast to Object otherwise it gives a classcastexception at runtime  
            if (ca == null)
                return;

            int index = -1;
            for (int i = 0, n = this.size; i < n; i++) {

                AbstractCache abstractCache = get(i);
                if (abstractCache.getId() == ca.getId()) {
                    index = i;
                }
            }

            if (index > -1) {
                // Replace LiveCache with Cache
                if (get(index).isLive()) {
                    if (!ca.isLive()) {
                        this.set(index, ca);
                        return;
                    }
                }
            }
            super.add(ca);
        }
    }


    //################## synchronised overrides ################################

//    public void MoveItemsLeft() {
//        synchronized ((Object) this.items) { //must cast to Object otherwise it gives a classcastexception at runtime
//            super.MoveItemsLeft();
//        }
//    }
//
//    public void MoveItemsRight() {
//        synchronized ((Object) this.items) { //must cast to Object otherwise it gives a classcastexception at runtime
//            super.MoveItemsRight();
//        }
//    }
//
//    public void MoveItemFirst(int index) {
//        synchronized ((Object) this.items) { //must cast to Object otherwise it gives a classcastexception at runtime
//            super.MoveItemFirst(index);
//        }
//
//    }
//
//    public void MoveItemLast(int index) {
//        synchronized ((Object) this.items) { //must cast to Object otherwise it gives a classcastexception at runtime
//            super.MoveItemLast(index);
//        }
//    }
//
//    public int MoveItem(int index, int Step) {
//        synchronized ((Object) this.items) { //must cast to Object otherwise it gives a classcastexception at runtime
//            return super.MoveItem(index, Step);
//        }
//    }
//
//    public void MoveItem(int index) {
//        synchronized ((Object) this.items) { //must cast to Object otherwise it gives a classcastexception at runtime
//            super.MoveItem(index);
//        }
//    }
//
//    public AbstractCache remove(int index) {
//        synchronized ((Object) this.items) { //must cast to Object otherwise it gives a classcastexception at runtime
//            return super.remove(index);
//        }
//    }

    public void addAll(Array<? extends AbstractCache> array) {
        synchronized ((Object) this.items) { //must cast to Object otherwise it gives a classcastexception at runtime
            super.addAll(array);
        }
    }

    public void addAll(Array<? extends AbstractCache> array, int start, int count) {
        synchronized ((Object) this.items) { //must cast to Object otherwise it gives a classcastexception at runtime
            super.addAll(array, start, count);
        }
    }

    public void addAll(AbstractCache... array) {
        synchronized ((Object) this.items) { //must cast to Object otherwise it gives a classcastexception at runtime
            super.addAll(array);
        }
    }

    public void addAll(AbstractCache[] array, int start, int count) {
        synchronized ((Object) this.items) { //must cast to Object otherwise it gives a classcastexception at runtime
            super.addAll(array, start, count);
        }
    }

    public AbstractCache get(int index) {
        synchronized ((Object) this.items) { //must cast to Object otherwise it gives a classcastexception at runtime
            return super.get(index);
        }
    }

    public void set(int index, AbstractCache value) {
        synchronized ((Object) this.items) { //must cast to Object otherwise it gives a classcastexception at runtime
            super.set(index, value);
        }
    }

    public void insert(int index, AbstractCache value) {
        synchronized ((Object) this.items) { //must cast to Object otherwise it gives a classcastexception at runtime
            super.insert(index, value);
        }
    }

    public void swap(int first, int second) {
        synchronized ((Object) this.items) { //must cast to Object otherwise it gives a classcastexception at runtime
            super.swap(first, second);
        }
    }

    public boolean contains(AbstractCache value, boolean identity) {
        synchronized ((Object) this.items) { //must cast to Object otherwise it gives a classcastexception at runtime
            return super.contains(value, identity);
        }
    }

    public int indexOf(AbstractCache value, boolean identity) {
        synchronized ((Object) this.items) { //must cast to Object otherwise it gives a classcastexception at runtime
            return super.indexOf(value, identity);
        }
    }

    public int lastIndexOf(AbstractCache value, boolean identity) {
        synchronized ((Object) this.items) { //must cast to Object otherwise it gives a classcastexception at runtime
            return super.lastIndexOf(value, identity);
        }
    }

    public boolean removeValue(AbstractCache value, boolean identity) {
        synchronized ((Object) this.items) { //must cast to Object otherwise it gives a classcastexception at runtime
            return super.removeValue(value, identity);
        }
    }

    public AbstractCache removeIndex(int index) {
        synchronized ((Object) this.items) { //must cast to Object otherwise it gives a classcastexception at runtime
            return super.removeIndex(index);
        }
    }

    public void removeRange(int start, int end) {
        synchronized ((Object) this.items) { //must cast to Object otherwise it gives a classcastexception at runtime
            super.removeRange(start, end);
        }
    }

    public boolean removeAll(Array<? extends AbstractCache> array, boolean identity) {
        synchronized ((Object) this.items) { //must cast to Object otherwise it gives a classcastexception at runtime
            return super.removeAll(array, identity);
        }
    }

    public AbstractCache pop() {
        synchronized ((Object) this.items) { //must cast to Object otherwise it gives a classcastexception at runtime
            return super.pop();
        }
    }

    public AbstractCache peek() {
        synchronized ((Object) this.items) { //must cast to Object otherwise it gives a classcastexception at runtime
            return super.peek();
        }
    }

    public AbstractCache first() {
        synchronized ((Object) this.items) { //must cast to Object otherwise it gives a classcastexception at runtime
            return super.first();
        }
    }


    public void sort() {
        synchronized ((Object) this.items) { //must cast to Object otherwise it gives a classcastexception at runtime
            super.sort(new Comparator<AbstractCache>() {
                @Override
                public int compare(AbstractCache o1, AbstractCache o2) {
                    return o1.getCachedDistance() - o2.getCachedDistance();
                }
            });
        }
    }

    public void sort(Comparator<? super AbstractCache> comparator) {
        synchronized ((Object) this.items) { //must cast to Object otherwise it gives a classcastexception at runtime
            super.sort(comparator);
        }
    }

    public AbstractCache selectRanked(Comparator<AbstractCache> comparator, int kthLowest) {
        synchronized ((Object) this.items) { //must cast to Object otherwise it gives a classcastexception at runtime
            return super.selectRanked(comparator, kthLowest);
        }
    }

    public int selectRankedIndex(Comparator<AbstractCache> comparator, int kthLowest) {
        synchronized ((Object) this.items) { //must cast to Object otherwise it gives a classcastexception at runtime
            return super.selectRankedIndex(comparator, kthLowest);
        }
    }

    public void reverse() {
        synchronized ((Object) this.items) { //must cast to Object otherwise it gives a classcastexception at runtime
            super.reverse();
        }
    }

    public void shuffle() {
        synchronized ((Object) this.items) { //must cast to Object otherwise it gives a classcastexception at runtime
            super.shuffle();
        }
    }

    public Iterator<AbstractCache> iterator() {
        synchronized ((Object) this.items) { //must cast to Object otherwise it gives a classcastexception at runtime
            return super.iterator();
        }
    }

    public Iterable<AbstractCache> select(Predicate<AbstractCache> predicate) {
        synchronized ((Object) this.items) { //must cast to Object otherwise it gives a classcastexception at runtime
            return super.select(predicate);
        }
    }

    public void truncate(int newSize) {
        synchronized ((Object) this.items) { //must cast to Object otherwise it gives a classcastexception at runtime
            super.truncate(newSize);
        }
    }

    public AbstractCache random() {
        synchronized ((Object) this.items) { //must cast to Object otherwise it gives a classcastexception at runtime
            return super.random();
        }
    }

    public AbstractCache[] toArray() {
        synchronized ((Object) this.items) { //must cast to Object otherwise it gives a classcastexception at runtime
            return super.toArray();
        }
    }

    public <V> V[] toArray(Class type) {
        synchronized ((Object) this.items) { //must cast to Object otherwise it gives a classcastexception at runtime
            return super.toArray(type);
        }
    }

    public int getSize() {
        synchronized ((Object) this.items) { //must cast to Object otherwise it gives a classcastexception at runtime
            return size;
        }
    }

    public AbstractCache getCacheById(long id) {
        synchronized ((Object) this.items) { //must cast to Object otherwise it gives a classcastexception at runtime
            Object[] items = this.items;
            for (int i = 0, n = size; i < n; i++)
                if (((AbstractCache) items[i]).getId() == id) return get(i);
            return null;
        }
    }

    public void setUnfilteredSize(int count) {
        this.unFilteredSize = count;
    }

    public int getUnFilteredSize() {
        return this.unFilteredSize;
    }
}
