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
package de.longri.cachebox3.apis.groundspeak_api;

import com.badlogic.gdx.Net;
import com.badlogic.gdx.net.HttpStatus;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.TestUtils;
import de.longri.cachebox3.callbacks.GenericCallBack;
import org.junit.jupiter.api.Test;
import travis.EXCLUDE_FROM_TRAVIS;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Created by longri on 14.04.17.
 */
class GetYourUserProfileTest {

    static {
        TestUtils.initialGdx();
    }

    final String apiKey = EXCLUDE_FROM_TRAVIS.GcAPI;
    final boolean isDummy = apiKey.equals(EXCLUDE_FROM_TRAVIS.DUMMY_API_KEY);

    private final String API_RESULT_JSON = "{\n" +
            "  \"Status\": {\n" +
            "    \"StatusCode\": 0,\n" +
            "    \"StatusMessage\": \"OK\",\n" +
            "    \"ExceptionDetails\": \"\",\n" +
            "    \"Warnings\": []\n" +
            "  },\n" +
            "  \"Profile\": {\n" +
            "    \"Challenges\": null,\n" +
            "    \"FavoritePoints\": null,\n" +
            "    \"Geocaches\": null,\n" +
            "    \"PublicProfile\": null,\n" +
            "    \"Souvenirs\": [],\n" +
            "    \"Stats\": {\n" +
            "      \"AccountsLogged\": 375423,\n" +
            "      \"ActiveCaches\": 2994885,\n" +
            "      \"ActiveCountries\": 222,\n" +
            "      \"NewLogs\": 6762575\n" +
            "    }\n" +
            "    \"Trackables\": null,\n" +
            "    \"User\": {\n" +
            "      \"AvatarUrl\": \"https:\\/\\/d1qqxh9zzqprtj.cloudfront.net\\/avatar\\/06d142.jpg\",\n" +
            "      \"FindCount\": 540,\n" +
            "      \"GalleryImageCount\": 31,\n" +
            "      \"HideCount\": 1,\n" +
            "      \"HomeCoordinates\": {\n" +
            "        \"Latitude\": 52.0,\n" +
            "        \"Longitude\": 13.0\n" +
            "      },\n" +
            "      \"Id\": 3852862,\n" +
            "      \"IsAdmin\": false,\n" +
            "      \"MemberType\": {\n" +
            "        \"MemberTypeId\": 3,\n" +
            "        \"MemberTypeName\": \"Premium\"\n" +
            "      },\n" +
            "      \"PublicGuid\": \"??????????????????????\",\n" +
            "      \"UserName\": \"LONGRI\"\n" +
            "    },\n" +
            "    \"EmailData\": null\n" +
            "  }\n" +
            "}";


    @Test
    void getRequest() throws IOException {
        String expected = TestUtils.getResourceRequestString("testsResources/GetYourUserProfile_request.txt",
                isDummy ? null : apiKey);
        GetYourUserProfile getYourUserProfile = new GetYourUserProfile(apiKey);

        StringWriter writer = new StringWriter();
        Json json = new Json(JsonWriter.OutputType.json);
        json.setWriter(writer);

        json.writeObjectStart();
        getYourUserProfile.getRequest(json);
        json.writeObjectEnd();

        String actual = writer.toString();
        assertEquals(expected, actual, "Should be equals");
    }

    @Test
    void parseResult() {
        final GetYourUserProfile getYourUserProfile = new GetYourUserProfile(apiKey);
        Net.HttpResponse response = new Net.HttpResponse() {
            @Override
            public byte[] getResult() {
                return new byte[0];
            }

            @Override
            public String getResultAsString() {
                return API_RESULT_JSON;
            }

            @Override
            public InputStream getResultAsStream() {
                return null;
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
        getYourUserProfile.handleHttpResponse(response, new GenericCallBack<ApiResultState>() {
            @Override
            public void callBack(ApiResultState value) {
                assertThat("Type should be 3", getYourUserProfile.getMembershipType() == ApiResultState.MEMBERSHIP_TYPE_PREMIUM);
                assertEquals(getYourUserProfile.getMemberName(), "LONGRI", "name should be LONGRI");
            }
        });
    }

    @Test
    void onlineTest() {
        if (isDummy) return;
        final GetYourUserProfile getYourUserProfile = new GetYourUserProfile(apiKey);

        final AtomicBoolean WAIT = new AtomicBoolean(true);
        final AtomicBoolean apiKeyExpired = new AtomicBoolean(false);
        getYourUserProfile.post(new GenericCallBack<ApiResultState>() {
            @Override
            public void callBack(ApiResultState value) {
                if (value == ApiResultState.EXPIRED_API_KEY) apiKeyExpired.set(true);
                WAIT.set(false);
            }
        });

        CB.wait(WAIT);

        if (apiKeyExpired.get()) {
            assertThat("API key expired, can't test!", false);
        } else {
            assertThat("Type should be 3", getYourUserProfile.getMembershipType() == ApiResultState.MEMBERSHIP_TYPE_PREMIUM);
            assertEquals(getYourUserProfile.getMemberName(), "Katipa", "name should be Katipa");
        }


    }
}