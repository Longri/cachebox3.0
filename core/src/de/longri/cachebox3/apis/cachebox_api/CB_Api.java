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

package de.longri.cachebox3.apis.cachebox_api;

import de.longri.cachebox3.settings.Config;
import de.longri.cachebox3.utils.http.Webb;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Diese Klasse stellt eine verbindung zu Team-Cachebox.de her und gibt dort hinterlegte Informationen zurück. (GCAuth url ; Versionsnummer)
 *
 * @author Longri
 */
public class CB_Api {
    private static final Logger log = LoggerFactory.getLogger(CB_Api.class);

    public static String getGcAuthUrl() {
        try {
            String url, resultKey;
            if (Config.UseTestUrl.getValue()) {
                url = "https://longri.de/CB_API/index.php?get=url_ACB_Staging";
                resultKey = "GcAuth_ACB_Staging";
                // {"GcAuth_ACB_Staging":"http:\/\/staging.oauth.Team-Cachebox.de\/index.php?Version=ACB "}
            } else {
                url = "https://longri.de/CB_API/index.php?get=url_ACB";
                resultKey = "GcAuth_ACB";
                // {"GcAuth_ACB":"http:\/\/oauth.Team-Cachebox.de\/index.php?Version=ACB "}
            }
            Webb httpClient = Webb.create();
            JSONObject response = httpClient
                    .get(url)
                    .ensureSuccess()
                    .asJsonObject()
                    .getBody();
            return response.getString(resultKey).trim();
        } catch (Exception ex) {
            log.error("getGcAuthUrl", ex);
            return "";
        }
    }

}
