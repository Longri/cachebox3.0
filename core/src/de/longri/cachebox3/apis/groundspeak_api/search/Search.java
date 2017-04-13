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

import com.badlogic.gdx.utils.JsonWriter;

/**
 * Search Definitions
 *
 * @author Hubert
 * @author Longri
 */
public class Search {
    public int number;
    public boolean excludeHides = false;
    public boolean excludeFounds = false;
    public boolean available = true;
    int geocacheLogCount = 10;
    int trackableLogCount = 10;
    private boolean isLite;

    Search(int number) {
        this.number = number;
    }

    protected void getRequest(JsonWriter writer) {

        //TODO change to JsonWriter

//		writer.json("IsLite", this.isLite);
//		request.put("StartIndex", 0);
//		request.put("MaxPerPage", this.number);
//		request.put("GeocacheLogCount", this.geocacheLogCount);
//		request.put("TrackableLogCount", this.trackableLogCount);
//		if (this.available) {
//			JSONObject excl = new JSONObject();
//			excl.put("Archived", false);
//			excl.put("Available", true);
//			request.put("GeocacheExclusions", excl);
//
//		}
//		if (this.excludeHides) {
//			JSONObject excl = new JSONObject();
//			JSONArray jarr = new JSONArray();
//			jarr.put(CB_Core_Settings.GcLogin.getValue());
//			excl.put("UserNames", jarr);
//			request.put("NotHiddenByUsers", excl);
//		}
//
//		if (this.excludeFounds) {
//			JSONObject excl = new JSONObject();
//			JSONArray jarr = new JSONArray();
//			jarr.put(CB_Core_Settings.GcLogin.getValue());
//			excl.put("UserNames", jarr);
//			request.put("NotFoundByUsers", excl);
//		}
    }

    public void setIsLite(boolean isLite) {
        this.isLite = isLite;
    }
}