/*
 * Copyright (C) 2017 - 2018 team-cachebox.de
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
package de.longri.cachebox3.apis.groundspeak_api.search;

import com.badlogic.gdx.Net;
import com.badlogic.gdx.net.HttpStatus;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter;
import de.longri.cachebox3.TestUtils;
import de.longri.cachebox3.apis.groundspeak_api.ApiResultState;
import de.longri.cachebox3.callbacks.GenericCallBack;
import de.longri.cachebox3.locator.Coordinate;
import de.longri.cachebox3.locator.CoordinateGPS;
import de.longri.cachebox3.sqlite.Database;
import de.longri.cachebox3.sqlite.dao.CacheList3DAO;
import de.longri.cachebox3.sqlite.dao.LogDAO;
import de.longri.cachebox3.sqlite.dao.TrackableDao;
import de.longri.cachebox3.types.*;
import org.junit.jupiter.api.Test;
import travis.EXCLUDE_FROM_TRAVIS;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Created by Longri on 17.04.17.
 */
class SearchGcOwnerTest {

    static {
        TestUtils.initialGdx();
    }

    final String apiKey = EXCLUDE_FROM_TRAVIS.GcAPI;
    final boolean isDummy = apiKey.equals(EXCLUDE_FROM_TRAVIS.DUMMY_API_KEY);

    @Test
    void getRequest() throws IOException {
        String expected = TestUtils.getResourceRequestString("testsResources/SearchGcOwner_request.txt",
                isDummy ? null : apiKey);

        Coordinate searchCoord = new CoordinateGPS(52.581892, 13.398128); // Home of Katipa(like Longri)
        Database testDB = TestUtils.getTestDB(false);
        SearchGCOwner searchGC = new SearchGCOwner(testDB, apiKey, 30, searchCoord, 50000, "bros", (byte) 2);

        StringWriter writer = new StringWriter();
        Json json = new Json(JsonWriter.OutputType.json);
        json.setWriter(writer);
        searchGC.getRequest(json);

        String actual = writer.toString();
        assertEquals(expected, actual);
        testDB.close();
        testDB.getFileHandle().delete();
    }


    @Test
    void parseJsonResult() throws IOException {
        final InputStream resultStream = TestUtils.getResourceRequestStream("testsResources/SearchGcOwner_result.txt");
        Coordinate searchCoord = new CoordinateGPS(52.581892, 13.398128); // Home of Katipa(like Longri)

        final Database testDB = TestUtils.getTestDB(false);
        final SearchGCOwner searchGC = new SearchGCOwner(testDB, apiKey, 30, searchCoord, 50000, "bros", (byte) 2) {

        };

        final AtomicBoolean WAIT = new AtomicBoolean(true);

        Net.HttpResponse response = new Net.HttpResponse() {
            @Override
            public byte[] getResult() {
                return new byte[0];
            }

            @Override
            public String getResultAsString() {
                return null;
            }

            @Override
            public InputStream getResultAsStream() {
                return resultStream;
            }

            @Override
            public HttpStatus getStatus() {
                return null;
            }

            @Override
            public String getHeader(String name) {
                return null;
            }

            @Override
            public Map<String, List<String>> getHeaders() {
                return null;
            }
        };

        searchGC.handleHttpResponse(response, new GenericCallBack<ApiResultState>() {
            @Override
            public void callBack(ApiResultState value) {

                CacheList3DAO list3DAO = new CacheList3DAO();
                CacheList cacheList = new CacheList();
                list3DAO.readCacheList(testDB, cacheList, null, true, true);

                assertEquals(value, ApiResultState.IO);

                assertEquals(23, cacheList.size);
                AbstractCache abstractCache = cacheList.get(0);

                assertEquals(false, abstractCache.isArchived());
                assertEquals(true, abstractCache.isAvailable());
                assertEquals("GC18JGX", abstractCache.getGcCode().toString());
                assertEquals(2, abstractCache.getWaypoints().size);

                AbstractWaypoint waypoint = abstractCache.getWaypoints().peek();
                assertEquals("PA18JGX", waypoint.getGcCode().toString());
                assertEquals("Parkmöglichkeit", waypoint.getDescription(testDB).toString());
                assertEquals("Parking", waypoint.getTitle().toString());
                assertEquals(CacheTypes.ParkingArea, waypoint.getType());
                assertEquals(52.633667, waypoint.getLatitude());
                assertEquals(13.375917, waypoint.getLongitude());

                AbstractWaypoint userWaypoint = abstractCache.getWaypoints().first();
                assertEquals("CO18JGX", userWaypoint.getGcCode().toString());
                assertEquals("", userWaypoint.getDescription(testDB).toString());
                assertEquals("Corrected Coordinates (API)", userWaypoint.getTitle().toString());
                assertEquals(CacheTypes.Final, userWaypoint.getType());
                assertEquals(52.616666666666667, userWaypoint.getLatitude());
                assertEquals(13.366666666666667, userWaypoint.getLongitude());
                assertEquals(true, userWaypoint.isUserWaypoint());

                assertEquals(CacheTypes.Traditional, abstractCache.getType());
                assertEquals(CacheSizes.small, abstractCache.getSize());
                assertEquals("Germany", abstractCache.getCountry(testDB));
                assertEquals(new Date(1200211200000L), abstractCache.getDateHidden(testDB));
                assertEquals(1.5f, abstractCache.getDifficulty());
                assertEquals("bücken!", abstractCache.getHint(testDB).toString());
                assertEquals(0, abstractCache.getFavoritePoints());
                assertEquals(false, abstractCache.isFound());
                assertEquals("768551", abstractCache.getGcId().toString());
                assertTrue(abstractCache.getLongDescription(testDB).startsWith("Vom empfohlenen Parkplatz beträgt die Wegstrecke etwa 500 m. Der Cac"));
                assertEquals("Weideblick", abstractCache.getName().toString());
                assertEquals("bros", abstractCache.getOwner().toString());
                assertEquals("bros", abstractCache.getPlacedBy().toString());
                assertEquals("Ein weiterer Cache im Tegeler Fließtal", abstractCache.getShortDescription(testDB));
                assertEquals(2f, abstractCache.getTerrain());
                assertEquals("http://coord.info/GC18JGX", abstractCache.getUrl(testDB));
                assertEquals(2, abstractCache.getApiState(testDB));
                assertEquals(52.62965, abstractCache.getLatitude());
                assertEquals(13.372317, abstractCache.getLongitude());

                assertEquals(2, abstractCache.getWaypoints().size);

                // Attribute Tests

                ArrayList<Attributes> positiveList = new ArrayList<>();
                ArrayList<Attributes> negativeList = new ArrayList<>();

                {
                    positiveList.add(Attributes.Dogs);
                    positiveList.add(Attributes.Recommended_for_kids);
                    positiveList.add(Attributes.Takes_less_than_an_hour);
                    positiveList.add(Attributes.Scenic_view);
                    positiveList.add(Attributes.Public_transportation);
                    positiveList.add(Attributes.Bicycles);


                }

                TestUtils.assetCacheAttributes(testDB, abstractCache, positiveList, negativeList);

                LogDAO dao = new LogDAO();
                Array<LogEntry> logList = dao.getLogs(testDB, null);


                //check Logs
                assertEquals(230, logList.size);
                LogEntry logEntry = logList.first();

                assertEquals(AbstractCache.GenerateCacheId(abstractCache.getGcCode().toString()), logEntry.CacheId);
                assertEquals(678589990, logEntry.Id);
                assertEquals(LogTypes.found, logEntry.Type);
                assertTrue(logEntry.Comment.startsWith("Fast in Berlin, fast auf'm Lan"));
                assertEquals(new Date(1492455600000L), logEntry.Timestamp);
                assertEquals("w2kurlgeo", logEntry.Finder);

                logEntry = logList.get(logList.size - 1);

                assertEquals(AbstractCache.GenerateCacheId("GC3FHRP"), logEntry.CacheId);
                assertEquals(664057049, logEntry.Id);
                assertEquals(LogTypes.found, logEntry.Type);
                assertTrue(logEntry.Comment.startsWith("Heute war ich am Morgen hier bei Neptun zu Besuch."));
                assertEquals(new Date(1486756800000L), logEntry.Timestamp);
                assertEquals("RJCK", logEntry.Finder);

                TrackableDao tbDao = new TrackableDao();
                Array<Trackable> tbList = tbDao.getTBs(testDB, null);

                assertEquals(4, tbList.size);
                Trackable trackable = tbList.first();

                assertEquals(482827, trackable.getId());
                assertEquals(false, trackable.getArchived());
                assertEquals("TBZ070", trackable.getGcCode());
                assertEquals(AbstractCache.GenerateCacheId("GCZ7AJ"), trackable.CacheId());
                assertTrue(trackable.getCurrenGoal().startsWith("Next destination is Cache: Minenfeld (GCGFQC)"));
                assertEquals(new Date(1144092669490L).toString(), trackable.getDateCreated().toString());
                assertTrue(trackable.getDescription().contains("After a big trip from Thailand (5811 Miles) back to Germany"));
                assertEquals("http://www.geocaching.com/images/wpttypes/21.gif", trackable.getIconUrl());
                assertEquals("André's First ", trackable.getName());
                assertEquals("http://coord.info/TBZ070", trackable.getUrl());

                WAIT.set(false);
            }
        });

        while (WAIT.get()) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        testDB.close();
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        testDB.getFileHandle().delete();
    }


    @Test
    void testOnline() {
        if (isDummy) return;
        Coordinate searchCoord = new CoordinateGPS(52.616667, 13.366667);

        final Database testDB = TestUtils.getTestDB(false);
        final SearchGCOwner searchGC = new SearchGCOwner(testDB, apiKey, 30, searchCoord, 50000, "bros", (byte) 2) {

        };

        //results
        final long gpxFilenameId = 10;
        final AtomicBoolean WAIT = new AtomicBoolean(true);
        final ApiResultState[] VALUE = new ApiResultState[1];
        try {
            searchGC.postRequest(new GenericCallBack<ApiResultState>() {
                @Override
                public void callBack(ApiResultState value) {
                    VALUE[0] = value;
                    WAIT.set(false);
                }
            }, gpxFilenameId);
        } catch (Exception e) {
            e.printStackTrace();
            WAIT.set(false);
        }

        while (WAIT.get()) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }


        assertThat("API key expired, can't test!", VALUE[0] != ApiResultState.EXPIRED_API_KEY);
        assertEquals(ApiResultState.IO, VALUE[0]);

        CacheList3DAO list3DAO = new CacheList3DAO();
        CacheList cacheList = new CacheList();
        list3DAO.readCacheList(testDB, cacheList, null, true, true);


        assertThat("Cache list size must bigger then 0", cacheList.size > 0);
        AbstractCache abstractCache = cacheList.GetCacheByGcCode("GC18JGX");

        assertThat("Cache must exist", abstractCache != null);


        assertEquals(false, abstractCache.isArchived());
        assertEquals(true, abstractCache.isAvailable());
        assertEquals("GC18JGX", abstractCache.getGcCode().toString());
        assertEquals(2, abstractCache.getWaypoints().size);

        AbstractWaypoint waypoint = abstractCache.getWaypoints().get(1);
        assertEquals("PA18JGX", waypoint.getGcCode().toString());
        assertEquals("Parkmöglichkeit", waypoint.getDescription(testDB).toString());
        assertEquals("Parking", waypoint.getTitle().toString());
        assertEquals(CacheTypes.ParkingArea, waypoint.getType());
        assertEquals(52.633667, waypoint.getLatitude());
        assertEquals(13.375917, waypoint.getLongitude());

        assertEquals(CacheTypes.Traditional, abstractCache.getType());
        assertEquals(CacheSizes.small, abstractCache.getSize());
        assertEquals("Germany", abstractCache.getCountry(testDB).toString());
        assertEquals(new Date(1200211200000L), abstractCache.getDateHidden(testDB));
        assertEquals(1.5f, abstractCache.getDifficulty());
        assertEquals("bücken!", abstractCache.getHint(testDB).toString());
        assertEquals(0, abstractCache.getFavoritePoints());
        assertEquals(false, abstractCache.isFound());
        assertEquals("768551", abstractCache.getGcId().toString());
        assertTrue(abstractCache.getLongDescription(testDB).startsWith("Vom empfohlenen Parkplatz beträgt die Wegstrecke etwa 500 m. Der Cac"));
        assertEquals("Weideblick", abstractCache.getName().toString());
        assertEquals("bros", abstractCache.getOwner().toString());
        assertEquals("bros", abstractCache.getPlacedBy().toString());
        assertEquals("Ein weiterer Cache im Tegeler Fließtal", abstractCache.getShortDescription(testDB).toString());
        assertEquals(2f, abstractCache.getTerrain());
        assertEquals("http://coord.info/GC18JGX", abstractCache.getUrl(testDB).toString());
        assertEquals(2, abstractCache.getApiState(testDB));
        assertEquals(52.62965, abstractCache.getLatitude());
        assertEquals(13.372317, abstractCache.getLongitude());

        // Attribute Tests

        ArrayList<Attributes> positiveList = new ArrayList<>();
        ArrayList<Attributes> negativeList = new ArrayList<>();

        {
            positiveList.add(Attributes.Dogs);
            positiveList.add(Attributes.Recommended_for_kids);
            positiveList.add(Attributes.Takes_less_than_an_hour);
            positiveList.add(Attributes.Scenic_view);
            positiveList.add(Attributes.Public_transportation);
            positiveList.add(Attributes.Bicycles);
        }

        TestUtils.assetCacheAttributes(testDB, abstractCache, positiveList, negativeList);

        LogDAO dao = new LogDAO();
        Array<LogEntry> logList = dao.getLogs(testDB, null);


        //check Logs
        assertThat("Log count must bigger then 100", logList.size > 100);
        LogEntry logEntry = logList.first();

        assertEquals(AbstractCache.GenerateCacheId(abstractCache.getGcCode().toString()), logEntry.CacheId);
        logEntry = logList.get(logList.size - 1);

        assertEquals(AbstractCache.GenerateCacheId("GC3FHRP"), logEntry.CacheId);


        testDB.close();


        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        testDB.getFileHandle().delete();
    }

}
