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

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter;
import de.longri.cachebox3.TestUtils;
import de.longri.cachebox3.apis.groundspeak_api.ApiResultState;
import de.longri.cachebox3.apis.groundspeak_api.GroundspeakLiveAPI;
import de.longri.cachebox3.sqlite.Database;
import org.junit.jupiter.api.Test;
import travis.EXCLUDE_FROM_TRAVIS;

import java.io.IOException;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Created by longri on 14.04.17.
 */
class SearchCoordinateTest {

    static {
        TestUtils.initialGdx();
    }

    final String apiKey = EXCLUDE_FROM_TRAVIS.GcAPI();
    final boolean isDummy = apiKey.equals(EXCLUDE_FROM_TRAVIS.DUMMY_API_KEY);

    @Test
    void getRequest() throws IOException {

        String expected = TestUtils.getResourceRequestString("testsResources/SearchGcCoordinate_request.txt",
                isDummy ? null : apiKey);


        //set MembershipType for tests to Premium
        GroundspeakLiveAPI.setTestMembershipType(ApiResultState.MEMBERSHIP_TYPE_PREMIUM);

        byte apiState;
        if (GroundspeakLiveAPI.isPremiumMember()) {
            apiState = 2;
        } else {
            apiState = 1;
        }

        Database testDB = TestUtils.getTestDB(true);
        SearchCoordinate searchCoordinate = new SearchCoordinate(testDB, apiKey, 50
                , TestUtils.LONGRI_HOME_COORDS, 50000, apiState);

        StringWriter writer = new StringWriter();
        Json json = new Json(JsonWriter.OutputType.json);
        json.setWriter(writer);
        searchCoordinate.getRequest(json);

        String actual = writer.toString();
        assertEquals(expected, actual);

        testDB.close();
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (!testDB.isInMemory()) testDB.getFileHandle().delete();

    }

}
