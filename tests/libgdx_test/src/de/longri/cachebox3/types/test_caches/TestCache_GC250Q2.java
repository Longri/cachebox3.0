/*
 * Copyright (C) 2018 team-cachebox.de
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
package de.longri.cachebox3.types.test_caches;

import de.longri.cachebox3.types.AbstractWaypoint;
import de.longri.cachebox3.types.CacheSizes;
import de.longri.cachebox3.types.CacheTypes;
import de.longri.cachebox3.types.MutableWaypoint;

import java.text.ParseException;

/**
 * Created by Longri on 11.04.2018.
 */
public class TestCache_GC250Q2 extends AbstractTestCache {
    @Override
    protected void setValues() {
        this.latitude = 48.766867;
        this.longitude = 9.183033;
        this.cacheType = CacheTypes.Mystery;
        this.gcCode = "GC250Q2";
        this.name = "Bopserbrünnele - Phoon";
        this.available = true;
        this.archived = false;
        this.placed_by = "Wanderprofi";
        this.owner = "Wanderprofi";
        this.container = CacheSizes.micro;
        this.url = "http://www.geocaching.com/seek/cache_details.aspx?guid=01776374-a3a3-4a7a-a4e8-634bfa6b0898";
        this.difficulty = 2.5f;
        this.terrain = 1.5f;
        this.country = "Germany";
        this.state = "";
        this.found = false;
        this.tbCount = 0;
        this.hint = "";
        this.favoritePoints = 31;
        this.note = "Gc Note";
        this.solver = "";
        try {
            this.dateHidden = DATE_PATTERN.parse("2010-03-14T08:00:00Z");
        } catch (ParseException e) {
            e.printStackTrace();
        }

        this.shortDescription = " ";
        this.longDescription = " ";
    }

    @Override
    protected boolean addWaypoints() {
        AbstractWaypoint wp1 = new MutableWaypoint(48.76687, 9.18288, this.id);
        wp1.setGcCode("C0250Q2");
        wp1.setType(CacheTypes.Final);
        wp1.setTitle("Final GSAK Corrected");
        wp1.setClue("");
        wp1.setUserWaypoint(false);
        wp1.setStart(false);
        this.waypoints.add(wp1);
        return true;
    }

    @Override
    protected boolean addLogs() throws ParseException {
        return true;
    }
}
