/* 
 * Copyright (C) 2014 team-cachebox.de
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


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.utils.JsonReader;
import de.longri.cachebox3.callbacks.GenericCallBack;
import de.longri.cachebox3.settings.Config;
import de.longri.cachebox3.utils.BuildInfo;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicInteger;

public class GroundspeakAPI {
    final static org.slf4j.Logger log = LoggerFactory.getLogger(GroundspeakAPI.class);
    public static final String GS_LIVE_URL = "https://api.groundspeak.com/LiveV6/geocaching.svc/";
    public static final String STAGING_GS_LIVE_URL = "https://staging.api.groundspeak.com/Live/V6Beta/geocaching.svc/";

    public static final int IO = 0;
    private static final int ERROR = -1;
    public static final int CONNECTION_TIMEOUT = -2;
    private static final int API_ERROR = -3;
    public static final int API_IS_UNAVAILABLE = -4;

    public static String LastAPIError = "";
    public static boolean CacheStatusValid = false;
    public static int CachesLeft = -1;
    public static int CurrentCacheCount = -1;
    public static int MaxCacheCount = -1;
    public static boolean CacheStatusLiteValid = false;
    public static int CachesLeftLite = -1;
    public static int CurrentCacheCountLite = -1;
    public static int MaxCacheCountLite = -1;
    public static String MemberName = ""; // this will be filled by
    private static boolean DownloadLimit = false;
    private static boolean API_isCheked = false;

    /**
     * Read the encrypted AccessToken from the config and check whether it is correct for Android CB
     *
     * @return
     */
    public static String GetAccessToken() {
        return GetAccessToken(false);
    }


    public static String GetAccessToken(boolean Url_Codiert) {
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


    /**
     * 0: Guest??? 1: Basic 2: Charter??? 3: Premium
     */
    private static int membershipType = -1;

//
//    public static boolean IsPremiumMember() {
//        if (membershipType < 0)
//            membershipType = GetMembershipType(null);
//        return membershipType == 3;
//    }

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


//    public static int CreateFieldNoteAndPublish(String cacheCode, int wptLogTypeId, Date dateLogged, String note, boolean directLog, final ICancel icancel) {
//	int chk = chkMembership(true);
//	if (chk < 0)
//	    return chk;
//
//	String URL = Config.StagingAPI.getValue() ? STAGING_GS_LIVE_URL : GS_LIVE_URL;
//
//	try {
//	    HttpPost httppost = new HttpPost(URL + "CreateFieldNoteAndPublish?format=json");
//	    String requestString = "";
//	    requestString = "{";
//	    requestString += "\"AccessToken\":\"" + GetAccessToken() + "\",";
//	    requestString += "\"CacheCode\":\"" + cacheCode + "\",";
//	    requestString += "\"WptLogTypeId\":" + String.valueOf(wptLogTypeId) + ",";
//	    requestString += "\"UTCDateLogged\":\"" + GetUTCDate(dateLogged) + "\",";
//	    requestString += "\"Note\":\"" + ConvertNotes(note) + "\",";
//	    if (directLog) {
//		requestString += "\"PromoteToLog\":true,";
//	    } else {
//		requestString += "\"PromoteToLog\":false,";
//	    }
//
//	    requestString += "\"FavoriteThisCache\":false";
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
//	    // Parse JSON Result
//	    try {
//		JSONTokener tokener = new JSONTokener(result);
//		JSONObject json = (JSONObject) tokener.nextValue();
//		JSONObject status = json.getJSONObject("Status");
//		if (status.getInt("StatusCode") == 0) {
//		    result = "";
//		    LastAPIError = "";
//		} else {
//		    result = "StatusCode = " + status.getInt("StatusCode") + "\n";
//		    result += status.getString("StatusMessage") + "\n";
//		    result += status.getString("ExceptionDetails");
//		    LastAPIError = result;
//		    return ERROR;
//		}
//
//	    } catch (JSONException e) {
//		e.printStackTrace();
//		logger.error("UploadFieldNotesAPI", e);
//		LastAPIError = e.getMessage();
//		return ERROR;
//	    }
//
//	} catch (ConnectTimeoutException e) {
//	    logger.error("UploadFieldNotesAPI ConnectTimeoutException", e);
//	    return CONNECTION_TIMEOUT;
//	} catch (UnsupportedEncodingException e) {
//	    logger.error("UploadFieldNotesAPI UnsupportedEncodingException", e);
//	    return ERROR;
//	} catch (ClientProtocolException e) {
//	    logger.error("UploadFieldNotesAPI ClientProtocolException", e);
//	    return ERROR;
//	} catch (IOException e) {
//	    logger.error("UploadFieldNotesAPI IOException", e);
//	    return ERROR;
//	}
//
//	LastAPIError = "";
//	return IO;
//    }
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
//	    requestString += "\"AccessToken\":\"" + GetAccessToken() + "\",";
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
//	    logger.error("GetCachesFound ConnectTimeoutException", e);
//	    return CONNECTION_TIMEOUT;
//	} catch (UnsupportedEncodingException e) {
//	    logger.error("GetCachesFound UnsupportedEncodingException", e);
//	    return ERROR;
//	} catch (ClientProtocolException e) {
//	    logger.error("GetCachesFound ClientProtocolException", e);
//	    return ERROR;
//	} catch (IOException e) {
//	    logger.error("GetCachesFound", e);
//	    return ERROR;
//	}
//
//	return (ERROR);
//    }
//

    /**
     * Loads the Membership type -1: Error 0: Guest??? 1: Basic 2: Charter??? 3: Premium
     */
    public static void GetMembershipType(final GenericCallBack<Integer> callBack) {
        if (API_isCheked) {
            callBack.callBack(membershipType);
            return;
        }

        String URL = Config.StagingAPI.getValue() ? STAGING_GS_LIVE_URL : GS_LIVE_URL;

        String requestString = "";
        requestString = "{";
        requestString += "\"AccessToken\":\"" + GetAccessToken() + "\",";
        requestString += "\"ProfileOptions\":{";
        requestString += "}" + ",";
        requestString += getDeviceInfoRequestString();
        requestString += "}";


        Net.HttpRequest httpPost = new Net.HttpRequest(Net.HttpMethods.POST);
        httpPost.setUrl(URL + "GetYourUserProfile?format=json");
        httpPost.setHeader("format", "json");
        httpPost.setHeader("Accept", "application/json");
        httpPost.setHeader("Content-type", "application/json");

        httpPost.setContent(requestString);
        httpPost.setIncludeCredentials(true);

        Gdx.net.sendHttpRequest(httpPost, new Net.HttpResponseListener() {
            public void handleHttpResponse(Net.HttpResponse httpResponse) {
                String result = httpResponse.getResultAsString();
                if (result.contains("The service is unavailable")) {
                    callBack.callBack(API_IS_UNAVAILABLE);
                    return;
                }
                int status = getApiStatus(result);

                if (status == 0) {
                    (new JsonReader() {
                        protected void number(String name, long value, String stringValue) {
                            super.number(name, value, stringValue);
                            if (name.equals("MemberTypeId")) {
                                membershipType = (int) value;
                            }
                        }

                        protected void string(String name, String value) {
                            super.string(name, value);
                            if (name.equals("UserName")) {
                                MemberName = value;
                            }
                        }

                    }).parse(result);
                    API_isCheked = true;
                    callBack.callBack(membershipType);
                } else if (status == 2 || status == 3 || status == 141) {
                    API_isCheked = false;
                    callBack.callBack(API_ERROR);
                } else {
                    log.warn("GetMembershipType API-Error: " + result);
                    API_isCheked = false;
                    callBack.callBack(API_ERROR);
                }
            }

            public void failed(Throwable t) {
                log.error("GetMembershipType", t);
                callBack.callBack(API_IS_UNAVAILABLE);
            }

            @Override
            public void cancelled() {
                log.error("cancelled GetMembershipType");
                callBack.callBack(API_IS_UNAVAILABLE);
            }
        });
    }


    public static int getApiStatus(String result) {
        final AtomicInteger st = new AtomicInteger(-1);
        (new JsonReader() {
            protected void number(String name, long value, String stringValue) {
                super.number(name, value, stringValue);
                if (name.equals("StatusCode")) {
                    st.set((int) value);
                }
            }
        }).parse(result);
        return st.get();
    }

    //
//    /**
//     * Gets the Status for the given Caches
//     *
//     * Staging
//     *            Config.settings.StagingAPI.getValue()
//     * accessToken
//     * conectionTimeout
//     *            Config.settings.conection_timeout.getValue()
//     * socketTimeout
//     *            Config.settings.socket_timeout.getValue()
//     * @param caches is also for return
//     * @return
//     */
//    public static int GetGeocacheStatus(ArrayList<Cache> caches, final ICancel icancel) {
//	int chk = chkMembership(false);
//	if (chk < 0)
//	    return chk;
//
//	try {
//	    Thread.sleep(2500);
//	} catch (InterruptedException e1) {
//	    e1.printStackTrace();
//	}
//
//	String URL = Config.StagingAPI.getValue() ? STAGING_GS_LIVE_URL : GS_LIVE_URL;
//
//	try {
//	    HttpPost httppost = new HttpPost(URL + "GetGeocacheStatus?format=json");
//	    String requestString = "";
//	    requestString = "{";
//	    requestString += "\"AccessToken\":\"" + GetAccessToken() + "\",";
//	    requestString += "\"CacheCodes\":[";
//
//	    int i = 0;
//	    for (Cache cache : caches) {
//		requestString += "\"" + cache.getGcCode() + "\"";
//		if (i < caches.size() - 1)
//		    requestString += ",";
//		i++;
//	    }
//
//	    requestString += "]";
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
//	    try
//	    // Parse JSON Result
//	    {
//		JSONTokener tokener = new JSONTokener(result);
//		JSONObject json = (JSONObject) tokener.nextValue();
//		JSONObject status = json.getJSONObject("Status");
//		if (status.getInt("StatusCode") == 0) {
//		    result = "";
//		    JSONArray geocacheStatuses = json.getJSONArray("GeocacheStatuses");
//		    for (int ii = 0; ii < geocacheStatuses.length(); ii++) {
//			JSONObject jCache = (JSONObject) geocacheStatuses.get(ii);
//
//			Iterator<Cache> iterator = caches.iterator();
//			do {
//			    Cache tmp = iterator.next();
//			    if (jCache.getString("CacheCode").equals(tmp.getGcCode())) {
//				tmp.setArchived(jCache.getBoolean("Archived"));
//				tmp.setAvailable(jCache.getBoolean("Available"));
//				tmp.NumTravelbugs = jCache.getInt("TrackableCount");
//				// weitere Infos in diesem Json record
//				// CacheName (getString)
//				// CacheType (getDouble / getLong ?)
//				// Premium   (getBoolean)
//				break;
//			    }
//			} while (iterator.hasNext());
//
//		    }
//
//		    return 0;
//		} else {
//		    result = "StatusCode = " + status.getInt("StatusCode") + "\n";
//		    result += status.getString("StatusMessage") + "\n";
//		    result += status.getString("ExceptionDetails");
//		    LastAPIError = result;
//		    return (-1);
//		}
//
//	    } catch (JSONException e) {
//		e.printStackTrace();
//	    }
//
//	} catch (ConnectTimeoutException e) {
//	    logger.error("GetGeocacheStatus ConnectTimeoutException", e);
//	    return CONNECTION_TIMEOUT;
//	} catch (UnsupportedEncodingException e) {
//	    logger.error("GetGeocacheStatus UnsupportedEncodingException", e);
//	    return ERROR;
//	} catch (ClientProtocolException e) {
//	    logger.error("GetGeocacheStatus ClientProtocolException", e);
//	    return ERROR;
//	} catch (IOException e) {
//	    logger.error("GetGeocacheStatus IOException", e);
//	    return ERROR;
//	}
//
//	return (-1);
//    }
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
//		requestString += "&AccessToken=" + GetAccessToken();
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
//				logger.error("SearchForGeocaches_ParseLogDate", exc);
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
//		logger.error("GetGeocacheLogsByCache ConnectTimeoutException", e);
//		return CONNECTION_TIMEOUT;
//	    } catch (UnsupportedEncodingException e) {
//		logger.error("GetGeocacheLogsByCache UnsupportedEncodingException", e);
//		return ERROR;
//	    } catch (ClientProtocolException e) {
//		logger.error("GetGeocacheLogsByCache ClientProtocolException", e);
//		return ERROR;
//	    } catch (IOException e) {
//		logger.error("GetGeocacheLogsByCache IOException", e);
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
//		request.put("AccessToken", GetAccessToken());
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
//	    logger.error("GetGeocacheStatus ConnectTimeoutException", e);
//	    return CONNECTION_TIMEOUT;
//	} catch (UnsupportedEncodingException e) {
//	    logger.error("GetGeocacheStatus UnsupportedEncodingException", e);
//	    return ERROR;
//	} catch (ClientProtocolException e) {
//	    logger.error("GetGeocacheStatus ClientProtocolException", e);
//	    return ERROR;
//	} catch (IOException e) {
//	    logger.error("GetGeocacheStatus IOException", e);
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
//    static int getCacheSize(int containerTypeId) {
//	switch (containerTypeId) {
//	case 1:
//	    return 0; // Unknown
//	case 2:
//	    return 1; // Micro
//	case 3:
//	    return 3; // Regular
//	case 4:
//	    return 4; // Large
//	case 5:
//	    return 5; // Virtual
//	case 6:
//	    return 0; // Other
//	case 8:
//	    return 2;
//	default:
//	    return 0;
//
//	}
//    }
//
//    static CacheTypes getCacheType(int apiTyp) {
//	switch (apiTyp) {
//	case 2:
//	    return CacheTypes.Traditional;
//	case 3:
//	    return CacheTypes.Multi;
//	case 4:
//	    return CacheTypes.Virtual;
//	case 5:
//	    return CacheTypes.Letterbox;
//	case 6:
//	    return CacheTypes.Event;
//	case 8:
//	    return CacheTypes.Mystery;
//	case 9:
//	    return CacheTypes.Cache; // Project APE Cache???
//	case 11:
//	    return CacheTypes.Camera;
//	case 12:
//	    return CacheTypes.Cache; // Locationless (Reverse) Cache
//	case 13:
//	    return CacheTypes.CITO; // Cache In Trash Out Event
//	case 137:
//	    return CacheTypes.Earth;
//	case 453:
//	    return CacheTypes.MegaEvent;
//	case 452:
//	    return CacheTypes.ReferencePoint;
//	case 1304:
//	    return CacheTypes.Cache; // GPS Adventures Exhibit
//	case 1858:
//	    return CacheTypes.Wherigo;
//
//	case 217:
//	    return CacheTypes.ParkingArea;
//	case 220:
//	    return CacheTypes.Final;
//	case 219:
//	    return CacheTypes.MultiStage;
//	case 221:
//	    return CacheTypes.Trailhead;
//	case 218:
//	    return CacheTypes.MultiQuestion;
//	case 7005:
//	    return CacheTypes.Giga;
//
//	default:
//	    return CacheTypes.Undefined;
//
//	}
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
//		request.put("AccessToken", GetAccessToken());
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
//	    logger.error("GetGeocacheStatus ConnectTimeoutException", e);
//	    return CONNECTION_TIMEOUT;
//	} catch (UnsupportedEncodingException e) {
//	    logger.error("GetGeocacheStatus UnsupportedEncodingException", e);
//	    return ERROR;
//	} catch (ClientProtocolException e) {
//	    logger.error("GetGeocacheStatus ClientProtocolException", e);
//	    return ERROR;
//	} catch (IOException e) {
//	    logger.error("GetGeocacheStatus IOException", e);
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
//	    HttpGet httppost = new HttpGet(URL + "GetTrackablesByTrackingNumber?AccessToken=" + GetAccessToken(true) + "&trackingNumber=" + TrackingCode + "&format=json");
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
//	    logger.error("getTBbyTreckNumber ConnectTimeoutException", e);
//	    TB = null;
//	    return CONNECTION_TIMEOUT;
//	} catch (UnsupportedEncodingException e) {
//	    logger.error("getTBbyTreckNumber UnsupportedEncodingException", e);
//	    TB = null;
//	    return ERROR;
//	} catch (ClientProtocolException e) {
//	    logger.error("getTBbyTreckNumber ClientProtocolException", e);
//	    TB = null;
//	    return ERROR;
//	} catch (IOException e) {
//	    logger.error("getTBbyTreckNumber IOException", e);
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
//	    HttpGet httppost = new HttpGet(URL + "GetTrackablesByTBCode?AccessToken=" + GetAccessToken(true) + "&tbCode=" + TrackingNumber + "&format=json");
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
//	    logger.error("getTBbyTbCode ConnectTimeoutException", e);
//	    TB = null;
//	    return CONNECTION_TIMEOUT;
//	} catch (UnsupportedEncodingException e) {
//	    logger.error("getTBbyTbCode UnsupportedEncodingException", e);
//	    TB = null;
//	    return ERROR;
//	} catch (ClientProtocolException e) {
//	    logger.error("getTBbyTbCode ClientProtocolException", e);
//	    TB = null;
//	    return ERROR;
//	} catch (IOException e) {
//	    logger.error("getTBbyTbCode IOException", e);
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
//	    HttpGet httppost = new HttpGet(URL + "GetImagesForGeocache?AccessToken=" + GetAccessToken() + "&CacheCode=" + cacheCode + "&format=json");
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
//	    logger.error("getImagesForGeocache ConnectTimeoutException", e);
//	    return CONNECTION_TIMEOUT;
//	} catch (UnsupportedEncodingException e) {
//	    logger.error("getImagesForGeocache UnsupportedEncodingException", e);
//	    return ERROR;
//	} catch (ClientProtocolException e) {
//	    logger.error("getImagesForGeocache ClientProtocolException", e);
//	    return ERROR;
//	} catch (IOException e) {
//	    logger.error("getImagesForGeocache IOException", e);
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
//    public static int GetAllImageLinks(String cacheCode, HashMap<String, URI> list, ICancel icancel) {
//	int chk = chkMembership(false);
//	if (chk < 0)
//	    return chk;
//
//	String URL = Config.StagingAPI.getValue() ? STAGING_GS_LIVE_URL : GS_LIVE_URL;
//	if (list == null)
//	    list = new HashMap<String, URI>();
//	try {
//	    HttpGet httppost = new HttpGet(URL + "GetImagesForGeocache?AccessToken=" + GetAccessToken(true) + "&CacheCode=" + cacheCode + "&format=json");
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
//			String name = jImage.getString("Name");
//			String uri = jImage.getString("Url");
//			// ignore log images
//			if (uri.contains("/cache/log"))
//			    continue; // LOG-Image
//			// Check for duplicate name
//			if (list.containsKey(name)) {
//			    for (int nr = 1; nr < 10; nr++) {
//				if (list.containsKey(name + "_" + nr)) {
//				    continue; // Name already exists
//				}
//				name += "_" + nr;
//				break;
//			    }
//			}
//			list.put(name, new URI(uri));
//		    }
//		    return IO;
//		} else if (status.getInt("StatusCode") == 140) {
//		    return 140; // API-Limit überschritten -> nach etwas Verzögerung wiederholen!
//		} else {
//		    LastAPIError = "";
//		    LastAPIError = "StatusCode = " + status.getInt("StatusCode") + "\n";
//		    LastAPIError += status.getString("StatusMessage") + "\n";
//		    LastAPIError += status.getString("ExceptionDetails");
//
//		    list = null;
//		    return ERROR;
//		}
//
//	    } catch (JSONException e) {
//		logger.error("getTBbyTbCode JSONException", e);
//	    } catch (URISyntaxException e) {
//		logger.error("getTBbyTbCode URISyntaxException", e);
//	    } catch (ClassCastException e) {
//		logger.error("getTBbyTbCode URISyntaxException", e);
//	    }
//
//	} catch (ConnectTimeoutException e) {
//	    logger.error("getTBbyTbCode ConnectTimeoutException", e);
//	    list = null;
//	    return CONNECTION_TIMEOUT;
//	} catch (UnsupportedEncodingException e) {
//	    logger.error("getTBbyTbCode UnsupportedEncodingException", e);
//	    list = null;
//	    return ERROR;
//	} catch (ClientProtocolException e) {
//	    logger.error("getTBbyTbCode ClientProtocolException", e);
//	    list = null;
//	    return ERROR;
//	} catch (IOException e) {
//	    logger.error("getTBbyTbCode IOException", e);
//	    list = null;
//	    return ERROR;
//	}
//
//	list = null;
//	return ERROR;
//    }
//
//    public static void WriteCachesLogsImages_toDB(CB_List<Cache> apiCaches, ArrayList<LogEntry> apiLogs, ArrayList<ImageEntry> apiImages) throws InterruptedException {
//	// Auf eventuellen Thread Abbruch reagieren
//	Thread.sleep(2);
//
//	Database.Data.beginTransaction();
//
//	CacheDAO cacheDAO = new CacheDAO();
//	LogDAO logDAO = new LogDAO();
//	ImageDAO imageDAO = new ImageDAO();
//	WaypointDAO waypointDAO = new WaypointDAO();
//
//	for (int c = 0; c < apiCaches.size(); c++) {
//	    Cache cache = apiCaches.get(c);
//	    Cache aktCache = Database.Data.Query.GetCacheById(cache.Id);
//
//	    if (aktCache != null && aktCache.isLive())
//		aktCache = null;
//
//	    if (aktCache == null) {
//		aktCache = cacheDAO.getFromDbByCacheId(cache.Id);
//	    }
//	    // Read Detail Info of Cache if not available
//	    if ((aktCache != null) && (aktCache.detail == null)) {
//		aktCache.loadDetail();
//	    }
//	    // If Cache into DB, extract saved rating
//	    if (aktCache != null) {
//		cache.Rating = aktCache.Rating;
//	    }
//
//	    // Falls das Update nicht klappt (Cache noch nicht in der DB) Insert machen
//	    if (!cacheDAO.UpdateDatabase(cache)) {
//		cacheDAO.WriteToDatabase(cache);
//	    }
//
//	    // Notes von Groundspeak überprüfen und evtl. in die DB an die vorhandenen Notes anhängen
//	    if (cache.getTmpNote() != null) {
//		String oldNote = Database.GetNote(cache);
//		String newNote = "";
//		if (oldNote == null) {
//		    oldNote = "";
//		}
//		String begin = "<Import from Geocaching.com>";
//		String end = "</Import from Geocaching.com>";
//		int iBegin = oldNote.indexOf(begin);
//		int iEnd = oldNote.indexOf(end);
//		if ((iBegin >= 0) && (iEnd > iBegin)) {
//		    // Note from Groundspeak already in Database
//		    // -> Replace only this part in whole Note
//		    newNote = oldNote.substring(0, iBegin - 1) + System.getProperty("line.separator"); // Copy the old part of Note before
//		    // the beginning of the groundspeak
//		    // block
//		    newNote += begin + System.getProperty("line.separator");
//		    newNote += cache.getTmpNote();
//		    newNote += System.getProperty("line.separator") + end;
//		    newNote += oldNote.substring(iEnd + end.length(), oldNote.length());
//		} else {
//		    newNote = oldNote + System.getProperty("line.separator");
//		    newNote += begin + System.getProperty("line.separator");
//		    newNote += cache.getTmpNote();
//		    newNote += System.getProperty("line.separator") + end;
//		}
//		cache.setTmpNote(newNote);
//		Database.SetNote(cache, cache.getTmpNote());
//	    }
//
//	    // Delete LongDescription from this Cache! LongDescription is Loading by showing DescriptionView direct from DB
//	    cache.setLongDescription("");
//
//	    for (LogEntry log : apiLogs) {
//		if (log.CacheId != cache.Id)
//		    continue;
//		// Write Log to database
//
//		logDAO.WriteToDatabase(log);
//	    }
//
//	    for (ImageEntry image : apiImages) {
//		if (image.CacheId != cache.Id)
//		    continue;
//		// Write Image to database
//
//		imageDAO.WriteToDatabase(image, false);
//	    }
//
//	    for (int i = 0, n = cache.waypoints.size(); i < n; i++) {
//		// must Cast to Full Waypoint. If Waypoint, is wrong createt!
//		Waypoint waypoint = cache.waypoints.get(i);
//		boolean update = true;
//
//		// dont refresh wp if aktCache.wp is user changed
//		if (aktCache != null) {
//		    if (aktCache.waypoints != null) {
//			for (int j = 0, m = aktCache.waypoints.size(); j < m; j++) {
//			    Waypoint wp = aktCache.waypoints.get(j);
//			    if (wp.getGcCode().equalsIgnoreCase(waypoint.getGcCode())) {
//				if (wp.IsUserWaypoint)
//				    update = false;
//				break;
//			    }
//			}
//		    }
//		}
//
//		if (update) {
//		    // do not store replication information when importing caches with GC api
//		    if (!waypointDAO.UpdateDatabase(waypoint, false)) {
//			waypointDAO.WriteToDatabase(waypoint, false); // do not store replication information here
//		    }
//		}
//
//	    }
//
//	    if (aktCache == null) {
//		Database.Data.Query.add(cache);
//		// cacheDAO.WriteToDatabase(cache);
//	    } else {
//		// 2012-11-17: do not remove old instance from Query because of problems with cacheList and MapView
//		// Database.Data.Query.remove(Database.Data.Query.GetCacheById(cache.Id));
//		// Database.Data.Query.add(cache);
//		aktCache.copyFrom(cache);
//		// cacheDAO.UpdateDatabase(cache);
//	    }
//
//	}
//	Database.Data.setTransactionSuccessful();
//	Database.Data.endTransaction();
//
//	Database.Data.GPXFilenameUpdateCacheCount();
//
//    }
//
    private static String getDeviceInfoRequestString() {
        String string = "\"DeviceInfo\":{";

        string += "\"ApplicationCurrentMemoryUsage\":\"" + String.valueOf(2147483647) + "\",";
        string += "\"ApplicationPeakMemoryUsage\":\"" + String.valueOf(2147483647) + "\",";
        string += "\"ApplicationSoftwareVersion\":\"" + BuildInfo.getRevison() + "\",";
        string += "\"DeviceManufacturer\":\"" + "?\"" + ",";
        string += "\"DeviceName\":\"" + "?\"" + ",";
        string += "\"DeviceOperatingSystem\":\"ANDROID\"" + ",";
        string += "\"DeviceTotalMemoryInMB\":\"" + String.valueOf(1.26743233E+15) + "\",";
        string += "\"DeviceUniqueId\":\"" + "?\"" + ",";
        string += "\"MobileHardwareVersion\":\"" + "?\"" + ",";
        string += "\"WebBrowserVersion\":\"" + "?\"";

        string += "}";

        return string;
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
//    public static int chkMembership(boolean withoutMsg) {
//	boolean isValid = false;
//	if (API_isCheked) {
//	    isValid = membershipType > 0;
//	    return isValid ? 0 : 1;
//	}
//	int ret = 0;
//	if (GetAccessToken().length() > 0) {
//
//	    if (!isValid) {
//		ret = GetMembershipType(null);
//		isValid = membershipType > 0;
//		if (ret < 0)
//		    return ret;
//	    }
//	    isValid = membershipType > 0;
//	}
//
//	if (!isValid && ret != CONNECTION_TIMEOUT) {
//	    if (!withoutMsg)
//		API_ErrorEventHandlerList.callInvalidApiKey(API_ErrorEventHandlerList.API_ERROR.INVALID);
//	}
//
//	if (ret != CONNECTION_TIMEOUT)
//	    API_isCheked = true;
//	else
//	    return CONNECTION_TIMEOUT;
//
//	return ret;
//    }
//
//    public static int isValidAPI_Key(boolean withoutMsg) {
//	if (API_isCheked)
//	    return membershipType;
//
//	return chkMembership(withoutMsg);
//    }
//
//    /**
//     * @param Staging
//     *            Config.settings.StagingAPI.getValue()
//     * @param accessToken
//     * @param TB
//     * @param cacheCode
//     * @param LogTypeId
//     * @param dateLogged
//     * @param note
//     * @param conectionTimeout
//     *            Config.settings.conection_timeout.getValue()
//     * @param socketTimeout
//     *            Config.settings.socket_timeout.getValue()
//     * @return
//     */
//    public static int createTrackableLog(Trackable TB, String cacheCode, int LogTypeId, Date dateLogged, String note, ICancel icancel) {
//	return createTrackableLog(TB.getGcCode(), TB.getTrackingNumber(), cacheCode, LogTypeId, dateLogged, note, icancel);
//    }
//
//    /**
//     * @param Staging
//     *            Config.settings.StagingAPI.getValue()
//     * @param accessToken
//     * @param TbCode
//     * @param TrackingNummer
//     * @param cacheCode
//     * @param LogTypeId
//     * @param dateLogged
//     * @param note
//     * @param conectionTimeout
//     *            Config.settings.conection_timeout.getValue()
//     * @param socketTimeout
//     *            Config.settings.socket_timeout.getValue()
//     * @return
//     */
//    public static int createTrackableLog(String TbCode, String TrackingNummer, String cacheCode, int LogTypeId, Date dateLogged, String note, ICancel icancel) {
//	int chk = chkMembership(false);
//	if (chk < 0)
//	    return chk;
//	String URL = Config.StagingAPI.getValue() ? STAGING_GS_LIVE_URL : GS_LIVE_URL;
//	if (cacheCode == null)
//	    cacheCode = "";
//
//	try {
//	    HttpPost httppost = new HttpPost(URL + "CreateTrackableLog?format=json");
//	    String requestString = "";
//	    requestString = "{";
//	    requestString += "\"AccessToken\":\"" + GetAccessToken() + "\",";
//	    requestString += "\"CacheCode\":\"" + cacheCode + "\",";
//	    requestString += "\"LogType\":" + String.valueOf(LogTypeId) + ",";
//	    requestString += "\"UTCDateLogged\":\"" + GetUTCDate(dateLogged) + "\",";
//	    requestString += "\"Note\":\"" + ConvertNotes(note) + "\",";
//	    requestString += "\"TravelBugCode\":\"" + String.valueOf(TbCode) + "\",";
//	    requestString += "\"TrackingNumber\":\"" + String.valueOf(TrackingNummer) + "\"";
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
//	    // Parse JSON Result
//	    try {
//		JSONTokener tokener = new JSONTokener(result);
//		JSONObject json = (JSONObject) tokener.nextValue();
//		JSONObject status = json.getJSONObject("Status");
//		if (status.getInt("StatusCode") == 0) {
//		    result = "";
//		    LastAPIError = "";
//		} else {
//		    result = "StatusCode = " + status.getInt("StatusCode") + "\n";
//		    result += status.getString("StatusMessage") + "\n";
//		    result += status.getString("ExceptionDetails");
//		    LastAPIError = result;
//		    return -1;
//		}
//
//	    } catch (JSONException e) {
//		e.printStackTrace();
//		logger.error("UploadFieldNotesAPI JSON-Error", e);
//		LastAPIError = e.getMessage();
//		return -1;
//	    }
//
//	} catch (ConnectTimeoutException e) {
//	    logger.error("createTrackableLog ConnectTimeoutException", e);
//	    return CONNECTION_TIMEOUT;
//	} catch (UnsupportedEncodingException e) {
//	    logger.error("createTrackableLog UnsupportedEncodingException", e);
//	    return ERROR;
//	} catch (ClientProtocolException e) {
//	    logger.error("createTrackableLog ClientProtocolException", e);
//	    return ERROR;
//	} catch (IOException e) {
//	    logger.error("createTrackableLog IOException", e);
//	    return ERROR;
//	}
//
//	LastAPIError = "";
//	return 0;
//    }

    public static boolean mAPI_isChecked() {
        return API_isCheked;
    }

    public static boolean ApiLimit() {
        return DownloadLimit;
    }

    public static void setDownloadLimit() {
        DownloadLimit = true;
    }

}
