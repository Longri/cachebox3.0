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


import de.longri.cachebox3.locator.Coordinate;

/**
 * Search Caches around a coordinate with lite state for showing on LiveMap. <br>
 * Extends SearchCoordinate with state lite.<br>
 * Without Logs and Trackable-Logs <br>
 *
 * @author Longri
 */
public class SearchLiveMap extends SearchCoordinate {

    public SearchLiveMap(String gcApiKey, int number, Coordinate coordinate, float distanceInMeters) {
        super(gcApiKey, number, coordinate, distanceInMeters);
        geocacheLogCount = 0;
        trackableLogCount = 0;
        setIsLite(true);
    }

}
