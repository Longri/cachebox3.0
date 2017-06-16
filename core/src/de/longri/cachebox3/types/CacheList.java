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


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Predicate;
import com.badlogic.gdx.utils.Select;
import com.badlogic.gdx.utils.Sort;
import de.longri.cachebox3.events.EventHandler;
import de.longri.cachebox3.locator.Coordinate;
import de.longri.cachebox3.utils.MathUtils;
import de.longri.cachebox3.utils.MoveableList;

import java.util.Calendar;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.Iterator;

public class CacheList extends MoveableList<Cache> {

    public boolean ResortAtWork = false;

    public Cache GetCacheByGcCode(String GcCode) {
        synchronized ((Object) this.items) { //must cast to Object otherwise it gives a classcastexception at runtime
            for (int i = 0, n = this.size; i < n; i++) {
                Cache cache = this.get(i);
                if (cache.getGcCode().equalsIgnoreCase(GcCode))
                    return cache;
            }
            return null;
        }
    }

    public Cache GetCacheById(long cacheId) {
        synchronized ((Object) this.items) { //must cast to Object otherwise it gives a classcastexception at runtime  
            for (int i = 0, n = this.size; i < n; i++) {
                Cache cache = this.get(i);
                if (cache.Id == cacheId)
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
    public CacheWithWP Resort(Coordinate selectedCoord, CacheWithWP selected) {
        synchronized ((Object) this.items) { //must cast to Object otherwise it gives a classcastexception at runtime  
            if (selected == null) return null;
            CacheWithWP retValue = null;

            this.ResortAtWork = true;
            boolean LocatorValid = EventHandler.getSelectedCoord() != null;
            // Alle Distanzen aktualisieren
            if (LocatorValid) {
                for (int i = 0, n = this.size; i < n; i++) {
                    Cache cache = this.get(i);
                    cache.Distance(MathUtils.CalculationType.FAST, true);
                }
            } else {
                // sort after distance from selected Cache
                Coordinate fromPos = selectedCoord;
                // avoid "illegal waypoint"
                if (fromPos == null || (fromPos.getLatitude() == 0 && fromPos.getLongitude() == 0)) {
                    if (selected.getCache() == null) return null;
                    fromPos = selected.getCache();
                }
                if (fromPos == null) {
                    this.ResortAtWork = false;
                    return retValue;
                }
                for (int i = 0, n = this.size; i < n; i++) {
                    Cache cache = this.get(i);
                    cache.Distance(MathUtils.CalculationType.FAST, true, fromPos);
                }
            }

            this.sort();

            // Nächsten Cache auswählen
            if (this.size > 0) {
                Cache nextCache = this.get(0); // or null ...
                for (int i = 0; i < this.size; i++) {
                    nextCache = this.get(i);
                    if (!nextCache.isArchived()) {
                        if (nextCache.isAvailable()) {
                            if (!nextCache.isFound())
                            // eigentlich wenn has_fieldnote(found,DNF,Maint,SBA, aber note vielleicht nicht)
                            {
                                if (!nextCache.ImTheOwner()) {
                                    if ((nextCache.Type == CacheTypes.Event) || (nextCache.Type == CacheTypes.MegaEvent) || (nextCache.Type == CacheTypes.CITO) || (nextCache.Type == CacheTypes.Giga)) {
                                        Calendar dateHidden = GregorianCalendar.getInstance();
                                        Calendar today = GregorianCalendar.getInstance();
                                        dateHidden.setTime(nextCache.getDateHidden());
                                        if (("" + today.get(Calendar.DAY_OF_MONTH) + today.get(Calendar.MONTH) + today.get(Calendar.YEAR))
                                                .equals("" + dateHidden.get(Calendar.DAY_OF_MONTH) + dateHidden.get(Calendar.MONTH) + dateHidden.get(Calendar.YEAR))) {
                                            break;
                                        }
                                    } else {
                                        if (nextCache.Type != CacheTypes.Mystery) {
                                            break;
                                        } else {
                                            if (nextCache.CorrectedCoordiantesOrMysterySolved()) {
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
                Waypoint waypoint = nextCache.GetFinalWaypoint();
                if (waypoint == null) {
                    // wenn ein Cache keinen Final Waypoint hat dann wird überprüft, ob dieser einen Startpunkt definiert hat
                    // Wenn ein Cache einen Startpunkt definiert hat dann wird beim Selektieren dieses Caches gleich dieser Startpunkt
                    // selektiert
                    waypoint = nextCache.GetStartWaypoint();
                }

                retValue = new CacheWithWP(nextCache, waypoint);
            }
            // vorhandenen Parkplatz Cache nach oben schieben
            Cache park = this.GetCacheByGcCode("CBPark");
            if (park != null) {
                this.MoveItemFirst(this.indexOf(park, false));
            }

            // Cursor.Current = Cursors.Default;
            this.ResortAtWork = false;

            Gdx.app.postRunnable(new Runnable() {
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
                Cache cache = this.get(i);
                if (!cache.isLive())
                    cache.dispose(); // don't dispose LiveCaches
            }
            super.clear();
        }
    }

    public Array<String> getGcCodes() {
        synchronized ((Object) this.items) { //must cast to Object otherwise it gives a classcastexception at runtime  
            Array<String> list = new Array<>(this.size);
            for (int i = 0, n = this.size; i < n; i++) {
                list.add(this.get(i).getGcCode());
            }
            return list;
        }
    }


    public void add(Cache ca) {
        synchronized ((Object) this.items) { //must cast to Object otherwise it gives a classcastexception at runtime  
            if (ca == null)
                return;

            int index = -1;
            for (int i = 0, n = this.size; i < n; i++) {

                Cache cache = get(i);
                if (cache.Id == ca.Id) {
                    index = i;
                }
            }

            if (index > -1) {
                // Replace LiveCache with Cache
                if (get(index).isLive()) {
                    if (!ca.isLive()) {
                        this.replace(ca, index);
                        return;
                    }
                }
            }
            super.add(ca);
        }
    }


    //################## synchronised overrides ################################

    public void MoveItemsLeft() {
        synchronized ((Object) this.items) { //must cast to Object otherwise it gives a classcastexception at runtime  
            super.MoveItemsLeft();
        }
    }

    public void MoveItemsRight() {
        synchronized ((Object) this.items) { //must cast to Object otherwise it gives a classcastexception at runtime  
            super.MoveItemsRight();
        }
    }

    public void MoveItemFirst(int index) {
        synchronized ((Object) this.items) { //must cast to Object otherwise it gives a classcastexception at runtime  
            super.MoveItemFirst(index);
        }

    }

    public void MoveItemLast(int index) {
        synchronized ((Object) this.items) { //must cast to Object otherwise it gives a classcastexception at runtime  
            super.MoveItemLast(index);
        }
    }

    public int MoveItem(int index, int Step) {
        synchronized ((Object) this.items) { //must cast to Object otherwise it gives a classcastexception at runtime  
            return super.MoveItem(index, Step);
        }
    }

    public void MoveItem(int index) {
        synchronized ((Object) this.items) { //must cast to Object otherwise it gives a classcastexception at runtime  
            super.MoveItem(index);
        }
    }

    public Cache remove(int index) {
        synchronized ((Object) this.items) { //must cast to Object otherwise it gives a classcastexception at runtime
            return super.remove(index);
        }
    }

    public void addAll(Array<? extends Cache> array) {
        synchronized ((Object) this.items) { //must cast to Object otherwise it gives a classcastexception at runtime
            super.addAll(array);
        }
    }

    public void addAll(Array<? extends Cache> array, int start, int count) {
        synchronized ((Object) this.items) { //must cast to Object otherwise it gives a classcastexception at runtime
            super.addAll(array, start, count);
        }
    }

    public void addAll(Cache... array) {
        synchronized ((Object) this.items) { //must cast to Object otherwise it gives a classcastexception at runtime
            super.addAll(array);
        }
    }

    public void addAll(Cache[] array, int start, int count) {
        synchronized ((Object) this.items) { //must cast to Object otherwise it gives a classcastexception at runtime
            super.addAll(array, start, count);
        }
    }

    public Cache get(int index) {
        synchronized ((Object) this.items) { //must cast to Object otherwise it gives a classcastexception at runtime
            return super.get(index);
        }
    }

    public void set(int index, Cache value) {
        synchronized ((Object) this.items) { //must cast to Object otherwise it gives a classcastexception at runtime
            super.set(index, value);
        }
    }

    public void insert(int index, Cache value) {
        synchronized ((Object) this.items) { //must cast to Object otherwise it gives a classcastexception at runtime
            super.insert(index, value);
        }
    }

    public void swap(int first, int second) {
        synchronized ((Object) this.items) { //must cast to Object otherwise it gives a classcastexception at runtime
            super.swap(first, second);
        }
    }

    public boolean contains(Cache value, boolean identity) {
        synchronized ((Object) this.items) { //must cast to Object otherwise it gives a classcastexception at runtime
            return super.contains(value, identity);
        }
    }

    public int indexOf(Cache value, boolean identity) {
        synchronized ((Object) this.items) { //must cast to Object otherwise it gives a classcastexception at runtime
            return super.indexOf(value, identity);
        }
    }

    public int lastIndexOf(Cache value, boolean identity) {
        synchronized ((Object) this.items) { //must cast to Object otherwise it gives a classcastexception at runtime
            return super.lastIndexOf(value, identity);
        }
    }

    public boolean removeValue(Cache value, boolean identity) {
        synchronized ((Object) this.items) { //must cast to Object otherwise it gives a classcastexception at runtime
            return super.removeValue(value, identity);
        }
    }

    public Cache removeIndex(int index) {
        synchronized ((Object) this.items) { //must cast to Object otherwise it gives a classcastexception at runtime
            return super.removeIndex(index);
        }
    }

    public void removeRange(int start, int end) {
        synchronized ((Object) this.items) { //must cast to Object otherwise it gives a classcastexception at runtime
            super.removeRange(start, end);
        }
    }

    public boolean removeAll(Array<? extends Cache> array, boolean identity) {
        synchronized ((Object) this.items) { //must cast to Object otherwise it gives a classcastexception at runtime
            return super.removeAll(array, identity);
        }
    }

    public Cache pop() {
        synchronized ((Object) this.items) { //must cast to Object otherwise it gives a classcastexception at runtime
            return super.pop();
        }
    }

    public Cache peek() {
        synchronized ((Object) this.items) { //must cast to Object otherwise it gives a classcastexception at runtime
            return super.peek();
        }
    }

    public Cache first() {
        synchronized ((Object) this.items) { //must cast to Object otherwise it gives a classcastexception at runtime
            return super.first();
        }
    }

//    public Cache[] shrink() {
//        synchronized ((Object) this.items) { //must cast to Object otherwise it gives a classcastexception at runtime
//            return super.shrink();
//        }
//    }
//
//    public Cache[] ensureCapacity(int additionalCapacity) {
//        synchronized ((Object) this.items) { //must cast to Object otherwise it gives a classcastexception at runtime
//            return super.ensureCapacity(additionalCapacity);
//        }
//    }
//
//    public Cache[] setSize(int newSize) {
//        synchronized ((Object) this.items) { //must cast to Object otherwise it gives a classcastexception at runtime
//            return super.setSize(newSize);
//        }
//    }
//
//    protected Cache[] resize(int newSize) {
//        synchronized ((Object) this.items) { //must cast to Object otherwise it gives a classcastexception at runtime
//            return super.resize(newSize);
//        }
//    }

    public void sort() {
        synchronized ((Object) this.items) { //must cast to Object otherwise it gives a classcastexception at runtime
            super.sort();
        }
    }

    public void sort(Comparator<? super Cache> comparator) {
        synchronized ((Object) this.items) { //must cast to Object otherwise it gives a classcastexception at runtime
            super.sort(comparator);
        }
    }

    public Cache selectRanked(Comparator<Cache> comparator, int kthLowest) {
        synchronized ((Object) this.items) { //must cast to Object otherwise it gives a classcastexception at runtime
            return super.selectRanked(comparator, kthLowest);
        }
    }

    public int selectRankedIndex(Comparator<Cache> comparator, int kthLowest) {
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

    public Iterator<Cache> iterator() {
        synchronized ((Object) this.items) { //must cast to Object otherwise it gives a classcastexception at runtime
            return super.iterator();
        }
    }

    public Iterable<Cache> select(Predicate<Cache> predicate) {
        synchronized ((Object) this.items) { //must cast to Object otherwise it gives a classcastexception at runtime
            return super.select(predicate);
        }
    }

    public void truncate(int newSize) {
        synchronized ((Object) this.items) { //must cast to Object otherwise it gives a classcastexception at runtime
            super.truncate(newSize);
        }
    }

    public Cache random() {
        synchronized ((Object) this.items) { //must cast to Object otherwise it gives a classcastexception at runtime
            return super.random();
        }
    }

    public Cache[] toArray() {
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
}
