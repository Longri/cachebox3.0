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
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonReader;
import de.longri.cachebox3.apis.groundspeak_api.json_parser.string_parser.ApiStatusResultParser;
import de.longri.cachebox3.callbacks.GenericCallBack;
import de.longri.cachebox3.utils.BuildInfo;
import org.oscim.backend.CanvasAdapter;

/**
 * Created by longri on 14.04.17.
 */
public class GetYourUserProfile extends PostRequest {

    private String memberName;
    private int membershipType;

    public GetYourUserProfile(String gcApiKey) {
        super(gcApiKey, null);
    }

    @Override
    protected void handleHttpResponse(Net.HttpResponse httpResponse, GenericCallBack<Integer> readyCallBack) {
        String result = httpResponse.getResultAsString();
        int status = getApiStatus(result);

        if (status == 0) {
            (new JsonReader() {
                public void number(String name, long value, String stringValue) {
                    super.number(name, value, stringValue);
                    if (name.equals("MemberTypeId")) {
                        membershipType = (int) value;
                    }
                }

                public void string(String name, String value) {
                    super.string(name, value);
                    if (name.equals("UserName")) {
                        memberName = value;
                    }
                }

            }).parse(result);
            log.debug("ready parse result Type:{} name:{}", membershipType, memberName);
            readyCallBack.callBack(NO_ERROR);
        } else if (status == 3) {
            // expired api key
            membershipType = -3;
            log.error("expired api key");
            readyCallBack.callBack(EXPIRED_API_KEY);
        } else {
            log.error("unknown result state");
            readyCallBack.callBack(ERROR);
        }
    }

    @Override
    protected String getCallUrl() {
        return "GetYourUserProfile?format=json";
    }

    /**
     * see: https://api.groundspeak.com/LiveV6/geocaching.svc/help/operations/GetYourUserProfile#request-json
     *
     * @param json
     */
    @Override
    protected void getRequest(Json json) {
        json.writeValue("AccessToken", this.gcApiKey);
        json.writeObjectStart("ProfileOptions");
        json.writeObjectEnd();
        json.writeObjectStart("DeviceInfo");
        json.writeValue("ApplicationSoftwareVersion", BuildInfo.getRevision());
        json.writeValue("DeviceOperatingSystem", getDeviceOperatingSystem());
        json.writeObjectEnd();
    }

    private String getDeviceOperatingSystem() {

        switch (CanvasAdapter.platform) {

            case ANDROID:
                return "Android";
            case IOS:
                return "iOS";
            case LINUX:
                return "Linux";
            case MACOS:
                return "MacOS";
            case UNKNOWN:
                return "UNKNOWN";
            case WEBGL:
                return "WebGl";
            case WINDOWS:
                return "Windows";
        }

        return "";
    }

    public static int getApiStatus(String result) {
        return new ApiStatusResultParser().get(result);
    }

    public int getMembershipType() {
        return membershipType;
    }

    public String getMemberName() {
        return memberName;
    }
}
