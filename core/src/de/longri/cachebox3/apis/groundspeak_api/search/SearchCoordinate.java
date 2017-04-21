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
import de.longri.cachebox3.locator.Coordinate;

/**
 * @author Hubert
 * @author Longri
 */
public class SearchCoordinate extends Search {
    public Coordinate pos;
    public float distanceInMeters;

    /**
     * @param gcApiKey valid encrypted Api-Key
     * @param number   MaxPerPage size for this request
     * @param apiState 0 = unknown, 1 = Basic Member, 2 = Premium Member
     */
    public SearchCoordinate(String gcApiKey, int number, Coordinate pos, float distanceInMeters, byte apiState) {
        super(gcApiKey, number, apiState);
        this.pos = pos;
        this.distanceInMeters = distanceInMeters;
    }

    @Override
    protected void getRequest(Json json) {
        // add Coordinate info
        //        "PointRadius":{
        //            "DistanceInMeters":9223372036854775807,
        //                    "Point":{
        //                "Latitude":1.26743233E+15,
        //                        "Longitude":1.26743233E+15
        //            }
        //        },

        boolean hasStart = false;

        try {
            json.writeObjectStart();
        } catch (IllegalStateException e) {
            hasStart = true;
        }

        json.writeObjectStart("PointRadius");
        json.writeValue("DistanceInMeters",this.distanceInMeters);
        json.writeObjectStart("Point");
        json.writeValue("Latitude",pos.getLatitude());
        json.writeValue("Longitude",pos.getLongitude());
        json.writeObjectEnd();
        json.writeObjectEnd();

        super.getRequest(json);
        if (!hasStart) json.writeObjectEnd();

//		JSONObject jpr = new JSONObject();
//		jpr.put("DistanceInMeters", String.valueOf((int) distanceInMeters));
//		JSONObject jpt = new JSONObject();
//		jpt.put("Latitude", String.valueOf(pos.getLatitude()));
//		jpt.put("Longitude", String.valueOf(pos.getLongitude()));
//		jpr.put("Point", jpt);
//		request.put("PointRadius", jpr);
    }
}