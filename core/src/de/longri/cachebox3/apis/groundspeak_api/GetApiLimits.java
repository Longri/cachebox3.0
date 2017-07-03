package de.longri.cachebox3.apis.groundspeak_api;

import com.badlogic.gdx.Net;
import de.longri.cachebox3.apis.groundspeak_api.json_parser.stream_parser.ApiLimitParser;
import de.longri.cachebox3.apis.groundspeak_api.json_parser.stream_parser.CheckCacheStateParser;
import de.longri.cachebox3.settings.Config;
import de.longri.cachebox3.utils.NetUtils;

import static de.longri.cachebox3.apis.groundspeak_api.GroundspeakAPI.getAccessToken;
import static de.longri.cachebox3.apis.groundspeak_api.PostRequest.GS_LIVE_URL;
import static de.longri.cachebox3.apis.groundspeak_api.PostRequest.STAGING_GS_LIVE_URL;

/**
 * Created by longri on 01.07.17.
 */
public class GetApiLimits {

    // https://api.groundspeak.com/LiveV6/geocaching.svc/help/operations/GetAPILimits
    // https://api.groundspeak.com/LiveV6/geocaching.svc/GetAPILimits?accessToken={ACCESSTOKEN}

    public static int getLimit() {
        String URL = Config.StagingAPI.getValue() ? STAGING_GS_LIVE_URL : GS_LIVE_URL;

        Net.HttpRequest httpGet = new Net.HttpRequest(Net.HttpMethods.GET);
        httpGet.setUrl(URL + "GetAPILimits?AccessToken=" + getAccessToken(true) + "&format=json");

        NetUtils.StreamHandleObject result = (NetUtils.StreamHandleObject) NetUtils.postAndWait(NetUtils.ResultType.STREAM, httpGet, null);
        //  for debug: String result = (String) NetUtils.postAndWait(NetUtils.ResultType.STRING, httpGet, null);

        ApiLimitParser parser = new ApiLimitParser();
        int parseResult = parser.parseCallsPerMinute(result.stream);
        result.handled();
        return parseResult;
    }

}
