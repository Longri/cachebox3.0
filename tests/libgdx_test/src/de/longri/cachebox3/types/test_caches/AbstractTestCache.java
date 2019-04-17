/*
 * Copyright (C) 2018 team-cachebox.de
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
package de.longri.cachebox3.types.test_caches;

import com.badlogic.gdx.utils.Array;
import de.longri.cachebox3.sqlite.Database;
import de.longri.cachebox3.types.*;
import de.longri.cachebox3.utils.CharSequenceUtil;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;

import static de.longri.cachebox3.platform_test.Assert.assertEquals;
import static de.longri.cachebox3.platform_test.Assert.assertTrue;
import de.longri.cachebox3.platform_test.PlatformAssertionError;

/**
 * Created by Longri on 31.03.18.
 */
public abstract class AbstractTestCache {
    final SimpleDateFormat DATE_PATTERN = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());

    private final boolean testWaypoints, testLogs;

    protected AbstractTestCache() {
        setValues();
        this.id = AbstractCache.GenerateCacheId(this.gcCode);
        testWaypoints = addWaypoints();
        boolean lt = false;
        try {
            lt = addLogs();
        } catch (ParseException e) {
            e.printStackTrace();
            lt = false;
        }
        if (lt) {
            // BBCODE are filtered with read from DB
            for (LogEntry entry : logEntries) {
                entry.Comment = LogEntry.filterBBCode(entry.Comment);
            }
        }
        testLogs = lt;
    }

    protected abstract void setValues();

    protected abstract boolean addWaypoints();

    protected abstract boolean addLogs() throws ParseException;


    double longitude;
    double latitude;
    CacheTypes cacheType;
    String gcCode;
    String name;
    long id;
    boolean available;
    boolean archived;
    String placed_by;
    String owner;
    CacheSizes container;
    ArrayList<Attributes> positiveList = new ArrayList<>();
    ArrayList<Attributes> negativeList = new ArrayList<>();
    String url;
    float difficulty;
    float terrain;
    String country;
    String state;
    String shortDescription;
    String longDescription;
    String hint;
    protected boolean found;
    protected boolean favorite;
    Date dateHidden;
    Array<AbstractWaypoint> waypoints = new Array<>();
    Array<LogEntry> logEntries = new Array<>();
    int tbCount;
    int favoritePoints;
    String note;
    String solver;


    public void assertCache(AbstractCache other, Database database) throws PlatformAssertionError {
        assertTrue(other != null, "Cache must not be NULL");
        assertTrue(latitude == other.getLatitude(), "Latitude must be " + latitude + " but was :" + other.getLatitude());
        assertTrue(longitude == other.getLongitude(), "Longitude must be " + longitude + " but was :" + other.getLongitude());
        assertTrue(cacheType == other.getType(), "Cache type must be " + cacheType + " but was :" + other.getType());
        assertTrue(CharSequenceUtil.equals(gcCode, other.getGcCode()), "GcCode must be " + gcCode + " but was :" + other.getGcCode());
        assertTrue(id == other.getId(), "Cache id must be " + id + " but was :" + other.getId());
        assertTrue(available == other.isAvailable(), "Cache available must be " + available + " but was :" + other.isAvailable());
        assertTrue(archived == other.isArchived(), "Cache archived must be " + archived + " but was :" + other.isArchived());
        assertTrue(CharSequenceUtil.equals(placed_by, other.getPlacedBy()), "Placed by must be " + placed_by + " but was :" + other.getPlacedBy());
        assertTrue(CharSequenceUtil.equals(owner, other.getOwner()), "Owner must be " + owner + " but was :" + other.getOwner());
        assertTrue(container == other.getSize(), "Container must be " + container + " but was :" + other.getSize());
        assetCacheAttributes(other, database);
        assertTrue(CharSequenceUtil.equals(url, other.getUrl()), "Cache Url must be " + url + " but was :" + other.getUrl());
        assertTrue(CharSequenceUtil.equals(country, other.getCountry()), "Country must be " + country + " but was :" + other.getCountry());
        assertTrue(CharSequenceUtil.equals(state, other.getState()), "State must be " + state + " but was :" + other.getState());
        assertTrue(difficulty == other.getDifficulty(), "Cache difficulty must be " + difficulty + " but was :" + other.getDifficulty());
        assertTrue(terrain == other.getTerrain(), "Cache terrain must be " + terrain + " but was :" + other.getTerrain());
        assertTrue(found == other.isFound(), "Cache Found must be " + found + " but was :" + other.isFound());
        assertEquals(shortDescription, other.getShortDescription().toString().replaceAll("\r\n", "\n"), "Short description should be charSequenceEquals");
        assertEquals(longDescription, other.getLongDescription().toString().replaceAll("\r\n", "\n"), "Long description should be charSequenceEquals");
        assertEquals(hint, other.getHint().toString().replaceAll("\r\n", "\n"), "Hint should be charSequenceEquals");
        assertTrue(tbCount == other.getNumTravelbugs(), "Cache Tb count must be " + tbCount + " but was :" + other.getNumTravelbugs());
        assertTrue(favorite == other.isFavorite(), "Cache Favorite must be " + favorite + " but was :" + other.isFavorite());
        assertTrue(favoritePoints == other.getFavoritePoints(), "Cache FavoritePoints count must be " + favoritePoints + " but was :" + other.getFavoritePoints());
        assertEquals(note, other.getTmpNote() != null ? other.getTmpNote().toString().replaceAll("\r\n", "\n") : null, "Cache note should be charSequenceEquals");
        assertEquals(solver, other.getTmpSolver() != null ? other.getTmpSolver().toString().replaceAll("\r\n", "\n") : null, "Cache solver should be charSequenceEquals");
        assertEquals(name, other.getName().toString(), "Cache name should be charSequenceEquals");

        String expectedDate = DATE_PATTERN.format(this.dateHidden);
        String actualDate = DATE_PATTERN.format(other.getDateHidden());
        assertEquals(expectedDate, actualDate, "HiddenDate should be charSequenceEquals");

        if (testWaypoints) assertWaypoints(other, database);

        if (testLogs) assertLogs(database);
    }

    private void assertLogs(Database database) throws PlatformAssertionError {
        Array<LogEntry> otherLogEntries = database.getLogs(this.id);

        assertTrue(logEntries.size == otherLogEntries.size, "LogEntries size must be " + logEntries.size + " but was :" + otherLogEntries.size);


        if (logEntries.size == 0) return;


        for (LogEntry otherLog : otherLogEntries) {
            boolean found = false;
            for (LogEntry thisLog : logEntries) {
                if (fullLogEntryEquals(thisLog, otherLog, database)) {
                    found = true;
                    break;
                }
            }
            assertTrue(found, "LogEntry not found");
        }

        for (LogEntry otherLog : logEntries) {
            boolean found = false;
            for (LogEntry thisLog : otherLogEntries) {
                if (fullLogEntryEquals(thisLog, otherLog, database)) {
                    found = true;
                    break;
                }
            }
            assertTrue(found, "LogEntry not found");
        }
    }

     private void assetCacheAttributes(AbstractCache abstractCache, Database database) throws PlatformAssertionError {
        Iterator<Attributes> positiveIterator = positiveList.iterator();
        Iterator<Attributes> negativeIterator = negativeList.iterator();


        while (positiveIterator.hasNext()) {
            Attributes att = positiveIterator.next();
            assertTrue(abstractCache.isAttributePositiveSet(att), "positive Attribute " + att + " wrong");
        }

        while (negativeIterator.hasNext()) {
            Attributes tmp = negativeIterator.next();
            assertTrue(abstractCache.isAttributeNegativeSet((tmp)), tmp.name() + " negative Attribute wrong");
        }

        // f�lle eine Liste mit allen Attributen
        ArrayList<Attributes> attributes = new ArrayList<Attributes>();
        Attributes[] tmp = Attributes.values();
        for (Attributes item : tmp) {
            attributes.add(item);
        }

        // L�sche die vergebenen Atribute aus der Kommplett Liste
        positiveIterator = positiveList.iterator();
        negativeIterator = negativeList.iterator();

        while (positiveIterator.hasNext()) {
            attributes.remove(positiveIterator.next());
        }

        while (negativeIterator.hasNext()) {
            attributes.remove(negativeIterator.next());
        }

        attributes.remove(Attributes.getAttributeEnumByGcComId(64));
        attributes.remove(Attributes.getAttributeEnumByGcComId(65));
        attributes.remove(Attributes.getAttributeEnumByGcComId(66));

        // Teste ob die �brig gebliebenen Atributte auch nicht vergeben wurden.
        Iterator<Attributes> RestInterator = attributes.iterator();

        while (RestInterator.hasNext()) {
            Attributes attr = (Attributes) RestInterator.next();
            assertTrue(!abstractCache.isAttributePositiveSet(attr), attr.name() + " Attribute wrong");
            assertTrue(!abstractCache.isAttributeNegativeSet(attr), attr.name() + " Attribute wrong");
        }
    }

    private void assertWaypoints(AbstractCache other, Database database) throws PlatformAssertionError {
        int wpSize = other.getWaypoints() != null ? other.getWaypoints().size : 0;
        assertTrue(waypoints.size == wpSize, "Waypoint size must be " + waypoints.size + " but was :" + wpSize);


        if (waypoints.size == 0) return;

        Array<AbstractWaypoint> otherWaypoints = other.getWaypoints();

        for (AbstractWaypoint otherWp : otherWaypoints) {
            boolean found = false;
            for (AbstractWaypoint thisWp : waypoints) {
                if (fullWaypointEquals(thisWp, otherWp, database)) {
                    found = true;
                    break;
                }
            }
            assertTrue(found, "Wp not found");
        }

        for (AbstractWaypoint thisWp : waypoints) {
            boolean found = false;
            for (AbstractWaypoint otherWp : otherWaypoints) {
                if (fullWaypointEquals(thisWp, otherWp, database)) {
                    found = true;
                    break;
                }
            }
            assertTrue(found, "Wp not found");
        }

    }

    private boolean fullWaypointEquals(AbstractWaypoint wp1, AbstractWaypoint wp2, Database database) throws PlatformAssertionError {
        if (!wp1.equals(wp2)) return false; // check GcCode


        assertTrue(wp1.getType() == wp2.getType(), "Waypoint Type of " + wp1.getGcCode() + " are wrong! " +
                "was " + wp1.getType() + " instead of " + wp2.getType());

        assertTrue(wp1.getCacheId() == wp2.getCacheId(), "Waypoint Cache id of " + wp1.getGcCode() + " are wrong! " +
                "was " + wp1.getCacheId() + " instead of " + wp2.getCacheId());

        assertTrue(CharSequenceUtil.equals(wp1.getClue(), wp2.getClue()), "Waypoint Clue of " + wp1.getGcCode() + " are wrong! " +
                "was " + wp1.getClue() + " instead of " + wp2.getClue());

        assertTrue(CharSequenceUtil.equals(wp1.getDescription(), wp2.getDescription()), "Waypoint Description of " + wp1.getGcCode() + " are wrong! " +
                "was " + wp1.getDescription() + " instead of " + wp2.getDescription());

        assertTrue(CharSequenceUtil.equals(wp1.getClue(), wp2.getClue()), "Waypoint Clue of " + wp1.getGcCode() + " are wrong! " +
                "was " + wp1.getClue() + " instead of " + wp2.getClue());

        assertTrue(CharSequenceUtil.equals(wp1.getTitle(), wp2.getTitle()), "Waypoint Title of " + wp1.getGcCode() + " are wrong! " +
                "was " + wp1.getTitle() + " instead of " + wp2.getTitle());

        assertTrue(wp1.getLatitude() == wp2.getLatitude(), "Waypoint Latitude of " + wp1.getGcCode() + " are wrong! " +
                "was " + wp1.getLatitude() + " instead of " + wp2.getLatitude());

        assertTrue(wp1.getLongitude() == wp2.getLongitude(), "Waypoint Longitude of " + wp1.getGcCode() + " are wrong! " +
                "was " + wp1.getLongitude() + " instead of " + wp2.getLongitude());

        assertTrue(wp1.isStart() == wp2.isStart(), "Waypoint is Start of " + wp1.getGcCode() + " are wrong! " +
                "was " + wp1.isStart() + " instead of " + wp2.isStart());

        assertTrue(wp1.isUserWaypoint() == wp2.isUserWaypoint(), "Waypoint is UserWaypoint of " + wp1.getGcCode() + " are wrong! " +
                "was " + wp1.isUserWaypoint() + " instead of " + wp2.isUserWaypoint());

        return true;
    }

     protected boolean fullLogEntryEquals(LogEntry log1, LogEntry log2, Database database) throws PlatformAssertionError {
        if (!log1.equals(log2)) return false; // check GcCode

        assertTrue(log1.Type == log2.Type, "LogEntry Type of " + log1.Id + " are wrong! " +
                "was " + log1.Type + " instead of " + log2.Type);

        assertTrue(log1.Finder.equals(log2.Finder), "LogEntry Finder of " + log1.Id + " are wrong! " +
                "was " + log1.Finder + " instead of " + log2.Finder);

        assertTrue(log1.CacheId == log2.CacheId, "LogEntry CacheId of " + log1.Id + " are wrong! " +
                "was " + log1.CacheId + " instead of " + log2.CacheId);

        assertEquals(log1.Comment, log2.Comment, "LogEntry Comment of " + log1.Id + " are wrong! ");

        String expectedDate = DATE_PATTERN.format(log1.Timestamp);
        String actualDate = DATE_PATTERN.format(log2.Timestamp);
        assertEquals(expectedDate, actualDate, "Timestamp of LogEntry " + log1.Id + " should be charSequenceEquals");

        return true;
    }


}
