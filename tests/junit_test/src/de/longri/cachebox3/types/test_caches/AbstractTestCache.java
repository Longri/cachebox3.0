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
import de.longri.cachebox3.utils.CharSequenceUtilTest;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

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


    public void assertCache(AbstractCache other, Database database) {
        assertThat("Cache must not be NULL", other != null);
        assertThat("Latitude must be " + latitude + " but was :" + other.getLatitude(), latitude == other.getLatitude());
        assertThat("Longitude must be " + longitude + " but was :" + other.getLongitude(), longitude == other.getLongitude());
        assertThat("Cache type must be " + cacheType + " but was :" + other.getType(), cacheType == other.getType());
        assertThat("GcCode must be " + gcCode + " but was :" + other.getGcCode(), CharSequenceUtilTest.equals(gcCode, other.getGcCode()));
        assertThat("Cache id must be " + id + " but was :" + other.getId(), id == other.getId());
        assertThat("Cache available must be " + available + " but was :" + other.isAvailable(), available == other.isAvailable());
        assertThat("Cache archived must be " + archived + " but was :" + other.isArchived(), archived == other.isArchived());
        assertThat("Placed by must be " + placed_by + " but was :" + other.getPlacedBy(), CharSequenceUtilTest.equals(placed_by, other.getPlacedBy()));
        assertThat("Owner must be " + owner + " but was :" + other.getOwner(), CharSequenceUtilTest.equals(owner, other.getOwner()));
        assertThat("Container must be " + container + " but was :" + other.getSize(), container == other.getSize());
        assetCacheAttributes(other, database);
        assertThat("Cache Url must be " + url + " but was :" + other.getUrl(), CharSequenceUtilTest.equals(url, other.getUrl()));
        assertThat("Country must be " + country + " but was :" + other.getCountry(), CharSequenceUtilTest.equals(country, other.getCountry()));
        assertThat("State must be " + state + " but was :" + other.getState(), CharSequenceUtilTest.equals(state, other.getState()));
        assertThat("Cache difficulty must be " + difficulty + " but was :" + other.getDifficulty(), difficulty == other.getDifficulty());
        assertThat("Cache terrain must be " + terrain + " but was :" + other.getTerrain(), terrain == other.getTerrain());
        assertThat("Cache Found must be " + found + " but was :" + other.isFound(), found == other.isFound());
        assertEquals(shortDescription, other.getShortDescription().toString().replaceAll("\r\n", "\n"), "Short description should be equals");
        assertEquals(longDescription, other.getLongDescription().toString().replaceAll("\r\n", "\n"), "Long description should be equals");
        assertEquals(hint, other.getHint().toString().replaceAll("\r\n", "\n"), "Hint should be equals");
        assertThat("Cache Tb count must be " + tbCount + " but was :" + other.getNumTravelbugs(), tbCount == other.getNumTravelbugs());
        assertThat("Cache Favorite must be " + favorite + " but was :" + other.isFavorite(), favorite == other.isFavorite());
        assertThat("Cache FavoritePoints count must be " + favoritePoints + " but was :" + other.getFavoritePoints(), favoritePoints == other.getFavoritePoints());
        assertEquals(note, other.getTmpNote() != null ? other.getTmpNote().toString().replaceAll("\r\n", "\n") : null, "Cache note should be equals");
        assertEquals(solver, other.getTmpSolver() != null ? other.getTmpSolver().toString().replaceAll("\r\n", "\n") : null, "Cache solver should be equals");
        assertEquals(name, other.getName().toString(), "Cache name should be equals");

        String expectedDate = DATE_PATTERN.format(this.dateHidden);
        String actualDate = DATE_PATTERN.format(other.getDateHidden());
        assertEquals(expectedDate, actualDate, "HiddenDate should be equals");

        if (testWaypoints) assertWaypoints(other, database);

        if (testLogs) assertLogs(database);
    }

    private void assertLogs(Database database) {
        Array<LogEntry> otherLogEntries = database.getLogs(this.id);

        assertThat("LogEntries size must be " + logEntries.size + " but was :" + otherLogEntries.size, logEntries.size == otherLogEntries.size);


        if (logEntries.size == 0) return;


        for (LogEntry otherLog : otherLogEntries) {
            boolean found = false;
            for (LogEntry thisLog : logEntries) {
                if (fullLogEntryEquals(thisLog, otherLog, database)) {
                    found = true;
                    break;
                }
            }
            assertThat("LogEntry not found", found);
        }

        for (LogEntry otherLog : logEntries) {
            boolean found = false;
            for (LogEntry thisLog : otherLogEntries) {
                if (fullLogEntryEquals(thisLog, otherLog, database)) {
                    found = true;
                    break;
                }
            }
            assertThat("LogEntry not found", found);
        }
    }

    private void assetCacheAttributes(AbstractCache abstractCache, Database database) {
        Iterator<Attributes> positiveIterator = positiveList.iterator();
        Iterator<Attributes> negativeIterator = negativeList.iterator();


        while (positiveIterator.hasNext()) {
            Attributes att = positiveIterator.next();
            assertThat("positive Attribute " + att + " wrong", abstractCache.isAttributePositiveSet(att));
        }

        while (negativeIterator.hasNext()) {
            Attributes tmp = negativeIterator.next();
            assertThat(tmp.name() + " negative Attribute wrong", abstractCache.isAttributeNegativeSet((tmp)));
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
            assertThat(attr.name() + " Attribute wrong", !abstractCache.isAttributePositiveSet(attr));
            assertThat(attr.name() + " Attribute wrong", !abstractCache.isAttributeNegativeSet(attr));
        }
    }

    private void assertWaypoints(AbstractCache other, Database database) {
        int wpSize = other.getWaypoints() != null ? other.getWaypoints().size : 0;
        assertThat("Waypoint size must be " + waypoints.size + " but was :" + wpSize, waypoints.size == wpSize);


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
            assertThat("Wp not found", found);
        }

        for (AbstractWaypoint thisWp : waypoints) {
            boolean found = false;
            for (AbstractWaypoint otherWp : otherWaypoints) {
                if (fullWaypointEquals(thisWp, otherWp, database)) {
                    found = true;
                    break;
                }
            }
            assertThat("Wp not found", found);
        }

    }

    private boolean fullWaypointEquals(AbstractWaypoint wp1, AbstractWaypoint wp2, Database database) {
        if (!wp1.equals(wp2)) return false; // check GcCode


        assertThat("Waypoint Type of " + wp1.getGcCode() + " are wrong! " +
                "was " + wp1.getType() + " instead of " + wp2.getType(), wp1.getType() == wp2.getType());

        assertThat("Waypoint Cache id of " + wp1.getGcCode() + " are wrong! " +
                "was " + wp1.getCacheId() + " instead of " + wp2.getCacheId(), wp1.getCacheId() == wp2.getCacheId());

        assertThat("Waypoint Clue of " + wp1.getGcCode() + " are wrong! " +
                "was " + wp1.getClue(database) + " instead of " + wp2.getClue(database), CharSequenceUtilTest.equals(wp1.getClue(database), wp2.getClue(database)));

        assertThat("Waypoint Description of " + wp1.getGcCode() + " are wrong! " +
                "was " + wp1.getDescription(database) + " instead of " + wp2.getDescription(database), CharSequenceUtilTest.equals(wp1.getDescription(database), wp2.getDescription(database)));

        assertThat("Waypoint Clue of " + wp1.getGcCode() + " are wrong! " +
                "was " + wp1.getClue(database) + " instead of " + wp2.getClue(database), CharSequenceUtilTest.equals(wp1.getClue(database), wp2.getClue(database)));

        assertThat("Waypoint Title of " + wp1.getGcCode() + " are wrong! " +
                "was " + wp1.getTitle() + " instead of " + wp2.getTitle(), CharSequenceUtilTest.equals(wp1.getTitle(), wp2.getTitle()));

        assertThat("Waypoint Latitude of " + wp1.getGcCode() + " are wrong! " +
                "was " + wp1.getLatitude() + " instead of " + wp2.getLatitude(), wp1.getLatitude() == wp2.getLatitude());

        assertThat("Waypoint Longitude of " + wp1.getGcCode() + " are wrong! " +
                "was " + wp1.getLongitude() + " instead of " + wp2.getLongitude(), wp1.getLongitude() == wp2.getLongitude());

        assertThat("Waypoint is Start of " + wp1.getGcCode() + " are wrong! " +
                "was " + wp1.isStart() + " instead of " + wp2.isStart(), wp1.isStart() == wp2.isStart());

        assertThat("Waypoint is UserWaypoint of " + wp1.getGcCode() + " are wrong! " +
                "was " + wp1.isUserWaypoint() + " instead of " + wp2.isUserWaypoint(), wp1.isUserWaypoint() == wp2.isUserWaypoint());

        return true;
    }

    protected boolean fullLogEntryEquals(LogEntry log1, LogEntry log2, Database database) {
        if (!log1.equals(log2)) return false; // check GcCode

        assertThat("LogEntry Type of " + log1.Id + " are wrong! " +
                "was " + log1.Type + " instead of " + log2.Type, log1.Type == log2.Type);

        assertThat("LogEntry Finder of " + log1.Id + " are wrong! " +
                "was " + log1.Finder + " instead of " + log2.Finder, log1.Finder.equals(log2.Finder));

        assertThat("LogEntry CacheId of " + log1.Id + " are wrong! " +
                "was " + log1.CacheId + " instead of " + log2.CacheId, log1.CacheId == log2.CacheId);

        assertEquals(log1.Comment, log2.Comment, "LogEntry Comment of " + log1.Id + " are wrong! ");

        String expectedDate = DATE_PATTERN.format(log1.Timestamp);
        String actualDate = DATE_PATTERN.format(log2.Timestamp);
        assertEquals(expectedDate, actualDate, "Timestamp of LogEntry " + log1.Id + " should be equals");

        return true;
    }


}
