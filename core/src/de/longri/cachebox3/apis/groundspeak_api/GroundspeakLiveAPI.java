/*
 * Copyright (C) 2014 - 2018 team-cachebox.de
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
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.Timer;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.apis.groundspeak_api.json_parser.stream_parser.CheckCacheStateParser;
import de.longri.cachebox3.apis.groundspeak_api.search.SearchGC;
import de.longri.cachebox3.callbacks.GenericCallBack;
import de.longri.cachebox3.settings.Config;
import de.longri.cachebox3.sqlite.Database;
import de.longri.cachebox3.sqlite.DatabaseSchema;
import de.longri.cachebox3.types.AbstractCache;
import de.longri.cachebox3.types.MutableCache;
import de.longri.cachebox3.types.Trackable;
import de.longri.cachebox3.utils.ICancel;
import de.longri.cachebox3.utils.NamedRunnable;
import de.longri.cachebox3.utils.NetUtils;
import de.longri.cachebox3.utils.json_parser.DraftUploadResultParser;
import de.longri.gdx.sqlite.GdxSqlite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static de.longri.cachebox3.apis.groundspeak_api.PostRequest.GS_LIVE_URL;
import static de.longri.cachebox3.apis.groundspeak_api.PostRequest.STAGING_GS_LIVE_URL;

public class GroundspeakLiveAPI {

    private static final Logger log = LoggerFactory.getLogger(GroundspeakLiveAPI.class);


    public static String LastAPIError = "";
    public static boolean CacheStatusValid = false;
    public static int CachesLeft = -1;
    public static int CurrentCacheCount = -1;
    public static int MaxCacheCount = -1;
    public static boolean CacheStatusLiteValid = false;
    public static int CachesLeftLite = -1;
    public static int CurrentCacheCountLite = -1;
    public static int MaxCacheCountLite = -1;
    public static String memberName = ""; // this will be filled by
    private static boolean DownloadLimit = false;
    private static boolean API_isChecked = false;
    private static Limit apiCallLimit;

    /**
     * 0: Guest??? 1: Basic 2: Charter??? 3: Premium
     */
    private static ApiResultState membershipType = ApiResultState.UNKNOWN;


    /**
     * Read the encrypted AccessToken from the config and check whether it is correct for Android CB
     *
     * @return
     */
    public static String getAccessToken() {
        return getAccessToken(false);
    }


    public static String getAccessToken(boolean Url_Codiert) {
        String act = "";
        if (Config.UseTestUrl.getValue()) {
            act = Config.AccessTokenForTest.getValue();
        } else {
            act = Config.AccessToken.getValue();
        }

        // Prüfen, ob das AccessToken für ACB ist!!!
        if (!(act.startsWith("A")))
            return "";
        String result = act.substring(1, act.length());

        // URL encoder
        if (Url_Codiert) {
            result = getUrlCodiert(result);
        }

        return result;
    }

    public static String getUrlCodiert(String value) {
        value = value.replace("/", "%2F");
        value = value.replace("\\", "%5C");
        value = value.replace("+", "%2B");
        value = value.replace("=", "%3D");
        return value;
    }

    private static String GetUTCDate(Date date) {
        long utc = date.getTime();
        // date.getTime already returns utc timestamp. Conversion to utc is not necessary!!!
        // TimeZone tz = TimeZone.getDefault();
        TimeZone tzp = TimeZone.getTimeZone("GMT-8");
        // int offset = tz.getOffset(utc);
        utc += /* offset */-tzp.getOffset(utc);
        return "\\/Date(" + utc + ")\\/";
    }

    private static String ConvertNotes(String note) {
        String result = note.replace("\r", "");
        result = result.replace("\"", "\\\"");
        return result.replace("\n", "\\n");
    }


    public static ApiResultState createDraftAndPublish(String cacheCode, int wptLogTypeId, Date dateLogged, String note, boolean directLog, final ICancel icancel) {
        ApiResultState chk = chkMembership(true);
        if (chk.isErrorState())
            return chk;


        waitApiCallLimit();


        String URL = Config.UseTestUrl.getValue() ? STAGING_GS_LIVE_URL : GS_LIVE_URL;

        try {
            String requestString = "";
            requestString = "{";
            requestString += "\"AccessToken\":\"" + getAccessToken() + "\",";
            requestString += "\"CacheCode\":\"" + cacheCode + "\",";
            requestString += "\"WptLogTypeId\":" + String.valueOf(wptLogTypeId) + ",";
            requestString += "\"UTCDateLogged\":\"" + GetUTCDate(dateLogged) + "\",";
            requestString += "\"Note\":\"" + ConvertNotes(note) + "\",";
            if (directLog) {
                requestString += "\"PromoteToLog\":true,";
            } else {
                requestString += "\"PromoteToLog\":false,";
            }

            requestString += "\"FavoriteThisCache\":false";
            requestString += "}";

            Net.HttpRequest httpPost = new Net.HttpRequest(Net.HttpMethods.POST);
            httpPost.setUrl(URL + "createFieldNoteAndPublish?format=json");
            httpPost.setTimeOut(Config.socket_timeout.getValue());
            httpPost.setHeader("format", "json");
            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("Content-type", "application/json");


            httpPost.setContent(requestString);

            // Execute HTTP Post Request
            String responseString = (String) NetUtils.postAndWait(NetUtils.ResultType.STRING, httpPost, icancel);

            if (responseString.contains("The service is unavailable")) {
                return ApiResultState.API_IS_UNAVAILABLE;
            }

            // Parse JSON Result
            if (DraftUploadResultParser.result(responseString)) {
                return ApiResultState.IO;
            }


        } catch (Exception e) {
            log.error("UploadDraftsAPI IOException", e);
            return ApiResultState.API_ERROR;
        }

        LastAPIError = "";
        return ApiResultState.API_ERROR;
    }
//
//
//    public static int GetCachesFound(final ICancel icancel) {
//
//	int chk = chkMembership(false);
//	if (chk < 0)
//	    return chk;
//
//	String URL = Config.StagingAPI.getValue() ? STAGING_GS_LIVE_URL : GS_LIVE_URL;
//
//	try {
//	    HttpPost httppost = new HttpPost(URL + "GetYourUserProfile?format=json");
//	    String requestString = "";
//	    requestString = "{";
//	    requestString += "\"AccessToken\":\"" + getAccessToken() + "\",";
//	    requestString += "\"ProfileOptions\":{";
//	    requestString += "}" + ",";
//	    requestString += getDeviceInfoRequestString();
//	    requestString += "}";
//
//	    httppost.setEntity(new ByteArrayEntity(requestString.getBytes("UTF8")));
//
//	    // set time outs
//	    HttpUtils.conectionTimeout = Config.conection_timeout.getValue();
//	    HttpUtils.socketTimeout = Config.socket_timeout.getValue();
//
//	    // Execute HTTP Post Request
//	    String result = HttpUtils.Execute(httppost, icancel);
//
//	    if (result.contains("The service is unavailable")) {
//		return API_IS_UNAVAILABLE;
//	    }
//
//	    try
//	    // Parse JSON Result
//	    {
//		JSONTokener tokener = new JSONTokener(result);
//		JSONObject json = (JSONObject) tokener.nextValue();
//		JSONObject status = json.getJSONObject("Status");
//		if (status.getInt("StatusCode") == 0) {
//		    result = "";
//		    JSONObject profile = json.getJSONObject("Profile");
//		    JSONObject user = profile.getJSONObject("User");
//		    return user.getInt("FindCount");
//
//		} else {
//		    result = "StatusCode = " + status.getInt("StatusCode") + "\n";
//		    result += status.getString("StatusMessage") + "\n";
//		    result += status.getString("ExceptionDetails");
//
//		    return ERROR;
//		}
//
//	    } catch (JSONException e) {
//		e.printStackTrace();
//	    }
//
//	} catch (ConnectTimeoutException e) {
//	    log.error("GetCachesFound ConnectTimeoutException", e);
//	    return CONNECTION_TIMEOUT;
//	} catch (UnsupportedEncodingException e) {
//	    log.error("GetCachesFound UnsupportedEncodingException", e);
//	    return ERROR;
//	} catch (ClientProtocolException e) {
//	    log.error("GetCachesFound ClientProtocolException", e);
//	    return ERROR;
//	} catch (IOException e) {
//	    log.error("GetCachesFound", e);
//	    return ERROR;
//	}
//
//	return (ERROR);
//    }
//

    /**
     * This method must call before every API-Call, for check any call restrictions!
     */
    public static int waitApiCallLimit() {
        return waitApiCallLimit(null);
    }

    public static int waitApiCallLimit(ICancel iCancel) {

        //Don't call and block GL thread
        if (CB.isGlThread()) throw new RuntimeException("Don't call and block GL thread");

        if (apiCallLimit == null) {
            if (Config.apiCallLimit.isExpired() || Config.apiCallLimit.getValue() < 1) {
                // get api limits from groundspeak
                int callsPerMinute = GetApiLimits.getLimit();

                if (callsPerMinute < 0) {
                    // API error, call cancel and give feedback
                    return callsPerMinute;
                }

                Calendar cal = Calendar.getInstance();
                if (callsPerMinute < 1) {
                    callsPerMinute = Config.apiCallLimit.getDefaultValue();
                } else {
                    //expired on end of this Month
                    cal.set(Calendar.DAY_OF_MONTH, 0);
                    cal.set(Calendar.HOUR_OF_DAY, 0);
                    cal.set(Calendar.MINUTE, 0);
                    cal.set(Calendar.SECOND, 0);
                    cal.set(Calendar.MILLISECOND, 0);
                    cal.add(Calendar.HOUR_OF_DAY, 24);
                    cal.add(Calendar.MONTH, 1);
                }

                log.debug("Set apiCallLimit to {} calls per minute", callsPerMinute);
                Config.apiCallLimit.setValue(callsPerMinute);

                log.debug("Set apiCallLimit expired on: {}", cal.getTime().toString());
                Config.apiCallLimit.setExpiredTime(cal.getTimeInMillis());
                Config.AcceptChanges();
            }
            apiCallLimit = new Limit(Config.apiCallLimit.getValue(), Calendar.MINUTE, 1);
        }
        apiCallLimit.waitForCall(iCancel);
        return 0;
    }


    /**
     * Loads the Membership type -1: Error 0: Guest??? 1: Basic 2: Charter??? 3: Premium
     */
    public static void getMembershipType(final GenericCallBack<ApiResultState> callBack) {
        if (API_isChecked) {
            callBack.callBack(membershipType);
            return;
        }

        // try to get type from settings
        if (Config.memberChipType.isExpired()) {
            CB.postAsync(new NamedRunnable("GroundspeakLiveAPI") {
                @Override
                public void run() {
                    final GetYourUserProfile getYourUserProfile = new GetYourUserProfile(getAccessToken());
                    getYourUserProfile.post(new GenericCallBack<ApiResultState>() {
                        @Override
                        public void callBack(ApiResultState value) {
                            if (value.isErrorState()) {
                                callBack.callBack(value);
                                return;
                            }
                            membershipType = getYourUserProfile.getMembershipType();
                            memberName = getYourUserProfile.getMemberName();
                            callBack.callBack(membershipType);
                            Config.memberChipType.setValue(membershipType.getState());

                            //expired on end of this day
                            Calendar cal = Calendar.getInstance();
                            cal.set(Calendar.HOUR_OF_DAY, 0);
                            cal.set(Calendar.MINUTE, 0);
                            cal.set(Calendar.SECOND, 0);
                            cal.set(Calendar.MILLISECOND, 0);
                            cal.add(Calendar.HOUR_OF_DAY, 24);
                            Config.memberChipType.setExpiredTime(cal.getTimeInMillis());
                            Config.AcceptChanges();

                            API_isChecked = true;
                        }
                    });
                }
            });
        } else {
            membershipType = ApiResultState.fromState(Config.memberChipType.getValue());
            callBack.callBack(membershipType);
            API_isChecked = true;
        }


    }


    public static ApiResultState getGeocacheStatus(Database database, Array<AbstractCache> caches, final ICancel icancel, CheckCacheStateParser.ProgressIncrement progressIncrement) {
        ApiResultState chk = chkMembership(false);
        if (chk.isErrorState())
            return chk;

        if (caches.size >= 110) throw new RuntimeException("Cache count must les then 110");

        waitApiCallLimit(icancel);
        String URL = Config.UseTestUrl.getValue() ? STAGING_GS_LIVE_URL : GS_LIVE_URL;

        Net.HttpRequest httpPost = new Net.HttpRequest(Net.HttpMethods.POST);
        httpPost.setUrl(URL + "getGeocacheStatus?format=json");
        httpPost.setTimeOut(Config.socket_timeout.getValue());
        httpPost.setHeader("format", "json");
        httpPost.setHeader("Accept", "application/json");
        httpPost.setHeader("Content-type", "application/json");

        String requestString = "";
        requestString = "{";
        requestString += "\"AccessToken\":\"" + getAccessToken() + "\",";
        requestString += "\"CacheCodes\":[";

        int i = 0;
        for (AbstractCache abstractCache : caches) {
            requestString += "\"" + abstractCache.getGcCode() + "\"";
            if (i < caches.size - 1)
                requestString += ",";
            i++;
        }

        requestString += "]";
        requestString += "}";

        httpPost.setContent(requestString);
        NetUtils.StreamHandleObject result = null;
        result = (NetUtils.StreamHandleObject) NetUtils.postAndWait(NetUtils.ResultType.STREAM, httpPost, icancel);

//        String debugStringResult = (String) NetUtils.postAndWait(NetUtils.ResultType.STRING, httpPost, icancel);


        if (icancel.cancel()) {
            if (result != null) result.handled();
            return ApiResultState.CANCELED;
        }
        CheckCacheStateParser parser = new CheckCacheStateParser();

        ApiResultState parseResult = parser.parse(database, result.stream, caches, icancel, progressIncrement);
        result.handled();
        return parseResult;
    }

    public static ApiResultState getGeocacheStatusFavoritePoints(final Database database, final Array<AbstractCache> caches, final ICancel icancel, final CheckCacheStateParser.ProgressIncrement progressIncrement) {
        ApiResultState chk = chkMembership(false);
        if (chk.isErrorState())
            return chk;

        if (caches.size >= 50) throw new RuntimeException("Cache count must les then 50");

        waitApiCallLimit(icancel);

        Array<String> gcCodes = new Array<>();
        for (AbstractCache ca : caches) {
            gcCodes.add(ca.getGcCode().toString());
        }

        final AtomicInteger idx = new AtomicInteger(0);

        //create inMemory DB
        GdxSqlite db = new GdxSqlite();
        db.openOrCreateDatabase();
        final DatabaseSchema sh = new DatabaseSchema();
        db.execSQL(sh.getEmptyNewDB());

        Database tmp = new Database(db);

        final SearchGC searchGC = new SearchGC(tmp, getAccessToken(), gcCodes, (byte) 2, icancel);
        searchGC.setIsLite(true);

        final ApiResultState result[] = new ApiResultState[1];
        final AtomicBoolean WAIT = new AtomicBoolean(true);
        searchGC.fireProgressEvent = false;
        searchGC.postRequest(new GenericCallBack<ApiResultState>() {
            @Override
            public void callBack(ApiResultState value) {
                result[0] = value;
                WAIT.set(false);
            }
        }, 0);

        while (WAIT.get()) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }


        //Store result from inMemory DB

        tmp.myDB.rawQuery("SELECT Id, BooleanStore, FavPoints, NumTravelbugs FROM CacheCoreInfo", new GdxSqlite.RowCallback() {
            @Override
            public void newRow(String[] columnName, Object[] value, int[] types) {
                //get cache from in list
                AbstractCache cache = null;
                long id = (long) value[0];
                int idx = 0;
                for (AbstractCache ca : caches) {
                    if (ca.getId() == id) {
                        cache = ca;
                        break;
                    }
                    idx++;
                }

                if (cache != null) {
                    cache.isChanged.set(false);
                    short booleanStore = ((Long) value[1]).shortValue();
                    boolean archieved = MutableCache.getMaskValue(MutableCache.MASK_ARCHIVED, booleanStore);
                    boolean availeble = MutableCache.getMaskValue(MutableCache.MASK_AVAILABLE, booleanStore);
                    short tbCount = (short) ((Long) value[3]).intValue();
                    int favPoints = ((Long) value[2]).intValue();

                    if (cache.isArchived() != archieved
                            || cache.isAvailable() != availeble
                            || cache.getNumTravelbugs() != tbCount
                            || cache.getFavoritePoints() != favPoints) {

                        cache.isChanged.set(false);
                        cache.setArchived(archieved);
                        cache.setAvailable(availeble);
                        cache.setNumTravelbugs(tbCount);
                        cache.setFavoritePoints(favPoints);
                    }

                    if (progressIncrement != null) progressIncrement.increment();
                }
            }
        });

        return result[0];
    }

    public static ApiResultState getAllImageLinks(String cacheCode, HashMap<String, URI> list, ICancel icancel) {
        ApiResultState chk = chkMembership(false);
        if (chk.isErrorState())
            return chk;

        String URL = Config.UseTestUrl.getValue() ? STAGING_GS_LIVE_URL : GS_LIVE_URL;
        if (list == null)
            list = new HashMap<String, URI>();

        waitApiCallLimit();
        try {
            Net.HttpRequest httpGet = new Net.HttpRequest(Net.HttpMethods.GET);
            httpGet.setUrl(URL + "GetImagesForGeocache?AccessToken=" + getAccessToken(true) + "&CacheCode=" + cacheCode + "&format=json");
            httpGet.setTimeOut(Config.socket_timeout.getValue());


            // Execute HTTP Post Request
            log.debug("Send Post request");
            String result = (String) NetUtils.postAndWait(NetUtils.ResultType.STRING, httpGet, icancel);

            if (result.contains("The service is unavailable")) {
                return ApiResultState.API_IS_UNAVAILABLE;
            }

            JsonValue root = new JsonReader().parse(result);
            JsonValue status = root.getChild("Status");
            if (status.getInt("StatusCode") == 0) {
                LastAPIError = "";
                JsonValue jImages = root.getChild("Images");
            }

//            JSONTokener tokener = new JSONTokener(result);
//            JSONObject json = (JSONObject) tokener.nextValue();
//            JSONObject status = json.getJSONObject("Status");
//            if (status.getInt("StatusCode") == 0) {
//                LastAPIError = "";
//                JSONArray jImages = json.getJSONArray("Images");
//
//                for (int ii = 0; ii < jImages.length(); ii++) {
//                    JSONObject jImage = (JSONObject) jImages.get(ii);
//                    String name = jImage.getString("Name");
//                    String uri = jImage.getString("Url");
//                    // ignore log images
//                    if (uri.contains("/cache/log"))
//                        continue; // LOG-Image
//                    // Check for duplicate name
//                    if (list.containsKey(name)) {
//                        for (int nr = 1; nr < 10; nr++) {
//                            if (list.containsKey(name + "_" + nr)) {
//                                continue; // Name already exists
//                            }
//                            name += "_" + nr;
//                            break;
//                        }
//                    }
//                    list.put(name, new URI(uri));
//                }
//                return IO;
//            } else if (status.getInt("StatusCode") == 140) {
//                return 140; // API-Limit überschritten -> nach etwas Verzögerung wiederholen!
//            } else {
//                LastAPIError = "";
//                LastAPIError = "StatusCode = " + status.getInt("StatusCode") + "\n";
//                LastAPIError += status.getString("StatusMessage") + "\n";
//                LastAPIError += status.getString("ExceptionDetails");
//
//                list = null;
//                return ERROR;
//            }

        } catch (Exception e) {
            log.error("getAllImageLinks()", e);
            list = null;
            return ApiResultState.API_ERROR;
        }

        list = null;
        return ApiResultState.API_ERROR;
    }


    public static ApiResultState chkMembership(boolean withoutMsg) {
        final ApiResultState[] ret = {ApiResultState.UNKNOWN};

        if (API_isChecked && !membershipType.isErrorState()) {
            log.debug("Membership ist checked, return stored state {}", membershipType.getState());
            return membershipType;
        }

        if (getAccessToken().length() > 0) {
            final AtomicBoolean WAIT = new AtomicBoolean(true);
            getMembershipType(new GenericCallBack<ApiResultState>() {
                @Override
                public void callBack(ApiResultState value) {
                    ret[0] = value;
                    WAIT.set(false);
                }
            });
            CB.wait(WAIT);
        } else {
            ret[0] = ApiResultState.NO_API_KEY;
        }

        if (ret[0].isErrorState() && !withoutMsg) {
            CB.checkApiResultState(ret[0]);
        }

        return ret[0];
    }

    public static ApiResultState isValidAPI_Key(boolean withoutMsg) {
        if (API_isChecked)
            return membershipType;

        return chkMembership(withoutMsg);
    }

    /**
     * @param trackable
     * @param cacheCode
     * @param logTypeId
     * @param dateLogged
     * @param note
     * @return
     */
    public static ApiResultState createTrackableLog(Trackable trackable, String cacheCode, int logTypeId, Date dateLogged, String note, ICancel icancel) {
        return createTrackableLog(trackable.getTBCode(), trackable.getTrackingCode(), cacheCode, logTypeId, dateLogged, note, icancel);
    }

    /**
     * @param tbCode
     * @param trackingNumber
     * @param cacheCode
     * @param logTypeId
     * @param dateLogged
     * @param note
     * @return
     */
    public static ApiResultState createTrackableLog(String tbCode, String trackingNumber, String cacheCode, int logTypeId, Date dateLogged, String note, ICancel icancel) {
        ApiResultState chk = chkMembership(false);
        if (chk.isErrorState())
            return chk;
        String URL = Config.UseTestUrl.getValue() ? STAGING_GS_LIVE_URL : GS_LIVE_URL;
        if (cacheCode == null)
            cacheCode = "";

        try {
            Net.HttpRequest httpPost = new Net.HttpRequest(Net.HttpMethods.POST);
            httpPost.setUrl(URL + "CreateTrackableLog?format=json");
            httpPost.setTimeOut(Config.socket_timeout.getValue());
            httpPost.setHeader("format", "json");
            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("Content-type", "application/json");


            String requestString;
            requestString = "{";
            requestString += "\"AccessToken\":\"" + getAccessToken() + "\",";
            requestString += "\"CacheCode\":\"" + cacheCode + "\",";
            requestString += "\"LogType\":" + String.valueOf(logTypeId) + ",";
            requestString += "\"UTCDateLogged\":\"" + GetUTCDate(dateLogged) + "\",";
            requestString += "\"Note\":\"" + ConvertNotes(note) + "\",";
            requestString += "\"TravelBugCode\":\"" + String.valueOf(tbCode) + "\",";
            requestString += "\"TrackingNumber\":\"" + String.valueOf(trackingNumber) + "\"";
            requestString += "}";

            httpPost.setContent(requestString);
            String result = (String) NetUtils.postAndWait(NetUtils.ResultType.STRING, httpPost, icancel);

            if (result.contains("The service is unavailable")) {
                return ApiResultState.API_IS_UNAVAILABLE;
            }
            // Parse JSON Result
            //TODO parse API ERROR


        } catch (Exception e) {
            log.error("createTrackableLog IOException", e);
            return ApiResultState.API_ERROR;
        }

        LastAPIError = "";
        return ApiResultState.IO;
    }

    public static boolean mAPI_isChecked() {
        return API_isChecked;
    }

    public static boolean ApiLimit() {
        return DownloadLimit;
    }

    public static void setDownloadLimit() {
        DownloadLimit = true;
    }

    public static boolean isPremiumMember() {
        final AtomicBoolean WAIT = new AtomicBoolean(true);

        if (membershipType == ApiResultState.UNKNOWN)
            getMembershipType(new GenericCallBack<ApiResultState>() {
                @Override
                public void callBack(ApiResultState value) {
                    membershipType = value;
                    log.debug("result for ask Member Type:{}", value);

                    Timer.schedule(new Timer.Task() {
                        @Override
                        public void run() {
                            //wait 2 sec! We can call the Groundspeak API only every 2 seconds
                            WAIT.set(false);
                        }
                    }, 2);


                }
            });
        else WAIT.set(false);

        if (CB.isGlThread())
            throw new RuntimeException("Call isPremiumMember() not on Main Thread, this will block for wait on a online result");

        while (WAIT.get()) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return membershipType == ApiResultState.MEMBERSHIP_TYPE_PREMIUM;
    }

    public static void setTestMembershipType(ApiResultState value) {
        membershipType = value;
    }

    public static void resetApiIsChecked() {
        API_isChecked = false;
    }
}
