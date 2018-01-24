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
import de.longri.cachebox3.sqlite.Database;

/**
 * @author Hubert
 * @author Longri
 */
public class SearchGCOwner extends SearchCoordinate {
    public String OwnerName;

    public SearchGCOwner(Database database, String gcApiKey, int number, Coordinate pos, float distanceInMeters, String ownerName, byte apiState) {
        super(database, gcApiKey, number, pos, distanceInMeters, apiState);
        this.OwnerName = ownerName;
    }

    @Override
    protected void getRequest(Json json) {
        boolean hasStart = false;

        try {
            json.writeObjectStart();
        } catch (IllegalStateException e) {
            hasStart = true;
        }

        json.writeObjectStart("HiddenByUsers");
        json.writeArrayStart("UserNames");
        json.writeValue(OwnerName);
        json.writeArrayEnd();
        json.writeObjectEnd();

        super.getRequest(json);
        if (!hasStart) json.writeObjectEnd();
    }
}