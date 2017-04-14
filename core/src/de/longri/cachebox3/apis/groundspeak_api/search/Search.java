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
package de.longri.cachebox3.apis.groundspeak_api.search;

import com.badlogic.gdx.utils.Json;
import de.longri.cachebox3.settings.Config;
import de.longri.cachebox3.types.Cache;
import de.longri.cachebox3.types.ImageEntry;
import de.longri.cachebox3.types.LogEntry;
import de.longri.cachebox3.utils.lists.CB_List;

/**
 * Search Definitions
 *
 * @author Hubert
 * @author Longri
 */
public class Search {
    public final String gcApiKey;
    public int number;
    public boolean excludeHides = false;
    public boolean excludeFounds = false;
    public boolean available = false;
    int geocacheLogCount = 10;
    int trackableLogCount = 10;
    private boolean isLite;

    Search(String gcApiKey, int number) {
        this.gcApiKey = gcApiKey;
        this.number = number;
    }

    /**
     * see https://api.groundspeak.com/LiveV6/geocaching.svc/help/operations/SearchForGeocaches
     *
     * @param json
     */
    protected void getRequest(Json json) {
        json.writeValue("AccessToken", this.gcApiKey);
        json.writeValue("MaxPerPage", this.number);
        json.writeValue("StartIndex", 0);
        json.writeValue("IsLite", this.isLite);
        json.writeValue("TrackableLogCount", this.trackableLogCount);
        json.writeValue("GeocacheLogCount", this.geocacheLogCount);

        if (this.available) {
            json.writeObjectStart("GeocacheExclusions");
            json.writeValue("Archived", false);
            json.writeValue("Available", true);
            json.writeObjectEnd();
        }
        if (this.excludeHides) {
            json.writeObjectStart("NotHiddenByUsers");
            json.writeArrayStart("UserNames");
            json.writeValue(Config.GcLogin.getValue());
            json.writeArrayEnd();
            json.writeObjectEnd();
        }

        if (this.excludeFounds) {
            json.writeObjectStart("NotFoundByUsers");
            json.writeArrayStart("UserNames");
            json.writeValue(Config.GcLogin.getValue());
            json.writeArrayEnd();
            json.writeObjectEnd();
        }
    }

    public void setIsLite(boolean isLite) {
        this.isLite = isLite;
    }


  public void postRequest(CB_List<Cache> cacheList, CB_List<LogEntry> logList,
                                     CB_List<ImageEntry> imageList, long gpxFilenameId) {

  }

}