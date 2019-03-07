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

import java.text.ParseException;

/**
 * Created by Longri on 09.04.2018.
 */
public class TestCache_GC2TNPV extends AbstractTestCache {
    @Override
    protected void setValues() {
        this.latitude = 52.5896;
        this.longitude = 13.3998;
        this.cacheType = CacheTypes.Traditional;
        this.gcCode = "GC2TNPV";
        this.name = "Home Sweet Home (our first)";
        this.available = true;
        this.archived = false;
        this.placed_by = "ToniSoprano &amp; -KayT-";
        this.owner = "ToniSoprano";
        this.container = CacheSizes.micro;
        this.url = "http://www.geocaching.com/seek/cache_details.aspx?guid=882578dd-3390-4ade-815f-eee8eb110f70";
        this.difficulty = 1.5f;
        this.terrain = 2f;
        this.country = "Germany";
        this.state = "Berlin";
        this.found = false;
        this.tbCount = 0;
        this.hint = "Anziehend! ;-)";
        this.note = "";
        this.solver = "";
        try {
            this.dateHidden = DATE_PATTERN.parse("2011-04-22T07:00:00Z");
        } catch (ParseException e) {
            e.printStackTrace();
        }
        this.positiveList.add(Attributes.Motorcycles);
        this.positiveList.add(Attributes.Bicycles);
        this.positiveList.add(Attributes.Available_at_all_times);
        this.positiveList.add(Attributes.Stroller_accessible);
        this.positiveList.add(Attributes.Needs_maintenance);
        this.positiveList.add(Attributes.Wheelchair_accessible);
        this.positiveList.add(Attributes.Off_road_vehicles);
        this.positiveList.add(Attributes.Horses);
        this.positiveList.add(Attributes.Snowmobiles);
        this.positiveList.add(Attributes.Dogs);
        this.positiveList.add(Attributes.Quads);

        this.shortDescription = "Unser erster Cache, also erwartet nicht zu viel!\n" +
                "Er führt euch in den Weg, wo wir aufgewachsen sind. Vorsicht vor Schlaglöchern und vor Muggel. Wir haben sehr wachsame Nachbarn! ;-)\n" +
                "Wir wünschen euch viel Spaß!\n" +
                "\n" +
                " -KayT- und ToniSoprano";

        this.longDescription = "Es erwartet den Erstfinder eine kleine süße Überraschung! :-D";
    }

    @Override
    protected boolean addWaypoints() {
        return false;
    }

    @Override
    protected boolean addLogs() throws ParseException {
        return false;
    }
}
