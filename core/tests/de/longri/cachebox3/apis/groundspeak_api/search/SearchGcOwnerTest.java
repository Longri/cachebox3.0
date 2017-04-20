/*
 * Copyright (C) 2017 team-cachebox.de
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

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.backends.lwjgl.LwjglNet;
import com.badlogic.gdx.net.HttpStatus;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter;
import de.longri.cachebox3.TestUtils;
import de.longri.cachebox3.callbacks.GenericCallBack;
import de.longri.cachebox3.locator.Coordinate;
import de.longri.cachebox3.locator.CoordinateGPS;
import de.longri.cachebox3.types.*;
import de.longri.cachebox3.utils.BuildInfo;
import de.longri.cachebox3.utils.lists.CB_List;
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
        SearchGCOwner searchGC = new SearchGCOwner(apiKey, 30, searchCoord, 50000, "bros", (byte) 2);

        StringWriter writer = new StringWriter();
        Json json = new Json(JsonWriter.OutputType.json);
        json.setWriter(writer);
        searchGC.getRequest(json);

        String actual = writer.toString();
        assertEquals(expected, actual);
    }


    @Test
    void parseJsonResult() throws IOException {
        final InputStream resultStream = TestUtils.getResourceRequestStream("testsResources/SearchGcOwner_result.txt");
        Coordinate searchCoord = new CoordinateGPS(52.581892, 13.398128); // Home of Katipa(like Longri)
        SearchGCOwner searchGC = new SearchGCOwner(apiKey, 30, searchCoord, 50000, "bros", (byte) 2);

        final AtomicBoolean WAIT = new AtomicBoolean(true);
        final CB_List<Cache> cacheList = new CB_List<>();
        final CB_List<LogEntry> logList = new CB_List<>();
        final CB_List<ImageEntry> imageList = new CB_List<>();
        final long gpxFilenameId = 10;

        searchGC.setLists(cacheList, logList, imageList, gpxFilenameId);

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

        searchGC.handleHttpResponse(response, new GenericCallBack<Integer>() {
            @Override
            public void callBack(Integer value) {

                assertEquals(23, cacheList.size());
                Cache cache = cacheList.first();

                assertEquals(false, cache.isArchived());
                assertEquals(true, cache.isAvailable());
                assertEquals("GC18JGX", cache.getGcCode());
                assertEquals(2, cache.waypoints.size());

                Waypoint waypoint = cache.waypoints.first();
                assertEquals("PA18JGX", waypoint.getGcCode());
                assertEquals("Parkmöglichkeit", waypoint.getDescription());
                assertEquals("Parking", waypoint.getTitle());
                assertEquals(CacheTypes.ParkingArea, waypoint.Type);
                assertEquals(52.633667, waypoint.getLatitude());
                assertEquals(13.375917, waypoint.getLongitude());

                Waypoint userWaypoint = cache.waypoints.last();
                assertEquals("CO18JGX", userWaypoint.getGcCode());
                assertEquals("", userWaypoint.getDescription());
                assertEquals("Corrected Coordinates (API)", userWaypoint.getTitle());
                assertEquals(CacheTypes.Final, userWaypoint.Type);
                assertEquals(52.616666666666667, userWaypoint.getLatitude());
                assertEquals(13.366666666666667, userWaypoint.getLongitude());
                assertEquals(true, userWaypoint.IsUserWaypoint);

                assertEquals(CacheTypes.Traditional, cache.Type);
                assertEquals(CacheSizes.other, cache.Size);
                assertEquals("Germany", cache.getCountry());
                assertEquals(new Date(1200211200000L), cache.getDateHidden());
                assertEquals(1.5f, cache.getDifficulty());
                assertEquals("bücken!", cache.getHint());
                assertEquals(0, cache.getFaviritPoints());
                assertEquals(false, cache.isFound());
                assertEquals("768551", cache.getGcId());
                assertTrue(cache.getLongDescription().startsWith("Vom empfohlenen Parkplatz beträgt die Wegstrecke etwa 500 m. Der Cac"));
                assertEquals("Weideblick", cache.getName());
                assertEquals("bros", cache.getOwner());
                assertEquals("bros", cache.getPlacedBy());
                assertEquals("Ein weiterer Cache im Tegeler Fließtal", cache.getShortDescription());
                assertEquals(2f, cache.getTerrain());
                assertEquals("http://coord.info/GC18JGX", cache.getUrl());
                assertEquals(2, cache.getApiState());
                assertEquals(52.62965, cache.getLatitude());
                assertEquals(13.372317, cache.getLongitude());

                assertEquals(2, cache.waypoints.size());

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

//                    negativeList.add(Attributes.Wheelchair_accessible);
//                    negativeList.add(Attributes.Horses);
//                    negativeList.add(Attributes.Campfires);

                }

                TestUtils.assetCacheAttributes(cache, positiveList, negativeList);


                //check Logs
                assertEquals(230, logList.size());
                LogEntry logEntry = logList.first();

                assertEquals(Cache.GenerateCacheId(cache.getGcCode()), logEntry.CacheId);
                assertEquals(678589990, logEntry.Id);
                assertEquals(LogTypes.found, logEntry.Type);
                assertTrue(logEntry.Comment.startsWith("Fast in Berlin, fast auf'm Lan"));
                assertEquals(new Date(1492455600000L), logEntry.Timestamp);
                assertEquals("w2kurlgeo", logEntry.Finder);

                logEntry = logList.last();

                assertEquals(Cache.GenerateCacheId("GC3FHRP"), logEntry.CacheId);
                assertEquals(664057049, logEntry.Id);
                assertEquals(LogTypes.found, logEntry.Type);
                assertTrue(logEntry.Comment.startsWith("Heute war ich am Morgen hier bei Neptun zu Besuch."));
                assertEquals(new Date(1486756800000L), logEntry.Timestamp);
                assertEquals("RJCK", logEntry.Finder);


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
    }


    @Test
    void testOnline() {
        if (isDummy) return;
        Coordinate searchCoord = new CoordinateGPS(52.581892, 13.398128); // Home of Katipa(like Longri)
        SearchGCOwner searchGC = new SearchGCOwner(apiKey, 30, searchCoord, 50000, "bros", (byte) 2);

        //results
        final CB_List<Cache> cacheList = new CB_List<>();
        final CB_List<LogEntry> logList = new CB_List<>();
        final CB_List<ImageEntry> imageList = new CB_List<>();
        final long gpxFilenameId = 10;
        final AtomicBoolean WAIT = new AtomicBoolean(true);

        try {
            searchGC.postRequest(new GenericCallBack<Integer>() {
                @Override
                public void callBack(Integer value) {

                    try {
                        assertEquals(23, cacheList.size());
                        Cache cache = cacheList.first();

                        assertEquals(false, cache.isArchived());
                        assertEquals(true, cache.isAvailable());
                        assertEquals("GC18JGX", cache.getGcCode());
                        assertEquals(2, cache.waypoints.size());

                        Waypoint waypoint = cache.waypoints.first();
                        assertEquals("PA18JGX", waypoint.getGcCode());
                        assertEquals("Parkmöglichkeit", waypoint.getDescription());
                        assertEquals("Parking", waypoint.getTitle());
                        assertEquals(CacheTypes.ParkingArea, waypoint.Type);
                        assertEquals(52.633667, waypoint.getLatitude());
                        assertEquals(13.375917, waypoint.getLongitude());

                        assertEquals(CacheTypes.Traditional, cache.Type);
                        assertEquals(CacheSizes.other, cache.Size);
                        assertEquals("Germany", cache.getCountry());
                        assertEquals(new Date(1200211200000L), cache.getDateHidden());
                        assertEquals(1.5f, cache.getDifficulty());
                        assertEquals("bücken!", cache.getHint());
                        assertEquals(0, cache.getFaviritPoints());
                        assertEquals(false, cache.isFound());
                        assertEquals("768551", cache.getGcId());
                        assertTrue(cache.getLongDescription().startsWith("Vom empfohlenen Parkplatz beträgt die Wegstrecke etwa 500 m. Der Cac"));
                        assertEquals("Weideblick", cache.getName());
                        assertEquals("bros", cache.getOwner());
                        assertEquals("bros", cache.getPlacedBy());
                        assertEquals("Ein weiterer Cache im Tegeler Fließtal", cache.getShortDescription());
                        assertEquals(2f, cache.getTerrain());
                        assertEquals("http://coord.info/GC18JGX", cache.getUrl());
                        assertEquals(2, cache.getApiState());
                        assertEquals(52.62965, cache.getLatitude());
                        assertEquals(13.372317, cache.getLongitude());

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

                        TestUtils.assetCacheAttributes(cache, positiveList, negativeList);


                        //check Logs
                        assertEquals(230, logList.size());
                        LogEntry logEntry = logList.first();

                        assertEquals(Cache.GenerateCacheId(cache.getGcCode()), logEntry.CacheId);
                        logEntry = logList.last();

                        assertEquals(Cache.GenerateCacheId("GC3FHRP"), logEntry.CacheId);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    WAIT.set(false);
                }
            }, cacheList, logList, imageList, gpxFilenameId);
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

    }

}
