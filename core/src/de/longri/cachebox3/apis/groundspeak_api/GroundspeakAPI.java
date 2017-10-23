/* 
 * Copyright (C) 2014 - 2017 team-cachebox.de
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
import de.longri.cachebox3.callbacks.GenericCallBack;
import de.longri.cachebox3.gui.events.CacheListChangedEventList;
import de.longri.cachebox3.settings.Config;
import de.longri.cachebox3.sqlite.Database;
import de.longri.cachebox3.sqlite.dao.*;
import de.longri.cachebox3.types.*;
import de.longri.cachebox3.utils.ICancel;
import de.longri.cachebox3.utils.NetUtils;
import de.longri.cachebox3.utils.json_parser.DraftUploadResultParser;
import de.longri.cachebox3.utils.lists.CB_List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicBoolean;

import static de.longri.cachebox3.apis.groundspeak_api.PostRequest.GS_LIVE_URL;
import static de.longri.cachebox3.apis.groundspeak_api.PostRequest.STAGING_GS_LIVE_URL;

public class GroundspeakAPI {

    private static final Logger log = LoggerFactory.getLogger(GroundspeakAPI.class);


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
        if (Config.StagingAPI.getValue()) {
            act = Config.GcAPIStaging.getValue();
        } else {
            act = Config.GcAPI.getValue();
        }

        // Prüfen, ob das AccessToken für ACB ist!!!
        if (!(act.startsWith("A")))
            return "";
        String result = act.substring(1, act.length());

        // URL encoder
        if (Url_Codiert) {
            result = result.replace("/", "%2F");
            result = result.replace("\\", "%5C");
            result = result.replace("+", "%2B");
            result = result.replace("=", "%3D");
        }

        return result;
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


        String URL = Config.StagingAPI.getValue() ? STAGING_GS_LIVE_URL : GS_LIVE_URL;

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
    public static void waitApiCallLimit() {
        waitApiCallLimit(null);
    }

    public static void waitApiCallLimit(ICancel iCancel) {

        //Don't call and block GL thread
        if (CB.isMainThread()) throw new RuntimeException("Don't call and block GL thread");

        if (apiCallLimit == null) {
            if (Config.apiCallLimit.isExpired() || Config.apiCallLimit.getValue() < 1) {
                // get api limits from groundspeak
                int callsPerMinute = GetApiLimits.getLimit();
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
            CB.postAsync(new Runnable() {
                @Override
                public void run() {
                    log.debug(("API is not checked, call API check"));
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


    public static ApiResultState getGeocacheStatus(Array<AbstractCache> caches, final ICancel icancel, CheckCacheStateParser.ProgressIncrement progressIncrement) {
        ApiResultState chk = chkMembership(false);
        if (chk.isErrorState())
            return chk;

        if (caches.size >= 110) throw new RuntimeException("Cache count must les then 110");

        waitApiCallLimit();
        String URL = Config.StagingAPI.getValue() ? STAGING_GS_LIVE_URL : GS_LIVE_URL;

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
        NetUtils.StreamHandleObject result = (NetUtils.StreamHandleObject) NetUtils.postAndWait(NetUtils.ResultType.STREAM, httpPost, icancel);
        if (icancel.cancel()) {
            if (result != null) result.handled();
            return ApiResultState.CANCELED;
        }
        CheckCacheStateParser parser = new CheckCacheStateParser();
        ApiResultState parseResult = parser.parse(result.stream, caches, icancel, progressIncrement);
        result.handled();
        return parseResult;
    }
//
//    /**
//     * Gets the Logs for the given Cache
//     *
//     * @param Staging
//     *            Config.settings.StagingAPI.getValue()
//     * @param accessToken
//     * @param conectionTimeout
//     *            Config.settings.conection_timeout.getValue()
//     * @param socketTimeout
//     *            Config.settings.socket_timeout.getValue()
//     * @param cache
//     * @return
//     */
//    public static int GetGeocacheLogsByCache(Cache cache, ArrayList<LogEntry> logList, boolean all, cancelRunnable cancelRun) {
//	String finders = Config.Friends.getValue();
//	String[] finder = finders.split("\\|");
//	ArrayList<String> finderList = new ArrayList<String>();
//	for (String f : finder) {
//	    finderList.add(f);
//	}
//
//	if (cache == null)
//	    return -3;
//	int chk = chkMembership(false);
//	if (chk < 0)
//	    return chk;
//
//	try {
//	    Thread.sleep(1000);
//	} catch (InterruptedException e1) {
//	    e1.printStackTrace();
//	}
//
//	int start = 1;
//	int count = 30;
//
//	String URL = Config.StagingAPI.getValue() ? STAGING_GS_LIVE_URL : GS_LIVE_URL;
//	while (!cancelRun.cancel() && (finderList.size() > 0 || all))
//	// Schleife, solange bis entweder keine Logs mehr geladen werden oder bis alle Logs aller Finder geladen sind.
//	{
//	    try {
//		String requestString = "";
//		requestString += "&AccessToken=" + getAccessToken();
//		requestString += "&CacheCode=" + cache.getGcCode();
//		requestString += "&StartIndex=" + start;
//		requestString += "&MaxPerPage=" + count;
//		HttpGet httppost = new HttpGet(URL + "GetGeocacheLogsByCacheCode?format=json" + requestString);
//
//		// set time outs
//		HttpUtils.conectionTimeout = Config.conection_timeout.getValue();
//		HttpUtils.socketTimeout = Config.socket_timeout.getValue();
//
//		// Execute HTTP Post Request
//		String result = HttpUtils.Execute(httppost, cancelRun);
//
//		if (result.contains("The service is unavailable")) {
//		    return API_IS_UNAVAILABLE;
//		}
//		try
//		// Parse JSON Result
//		{
//		    JSONTokener tokener = new JSONTokener(result);
//		    JSONObject json = (JSONObject) tokener.nextValue();
//		    JSONObject status = json.getJSONObject("Status");
//		    if (status.getInt("StatusCode") == 0) {
//			result = "";
//			JSONArray geocacheLogs = json.getJSONArray("Logs");
//			for (int ii = 0; ii < geocacheLogs.length(); ii++) {
//			    JSONObject jLogs = (JSONObject) geocacheLogs.get(ii);
//			    JSONObject jFinder = (JSONObject) jLogs.get("Finder");
//			    JSONObject jLogType = (JSONObject) jLogs.get("LogType");
//			    LogEntry log = new LogEntry();
//			    log.CacheId = cache.Id;
//			    log.Comment = jLogs.getString("LogText");
//			    log.Finder = jFinder.getString("UserName");
//			    if (!finderList.contains(log.Finder)) {
//				continue;
//			    }
//			    finderList.remove(log.Finder);
//			    log.Id = jLogs.getInt("ID");
//			    log.Timestamp = new Date();
//			    try {
//				String dateCreated = jLogs.getString("VisitDate");
//				int date1 = dateCreated.indexOf("/Date(");
//				int date2 = dateCreated.indexOf("-");
//				String date = (String) dateCreated.subSequence(date1 + 6, date2);
//				log.Timestamp = new Date(Long.valueOf(date));
//			    } catch (Exception exc) {
//				log.error("SearchForGeocaches_ParseLogDate", exc);
//			    }
//			    log.Type = LogTypes.GC2CB_LogType(jLogType.getInt("WptLogTypeId"));
//			    logList.add(log);
//
//			}
//
//			if ((geocacheLogs.length() < count) || (finderList.size() == 0)) {
//			    return 0; // alle Logs des Caches geladen oder alle gesuchten Finder gefunden
//			}
//		    } else {
//			result = "StatusCode = " + status.getInt("StatusCode") + "\n";
//			result += status.getString("StatusMessage") + "\n";
//			result += status.getString("ExceptionDetails");
//			LastAPIError = result;
//			return (-1);
//		    }
//
//		} catch (JSONException e) {
//		    e.printStackTrace();
//		}
//
//	    } catch (ConnectTimeoutException e) {
//		log.error("GetGeocacheLogsByCache ConnectTimeoutException", e);
//		return CONNECTION_TIMEOUT;
//	    } catch (UnsupportedEncodingException e) {
//		log.error("GetGeocacheLogsByCache UnsupportedEncodingException", e);
//		return ERROR;
//	    } catch (ClientProtocolException e) {
//		log.error("GetGeocacheLogsByCache ClientProtocolException", e);
//		return ERROR;
//	    } catch (IOException e) {
//		log.error("GetGeocacheLogsByCache IOException", e);
//		return ERROR;
//	    }
//	    // die nächsten Logs laden
//	    start += count;
//	}
//	return (-1);
//    }
//
//    /**
//     * returns Status Code (0 -> OK)
//     *
//     */
//    public static int GetCacheLimits(ICancel icancel) {
//	if (CachesLeft > -1)
//	    return 0;
//
//	int chk = chkMembership(false);
//	if (chk < 0)
//	    return chk;
//
//	String URL = Config.StagingAPI.getValue() ? STAGING_GS_LIVE_URL : GS_LIVE_URL;
//
//	LastAPIError = "";
//	// zum Abfragen der CacheLimits einfach nach einem Cache suchen, der
//	// nicht existiert.
//	// dadurch wird der Zähler nicht erhöht, die Limits aber zurückgegeben.
//	try {
//	    HttpPost httppost = new HttpPost(URL + "SearchForGeocaches?format=json");
//	    try {
//		JSONObject request = new JSONObject();
//		request.put("AccessToken", getAccessToken());
//		request.put("IsLight", false);
//		request.put("StartIndex", 0);
//		request.put("MaxPerPage", 1);
//		request.put("GeocacheLogCount", 0);
//		request.put("TrackableLogCount", 0);
//		JSONObject requestcc = new JSONObject();
//		JSONArray requesta = new JSONArray();
//		requesta.put("GCZZZZZ");
//		requestcc.put("CacheCodes", requesta);
//		request.put("CacheCode", requestcc);
//
//		String requestString = request.toString();
//
//		httppost.setEntity(new ByteArrayEntity(requestString.getBytes("UTF8")));
//
//		// set time outs
//		HttpUtils.conectionTimeout = Config.conection_timeout.getValue();
//		HttpUtils.socketTimeout = Config.socket_timeout.getValue();
//
//		// Execute HTTP Post Request
//		String result = HttpUtils.Execute(httppost, icancel);
//
//		if (result.contains("The service is unavailable")) {
//		    return API_IS_UNAVAILABLE;
//		}
//		// Parse JSON Result
//
//		JSONTokener tokener = new JSONTokener(result);
//		JSONObject json = (JSONObject) tokener.nextValue();
//		int status = checkCacheStatus(json, false);
//		// hier keine Überprüfung des Status, da dieser z.B. 118
//		// (Überschreitung des Limits) sein kann, aber der CacheStatus
//		// aber trotzdem drin ist.
//		return status;
//	    } catch (JSONException e) {
//		e.printStackTrace();
//		LastAPIError = "API Error: " + e.getMessage();
//		return -2;
//	    }
//
//	} catch (ConnectTimeoutException e) {
//	    log.error("getGeocacheStatus ConnectTimeoutException", e);
//	    return CONNECTION_TIMEOUT;
//	} catch (UnsupportedEncodingException e) {
//	    log.error("getGeocacheStatus UnsupportedEncodingException", e);
//	    return ERROR;
//	} catch (ClientProtocolException e) {
//	    log.error("getGeocacheStatus ClientProtocolException", e);
//	    return ERROR;
//	} catch (IOException e) {
//	    log.error("getGeocacheStatus IOException", e);
//	    return ERROR;
//	}
//    }
//
//    // liest den CacheStatus aus dem gegebenen json Object aus.
//    // darin ist gespeichert, wie viele Full Caches schon geladen wurden und wie
//    // viele noch frei sind
//    static int checkCacheStatus(JSONObject json, boolean isLite) {
//	LastAPIError = "";
//	try {
//	    JSONObject cacheLimits = json.getJSONObject("CacheLimits");
//	    if (isLite) {
//		CachesLeftLite = cacheLimits.getInt("CachesLeft");
//		CurrentCacheCountLite = cacheLimits.getInt("CurrentCacheCount");
//		MaxCacheCountLite = cacheLimits.getInt("MaxCacheCount");
//		CacheStatusLiteValid = true;
//	    } else {
//		CachesLeft = cacheLimits.getInt("CachesLeft");
//		CurrentCacheCount = cacheLimits.getInt("CurrentCacheCount");
//		MaxCacheCount = cacheLimits.getInt("MaxCacheCount");
//		CacheStatusValid = true;
//	    }
//	    return 0;
//	} catch (Exception e) {
//	    e.printStackTrace();
//	    System.out.println(e.getMessage());
//	    LastAPIError = "API Error: " + e.getMessage();
//	    return -4;
//	}
//    }
//

    //    }
//
//    /**
//     * Ruft die Liste der TBs ab, die im Besitz des Users sind
//     *
//     * @param Staging
//     *            Config.settings.StagingAPI.getValue()
//     * @param String
//     *            accessToken
//     * @param conectionTimeout
//     *            Config.settings.conection_timeout.getValue()
//     * @param socketTimeout
//     *            Config.settings.socket_timeout.getValue()
//     * @param TbList
//     *            list
//     * @return
//     */
//    public static int getMyTbList(TbList list, ICancel icancel) {
//	int chk = chkMembership(false);
//	if (chk < 0)
//	    return chk;
//
//	String URL = Config.StagingAPI.getValue() ? STAGING_GS_LIVE_URL : GS_LIVE_URL;
//
//	try {
//	    HttpPost httppost = new HttpPost(URL + "GetUsersTrackables?format=json");
//	    try {
//		/*
//			"AccessToken":"String content",
//			"StartIndex":2147483647,
//			"MaxPerPage":2147483647,
//			"TrackableLogsCount":2147483647,
//			"CollectionOnly":true
//		*/
//		JSONObject request = new JSONObject();
//		request.put("AccessToken", getAccessToken());
//		request.put("MaxPerPage", 30);
//
//		String requestString = request.toString();
//
//		httppost.setEntity(new ByteArrayEntity(requestString.getBytes("UTF8")));
//
//		// set time outs
//		HttpUtils.conectionTimeout = Config.conection_timeout.getValue();
//		HttpUtils.socketTimeout = Config.socket_timeout.getValue();
//
//		// Execute HTTP Post Request
//		String result = HttpUtils.Execute(httppost, icancel);
//
//		if (result.contains("The service is unavailable")) {
//		    return API_IS_UNAVAILABLE;
//		}
//
//		// Parse JSON Result
//		JSONTokener tokener = new JSONTokener(result);
//		JSONObject json = (JSONObject) tokener.nextValue();
//		JSONObject status = json.getJSONObject("Status");
//		if (status.getInt("StatusCode") == 0) {
//		    LastAPIError = "";
//		    JSONArray jTrackables = json.getJSONArray("Trackables");
//
//		    for (int ii = 0; ii < jTrackables.length(); ii++) {
//			JSONObject jTrackable = (JSONObject) jTrackables.get(ii);
//			boolean InCollection = false;
//			try {
//			    InCollection = jTrackable.getBoolean("InCollection");
//			} catch (JSONException e) {
//			}
//			if (!InCollection)
//			    list.add(new Trackable(jTrackable));
//		    }
//		    return 0;
//		} else {
//		    LastAPIError = "";
//		    LastAPIError = "StatusCode = " + status.getInt("StatusCode") + "\n";
//		    LastAPIError += status.getString("StatusMessage") + "\n";
//		    LastAPIError += status.getString("ExceptionDetails");
//
//		    return (-1);
//		}
//
//	    } catch (JSONException e) {
//		e.printStackTrace();
//	    }
//
//	} catch (ConnectTimeoutException e) {
//	    log.error("getGeocacheStatus ConnectTimeoutException", e);
//	    return CONNECTION_TIMEOUT;
//	} catch (UnsupportedEncodingException e) {
//	    log.error("getGeocacheStatus UnsupportedEncodingException", e);
//	    return ERROR;
//	} catch (ClientProtocolException e) {
//	    log.error("getGeocacheStatus ClientProtocolException", e);
//	    return ERROR;
//	} catch (IOException e) {
//	    log.error("getGeocacheStatus IOException", e);
//	    return ERROR;
//	}
//
//	return (-1);
//    }
//
//    /**
//     * @param Staging
//     *            Config.settings.StagingAPI.getValue()
//     * @param accessToken
//     * @param TrackingCode
//     * @param TB
//     * @param conectionTimeout
//     *            Config.settings.conection_timeout.getValue()
//     * @param socketTimeout
//     *            Config.settings.socket_timeout.getValue()
//     * @return
//     */
//    public static int getTBbyTreckNumber(String TrackingCode, ByRef<Trackable> TB, ICancel icancel) {
//	int chk = chkMembership(false);
//	if (chk < 0)
//	    return chk;
//
//	String URL = Config.StagingAPI.getValue() ? STAGING_GS_LIVE_URL : GS_LIVE_URL;
//
//	try {
//	    HttpGet httppost = new HttpGet(URL + "GetTrackablesByTrackingNumber?AccessToken=" + getAccessToken(true) + "&trackingNumber=" + TrackingCode + "&format=json");
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
//	    try
//	    // Parse JSON Result
//	    {
//		JSONTokener tokener = new JSONTokener(result);
//		JSONObject json = (JSONObject) tokener.nextValue();
//		JSONObject status = json.getJSONObject("Status");
//		if (status.getInt("StatusCode") == 0) {
//		    LastAPIError = "";
//		    JSONArray jTrackables = json.getJSONArray("Trackables");
//
//		    for (int i = 0; i < jTrackables.length();) {
//			JSONObject jTrackable = (JSONObject) jTrackables.get(i);
//			TB.set(new Trackable(jTrackable));
//			TB.get().setTrackingCode(TrackingCode);
//			return IO;
//		    }
//		} else {
//		    LastAPIError = "";
//		    LastAPIError = "StatusCode = " + status.getInt("StatusCode") + "\n";
//		    LastAPIError += status.getString("StatusMessage") + "\n";
//		    LastAPIError += status.getString("ExceptionDetails");
//		    TB = null;
//		    return ERROR;
//		}
//
//	    } catch (JSONException e) {
//		e.printStackTrace();
//	    }
//
//	} catch (ConnectTimeoutException e) {
//	    log.error("getTBbyTreckNumber ConnectTimeoutException", e);
//	    TB = null;
//	    return CONNECTION_TIMEOUT;
//	} catch (UnsupportedEncodingException e) {
//	    log.error("getTBbyTreckNumber UnsupportedEncodingException", e);
//	    TB = null;
//	    return ERROR;
//	} catch (ClientProtocolException e) {
//	    log.error("getTBbyTreckNumber ClientProtocolException", e);
//	    TB = null;
//	    return ERROR;
//	} catch (IOException e) {
//	    log.error("getTBbyTreckNumber IOException", e);
//	    TB = null;
//	    return ERROR;
//	}
//
//	TB = null;
//	return ERROR;
//    }
//
//    /**
//     * @param Staging
//     *            Config.settings.StagingAPI.getValue()
//     * @param accessToken
//     * @param TrackingNumber
//     * @param TB
//     * @param conectionTimeout
//     *            Config.settings.conection_timeout.getValue()
//     * @param socketTimeout
//     *            Config.settings.socket_timeout.getValue()
//     * @return
//     */
//    public static int getTBbyTbCode(String TrackingNumber, ByRef<Trackable> TB, ICancel icancel) {
//	int chk = chkMembership(false);
//	if (chk < 0)
//	    return chk;
//	String URL = Config.StagingAPI.getValue() ? STAGING_GS_LIVE_URL : GS_LIVE_URL;
//
//	try {
//	    HttpGet httppost = new HttpGet(URL + "GetTrackablesByTBCode?AccessToken=" + getAccessToken(true) + "&tbCode=" + TrackingNumber + "&format=json");
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
//	    try
//	    // Parse JSON Result
//	    {
//		JSONTokener tokener = new JSONTokener(result);
//		JSONObject json = (JSONObject) tokener.nextValue();
//		JSONObject status = json.getJSONObject("Status");
//		if (status.getInt("StatusCode") == 0) {
//		    LastAPIError = "";
//		    JSONArray jTrackables = json.getJSONArray("Trackables");
//
//		    for (int ii = 0; ii < jTrackables.length();) {
//			JSONObject jTrackable = (JSONObject) jTrackables.get(ii);
//			TB.set(new Trackable(jTrackable));
//			return IO;
//		    }
//		} else {
//		    LastAPIError = "";
//		    LastAPIError = "StatusCode = " + status.getInt("StatusCode") + "\n";
//		    LastAPIError += status.getString("StatusMessage") + "\n";
//		    LastAPIError += status.getString("ExceptionDetails");
//
//		    return ERROR;
//		}
//
//	    } catch (JSONException e) {
//		e.printStackTrace();
//	    }
//
//	} catch (ConnectTimeoutException e) {
//	    log.error("getTBbyTbCode ConnectTimeoutException", e);
//	    TB = null;
//	    return CONNECTION_TIMEOUT;
//	} catch (UnsupportedEncodingException e) {
//	    log.error("getTBbyTbCode UnsupportedEncodingException", e);
//	    TB = null;
//	    return ERROR;
//	} catch (ClientProtocolException e) {
//	    log.error("getTBbyTbCode ClientProtocolException", e);
//	    TB = null;
//	    return ERROR;
//	} catch (IOException e) {
//	    log.error("getTBbyTbCode IOException", e);
//	    TB = null;
//	    return ERROR;
//	}
//
//	TB = null;
//	return ERROR;
//
//    }
//
//    /**
//     * Ruft die Liste der Bilder ab, die in einem Cache sind
//     *
//     * @param Staging
//     *            Config.settings.StagingAPI.getValue()
//     * @param String
//     *            accessToken
//     * @param TbList
//     *            list
//     * @return
//     */
//    public static int getImagesForGeocache(String cacheCode, ArrayList<String> images, ICancel icancel) {
//	int chk = chkMembership(false);
//	if (chk < 0)
//	    return chk;
//
//	String URL = Config.StagingAPI.getValue() ? STAGING_GS_LIVE_URL : GS_LIVE_URL;
//
//	try {
//	    HttpGet httppost = new HttpGet(URL + "GetImagesForGeocache?AccessToken=" + getAccessToken() + "&CacheCode=" + cacheCode + "&format=json");
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
//	    try
//	    // Parse JSON Result
//	    {
//		JSONTokener tokener = new JSONTokener(result);
//		JSONObject json = (JSONObject) tokener.nextValue();
//		JSONObject status = json.getJSONObject("Status");
//		if (status.getInt("StatusCode") == 0) {
//		    LastAPIError = "";
//		    JSONArray jImages = json.getJSONArray("Images");
//
//		    for (int ii = 0; ii < jImages.length(); ii++) {
//			JSONObject jImage = (JSONObject) jImages.get(ii);
//			images.add(jImage.getString("Url"));
//		    }
//		    return 0;
//		} else {
//		    LastAPIError = "";
//		    LastAPIError = "StatusCode = " + status.getInt("StatusCode") + "\n";
//		    LastAPIError += status.getString("StatusMessage") + "\n";
//		    LastAPIError += status.getString("ExceptionDetails");
//
//		    return (-1);
//		}
//
//	    } catch (JSONException e) {
//		e.printStackTrace();
//	    }
//
//	} catch (ConnectTimeoutException e) {
//	    log.error("getImagesForGeocache ConnectTimeoutException", e);
//	    return CONNECTION_TIMEOUT;
//	} catch (UnsupportedEncodingException e) {
//	    log.error("getImagesForGeocache UnsupportedEncodingException", e);
//	    return ERROR;
//	} catch (ClientProtocolException e) {
//	    log.error("getImagesForGeocache ClientProtocolException", e);
//	    return ERROR;
//	} catch (IOException e) {
//	    log.error("getImagesForGeocache IOException", e);
//	    return ERROR;
//	}
//
//	return (-1);
//    }
//
//    /**
//     * @param Staging
//     *            Config.settings.StagingAPI.getValue()
//     * @param accessToken
//     * @param cacheCode
//     * @param list
//     * @param conectionTimeout
//     *            Config.settings.conection_timeout.getValue()
//     * @param socketTimeout
//     *            Config.settings.socket_timeout.getValue()
//     * @return
//     */
    public static ApiResultState getAllImageLinks(String cacheCode, HashMap<String, URI> list, ICancel icancel) {
        ApiResultState chk = chkMembership(false);
        if (chk.isErrorState())
            return chk;

        String URL = Config.StagingAPI.getValue() ? STAGING_GS_LIVE_URL : GS_LIVE_URL;
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

    public static void WriteCachesLogsImages_toDB(CB_List<AbstractCache> apiCaches, CB_List<LogEntry> apiLogs, CB_List<ImageEntry> apiImages) throws InterruptedException {
        // Auf eventuellen Thread Abbruch reagieren
        Thread.sleep(2);

        Database.Data.beginTransaction();

        AbstractCacheDAO abstractCacheDAO = new CacheDAO();
        LogDAO logDAO = new LogDAO();
        ImageDAO imageDAO = new ImageDAO();
        AbstractWaypointDAO abstractWaypointDAO = new WaypointDAO();

        for (int c = 0; c < apiCaches.size; c++) {
            AbstractCache abstractCache = apiCaches.get(c);
            AbstractCache aktCache = Database.Data.Query.GetCacheById(abstractCache.getId());

            if (aktCache != null && aktCache.isLive())
                aktCache = null;

            if (aktCache == null) {
                aktCache = abstractCacheDAO.getFromDbByCacheId(Database.Data, abstractCache.getId());
            }
            // Read Detail Info of Cache if not available
            if ((aktCache != null) && (aktCache.getDetail() == null)) {
                aktCache.loadDetail();
            }
            // If Cache into DB, extract saved rating
            if (aktCache != null) {
                abstractCache.setRating(aktCache.getRating());
            }

            // Falls das Update nicht klappt (Cache noch nicht in der DB) Insert machen
            if (!abstractCacheDAO.updateDatabase(Database.Data, abstractCache)) {
                abstractCacheDAO.writeToDatabase(Database.Data, abstractCache);
            }

            // Notes von Groundspeak überprüfen und evtl. in die DB an die vorhandenen Notes anhängen
            if (abstractCache.getTmpNote() != null) {
                String oldNote = Database.getNote(abstractCache.getId());
                String newNote = "";
                if (oldNote == null) {
                    oldNote = "";
                }
                String begin = "<Import from Geocaching.com>";
                String end = "</Import from Geocaching.com>";
                int iBegin = oldNote.indexOf(begin);
                int iEnd = oldNote.indexOf(end);
                if ((iBegin >= 0) && (iEnd > iBegin)) {
                    // Note from Groundspeak already in Database
                    // -> Replace only this part in whole Note
                    newNote = oldNote.substring(0, iBegin - 1) + System.getProperty("line.separator"); // Copy the old part of Note before
                    // the beginning of the groundspeak
                    // block
                    newNote += begin + System.getProperty("line.separator");
                    newNote += abstractCache.getTmpNote();
                    newNote += System.getProperty("line.separator") + end;
                    newNote += oldNote.substring(iEnd + end.length(), oldNote.length());
                } else {
                    newNote = oldNote + System.getProperty("line.separator");
                    newNote += begin + System.getProperty("line.separator");
                    newNote += abstractCache.getTmpNote();
                    newNote += System.getProperty("line.separator") + end;
                }
                abstractCache.setTmpNote(newNote);
                Database.setNote(abstractCache.getId(), abstractCache.getTmpNote());
            }

            // Delete LongDescription from this Cache! LongDescription is Loading by showing DescriptionView direct from DB
            abstractCache.setLongDescription("");

            for (LogEntry log : apiLogs) {
                if (log.CacheId != abstractCache.getId())
                    continue;
                // Write Log to database

                logDAO.WriteToDatabase(log);
            }

            for (ImageEntry image : apiImages) {
                if (image.CacheId != abstractCache.getId())
                    continue;
                // Write Image to database

                imageDAO.WriteToDatabase(image, false);
            }

            for (int i = 0, n = abstractCache.getWaypoints().size; i < n; i++) {
                // must Cast to Full Waypoint. If Waypoint, is wrong createt!
                AbstractWaypoint waypoint = abstractCache.getWaypoints().get(i);
                boolean update = true;

                // dont refresh wp if aktCache.wp is user changed
                if (aktCache != null) {
                    if (aktCache.getWaypoints() != null) {
                        for (int j = 0, m = aktCache.getWaypoints().size; j < m; j++) {
                            AbstractWaypoint wp = aktCache.getWaypoints().get(j);
                            if (wp.getGcCode().toString().equalsIgnoreCase(waypoint.getGcCode().toString())) {
                                if (wp.isUserWaypoint())
                                    update = false;
                                break;
                            }
                        }
                    }
                }

                if (update) {
                    // do not store replication information when importing caches with GC api
                    if (!abstractWaypointDAO.updateDatabase(Database.Data, waypoint)) {
                        abstractWaypointDAO.writeToDatabase(Database.Data, waypoint); // do not store replication information here
                    }
                }

            }

            if (aktCache == null) {
                Database.Data.Query.add(abstractCache);
                // cacheDAO.writeToDatabase(cache);
            } else {
                Database.Data.Query.removeValue(Database.Data.Query.GetCacheById(abstractCache.getId()), false);
                Database.Data.Query.add(abstractCache);
                // cacheDAO.updateDatabase(cache);
            }

        }
        Database.Data.setTransactionSuccessful();
        Database.Data.endTransaction();
        Database.Data.gpxFilenameUpdateCacheCount();

        CacheListChangedEventList.Call();
    }


//

    //
//    /**
//     * Returns True if the APY-Key INVALID
//     *
//     * @param Staging
//     *            Config.settings.StagingAPI.getValue()
//     * @param accessToken
//     * @param conectionTimeout
//     *            Config.settings.conection_timeout.getValue()
//     * @param socketTimeout
//     *            Config.settings.socket_timeout.getValue()
//     * @return 0=false 1=true
//     */
    public static ApiResultState chkMembership(boolean withoutMsg) {
        if (API_isChecked) {
            return membershipType;
        }
        final ApiResultState[] ret = {ApiResultState.UNKNOWN};
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
        return createTrackableLog(trackable.getGcCode(), trackable.getTrackingNumber(), cacheCode, logTypeId, dateLogged, note, icancel);
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
        String URL = Config.StagingAPI.getValue() ? STAGING_GS_LIVE_URL : GS_LIVE_URL;
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

        if (CB.isMainThread())
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
