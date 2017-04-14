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

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter;
import de.longri.cachebox3.types.Cache;
import de.longri.cachebox3.types.ImageEntry;
import de.longri.cachebox3.types.LogEntry;
import de.longri.cachebox3.utils.lists.CB_List;
import org.apache.commons.codec.Charsets;
import org.junit.jupiter.api.Test;
import travis.EXCLUDE_FROM_TRAVIS;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.*;
import static travis.EXCLUDE_FROM_TRAVIS.LONGRI_HOME_COORDS;

/**
 * Created by longri on 14.04.17.
 */
class SearchGCTest {

    final String apiKey = EXCLUDE_FROM_TRAVIS.GcAPI;
    final boolean isDummy = apiKey.equals(EXCLUDE_FROM_TRAVIS.DUMMY_API_KEY);

    @Test
    void getRequest() throws IOException {
        File file = new File("testsResources/SearchGc_request.txt");
        FileInputStream stream = new FileInputStream(file);


        byte[] b = new byte[(int) file.length()];
        int len = b.length;
        int total = 0;

        while (total < len) {
            int result = stream.read(b, total, len - total);
            if (result == -1) {
                break;
            }
            total += result;
        }
        String expected = new String(b, Charsets.UTF_8);

        if (!isDummy) {
            expected = expected.replace("\"AccessToken\":\"+DummyKEY\"",
                    "\"AccessToken\":\"" + apiKey + "\"");
        }

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

    }

    @Test
    void testOnline() {
        if (isDummy) return;

        SearchGC searchGC = new SearchGC(apiKey, "GC1T33T");

        //results
        CB_List<Cache> cacheList = new CB_List<>();
        CB_List<LogEntry> logList = new CB_List<>();
        CB_List<ImageEntry> imageList = new CB_List<>();
        long gpxFilenameId = 10;

        searchGC.postRequest(cacheList, logList, imageList, gpxFilenameId);

    }

}
