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

import de.longri.cachebox3.types.Attributes;
import de.longri.cachebox3.types.CacheSizes;
import de.longri.cachebox3.types.CacheTypes;

/**
 * Created by Longri on 31.03.18.
 */
public class TestCache_GC2T9RW extends AbstractTestCache {
    public TestCache_GC2T9RW() {
        this.latitude = 49.349817;
        this.longitude = 8.62925;
        this.cacheType = CacheTypes.Traditional;
        this.gcCode = "GC2T9RW";
        this.name = "der Hampir - T5 -";
        this.id = 2190117L;
        this.available = true;
        this.archived = false;
        this.placed_by = "Team Rabbits";
        this.owner = "Team Rabbits";
        this.container = CacheSizes.small;

        this.positiveList.add(Attributes.Bicycles);
        this.positiveList.add(Attributes.Available_at_all_times);
        this.positiveList.add(Attributes.Public_restrooms_nearby);
        this.positiveList.add(Attributes.Parking_available);
        this.positiveList.add(Attributes.Fuel_Nearby);
        this.positiveList.add(Attributes.Hunting);
        this.positiveList.add(Attributes.Short_hike);
        this.positiveList.add(Attributes.Climbing_gear);
        this.positiveList.add(Attributes.Ticks);
        this.positiveList.add(Attributes.Dogs);

    }

}
