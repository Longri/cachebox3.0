/* 
 * Copyright (C) 2011 - 2017 team-cachebox.de
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

package de.longri.cachebox3.api;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.net.HttpRequestBuilder;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import de.longri.cachebox3.callbacks.GenericCallBack;
import de.longri.cachebox3.settings.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Diese Klasse stellt eine verbindung zu Team-Cachebox.de her und gibt dort hinterlegte Informationen zurück. (GCAuth url ; Versionsnummer)
 *
 * @author Longri
 */
public class CB_Api {
    private static final Logger log = LoggerFactory.getLogger(CB_Api.class);

    private static final String CB_API_URL_GET_URLS = "http://team-cachebox.de/CB_API/index.php?get=url_ACB";
    private static final String CB_API_URL_GET_URLS_Staging = "http://team-cachebox.de/CB_API/index.php?get=url_ACB_Staging";

    /**
     * Gibt die bei Team-Cachebox.de hinterlegte GC Auth url zurück
     *
     * @return String
     */
    public static void getGcAuthUrl(final GenericCallBack<String> callBack) {

        // ues Gdx http request described: https://github.com/libgdx/libgdx/wiki/Networking

        HttpRequestBuilder requestBuilder = new HttpRequestBuilder();
        Net.HttpRequest httpRequest = requestBuilder.newRequest().method(Net.HttpMethods.GET)
                .url(Config.StagingAPI.getValue() ? CB_API_URL_GET_URLS_Staging : CB_API_URL_GET_URLS).build();
        Net.HttpResponseListener httpResponseListener = new Net.HttpResponseListener() {
            @Override
            public void handleHttpResponse(Net.HttpResponse httpResponse) {
                String jsonResult = httpResponse.getResultAsString();
                JsonValue json = new JsonReader().parse(jsonResult);

                String url;
                if (Config.StagingAPI.getValue())
                    url = json.getString("GcAuth_ACB_Staging");
                else
                    url = json.getString("GcAuth_ACB");

                url=url.replace("\\/","/").trim();

                callBack.callBack(url);
            }

            @Override
            public void failed(Throwable t) {
                log.error("CB_Api request failed", t);
                callBack.callBack("");
            }

            @Override
            public void cancelled() {
                log.debug("CB_Api request canceled");
                callBack.callBack("");
            }
        };
        Gdx.net.sendHttpRequest(httpRequest, httpResponseListener);
    }
}
