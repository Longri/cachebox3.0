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
import com.badlogic.gdx.utils.Timer;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.callbacks.GenericCallBack;
import de.longri.cachebox3.settings.Config;
import de.longri.cachebox3.utils.ICancel;
import de.longri.cachebox3.utils.NamedRunnable;
import de.longri.cachebox3.utils.NetUtils;
import de.longri.cachebox3.utils.json_parser.DraftUploadResultParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicBoolean;

import static de.longri.cachebox3.apis.groundspeak_api.PostRequest.GS_LIVE_URL;
import static de.longri.cachebox3.apis.groundspeak_api.PostRequest.STAGING_GS_LIVE_URL;

public class GroundspeakLiveAPI {
    private static final Logger log = LoggerFactory.getLogger(GroundspeakLiveAPI.class);
    public static String LastAPIError = "";
    public static boolean CacheStatusValid = false;
    public static boolean CacheStatusLiteValid = false;
    public static String memberName = ""; // this will be filled by
    private static boolean API_isChecked = false;
    private static Limit apiCallLimit;
    private static ApiResultState membershipType = ApiResultState.UNKNOWN;

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


        waitApiCallLimit(null);


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

    public static void resetApiIsChecked() {
        API_isChecked = false;
    }
}
