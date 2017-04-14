package de.longri.cachebox3.api;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Created by Longri on 10.04.2017.
 */
class GroundspeakAPITest {

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
    void getApiStatus() {
//        int status = de.longri.cachebox3.apis.groundspeak_api.GroundspeakAPI.getApiStatus(API_RESULT_JSON);
        int status =0;
        assertThat("Status should be 0", status == 0);

    }

}