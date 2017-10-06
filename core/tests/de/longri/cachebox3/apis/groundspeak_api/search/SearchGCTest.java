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
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationLogger;
import com.badlogic.gdx.backends.lwjgl.LwjglFiles;
import com.badlogic.gdx.backends.lwjgl.LwjglNet;
import com.badlogic.gdx.net.HttpStatus;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter;
import de.longri.cachebox3.TestUtils;
import de.longri.cachebox3.callbacks.GenericCallBack;
import de.longri.cachebox3.types.*;
import de.longri.cachebox3.utils.BuildInfo;
import de.longri.cachebox3.utils.lists.CB_List;
import org.junit.jupiter.api.Test;
import org.slf4j.impl.DummyLogApplication;
import travis.EXCLUDE_FROM_TRAVIS;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Created by Longri on 14.04.17.
 */
class SearchGCTest {

    static {
        TestUtils.initialGdx();
    }

    final String apiKey = EXCLUDE_FROM_TRAVIS.GcAPI;
    final boolean isDummy = apiKey.equals(EXCLUDE_FROM_TRAVIS.DUMMY_API_KEY);

    @Test
    void getRequest() throws IOException {
        String expected = TestUtils.getResourceRequestString("core/testsResources/SearchGc_request.txt",
                isDummy ? null : apiKey);
        SearchGC searchGC = new SearchGC(apiKey, "GC1T33T");

        StringWriter writer = new StringWriter();
        Json json = new Json(JsonWriter.OutputType.json);
        json.setWriter(writer);
        searchGC.getRequest(json);

        String actual = writer.toString();
        assertEquals(expected, actual, "Should be equals");
    }


    @Test
    void parseJsonResult() throws IOException {
        final InputStream resultStream = TestUtils.getResourceRequestStream("core/testsResources/SearchGc_result.txt");
        final CB_List<Cache> cacheList = new CB_List<>();
        final CB_List<LogEntry> logList = new CB_List<>();
        final CB_List<ImageEntry> imageList = new CB_List<>();
        final SearchGC searchGC = new SearchGC(apiKey, "GC1T33T"){
            protected void writeLogToDB(final LogEntry logEntry) {
                logList.add(logEntry);
            }

            protected void writeImagEntryToDB(final ImageEntry imageEntry) {
                imageList.add(imageEntry);
            }

            protected void writeCacheToDB(final Cache cache) {
                cacheList.add(cache);
            }
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

        searchGC.handleHttpResponse(response, new GenericCallBack<Integer>() {
            @Override
            public void callBack(Integer value) {

                assertEquals(1, cacheList.size);
                Cache cache = cacheList.pop();

                assertEquals(false, cache.isArchived());
                assertEquals(true, cache.isAvailable());
                assertEquals("GC1T33T", cache.getGcCode());
                assertEquals(0, cache.waypoints.size);
                assertEquals(CacheTypes.Traditional, cache.Type);
                assertEquals(CacheSizes.other, cache.Size);
                assertEquals("Germany", cache.getCountry());
                assertEquals(new Date(1243753200000L), cache.getDateHidden());
                assertEquals(3f, cache.getDifficulty());
                assertEquals("", cache.getHint());
                assertEquals(12, cache.getFaviritPoints());
                assertEquals(true, cache.isFound());
                assertEquals("1260177", cache.getGcId());
                assertTrue(cache.getLongDescription().startsWith("<div style=\"text-align:center;\">Eine Hunderunde gedreht und mal "));
                assertEquals("Nur ein Berg", cache.getName());
                assertEquals("Wurzellisel", cache.getOwner());
                assertEquals("Wurzellisel", cache.getPlacedBy());
                assertEquals("\r\n", cache.getShortDescription());
                assertEquals(2f, cache.getTerrain());
                assertEquals("http://coord.info/GC1T33T", cache.getUrl());
                assertEquals(2, cache.getApiState());
                assertEquals(52.579267, cache.getLatitude());
                assertEquals(13.381983, cache.getLongitude());

                // Attribute Tests

                ArrayList<Attributes> positiveList = new ArrayList<>();
                ArrayList<Attributes> negativeList = new ArrayList<>();

                {
                    positiveList.add(Attributes.Dogs);
                    positiveList.add(Attributes.Recommended_for_kids);
                    positiveList.add(Attributes.Available_at_all_times);
                    positiveList.add(Attributes.Available_during_winter);
                    positiveList.add(Attributes.Ticks);
                    positiveList.add(Attributes.Bicycles);
                    positiveList.add(Attributes.Stealth_required);

                    negativeList.add(Attributes.Wheelchair_accessible);
                    negativeList.add(Attributes.Horses);
                    negativeList.add(Attributes.Campfires);

                }

                TestUtils.assetCacheAttributes(cache, positiveList, negativeList);


                //check Logs
                assertEquals(10, logList.size);
                LogEntry logEntry = logList.first();

                assertEquals(Cache.GenerateCacheId(cache.getGcCode()), logEntry.CacheId);
                assertEquals(677446155, logEntry.Id);
                assertEquals(LogTypes.found, logEntry.Type);
                assertTrue(logEntry.Comment.startsWith("Heute fand das Event  GC73332 Eiergolf"));
                assertEquals(new Date(1492196400000L), logEntry.Timestamp);
                assertEquals("TeamReitenwolfgang",logEntry.Finder);

                logEntry = logList.last();

                assertEquals(Cache.GenerateCacheId(cache.getGcCode()), logEntry.CacheId);
                assertEquals(663746391, logEntry.Id);
                assertEquals(LogTypes.found, logEntry.Type);
                assertTrue(logEntry.Comment.startsWith("Ein freundliches Hallo an Wurzellisel,"));
                assertEquals(new Date(1486497600000L), logEntry.Timestamp);
                assertEquals("kerbholz",logEntry.Finder);


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
        final CB_List<Cache> cacheList = new CB_List<>();
        final CB_List<LogEntry> logList = new CB_List<>();
        final CB_List<ImageEntry> imageList = new CB_List<>();
        final SearchGC searchGC = new SearchGC(apiKey, "GC1T33T"){
            protected void writeLogToDB(final LogEntry logEntry) {
                logList.add(logEntry);
            }

            protected void writeImagEntryToDB(final ImageEntry imageEntry) {
                imageList.add(imageEntry);
            }

            protected void writeCacheToDB(final Cache cache) {
                cacheList.add(cache);
            }
        };

        //results
        final long gpxFilenameId = 10;
        final AtomicBoolean WAIT = new AtomicBoolean(true);

        searchGC.postRequest(new GenericCallBack<Integer>() {
            @Override
            public void callBack(Integer value) {

                assertEquals(1, cacheList.size);
                Cache cache = cacheList.pop();

                assertEquals(false, cache.isArchived());
                assertEquals(true, cache.isAvailable());
                assertEquals("GC1T33T", cache.getGcCode());
                assertEquals(0, cache.waypoints.size);
                assertEquals(CacheTypes.Traditional, cache.Type);
                assertEquals(CacheSizes.other, cache.Size);
                assertEquals("Germany", cache.getCountry());
                assertEquals(new Date(1243753200000L), cache.getDateHidden());
                assertEquals(3f, cache.getDifficulty());
                assertEquals("", cache.getHint());
                assertEquals(12, cache.getFaviritPoints());
                assertEquals(true, cache.isFound());
                assertEquals("1260177", cache.getGcId());
                assertTrue(cache.getLongDescription().startsWith("<div style=\"text-align:center;\">Eine Hunderunde gedreht und mal "));
                assertEquals("Nur ein Berg", cache.getName());
                assertEquals("Wurzellisel", cache.getOwner());
                assertEquals("Wurzellisel", cache.getPlacedBy());
                assertEquals("\r\n", cache.getShortDescription());
                assertEquals(2f, cache.getTerrain());
                assertEquals("http://coord.info/GC1T33T", cache.getUrl());
                assertEquals(2, cache.getApiState());
                assertEquals(52.579267, cache.getLatitude());
                assertEquals(13.381983, cache.getLongitude());

                // Attribute Tests

                ArrayList<Attributes> positiveList = new ArrayList<>();
                ArrayList<Attributes> negativeList = new ArrayList<>();

                {
                    positiveList.add(Attributes.Dogs);
                    positiveList.add(Attributes.Recommended_for_kids);
                    positiveList.add(Attributes.Available_at_all_times);
                    positiveList.add(Attributes.Available_during_winter);
                    positiveList.add(Attributes.Ticks);
                    positiveList.add(Attributes.Bicycles);
                    positiveList.add(Attributes.Stealth_required);

                    negativeList.add(Attributes.Wheelchair_accessible);
                    negativeList.add(Attributes.Horses);
                    negativeList.add(Attributes.Campfires);

                }

                TestUtils.assetCacheAttributes(cache, positiveList, negativeList);







                WAIT.set(false);
            }
        }, gpxFilenameId);

        while (WAIT.get()) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

}
