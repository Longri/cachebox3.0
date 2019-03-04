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
public class TestCache_GC52BKF_without_logs extends AbstractTestCache {
    @Override
    protected void setValues() {
        this.latitude = 53.104683;
        this.longitude = 9.152833;
        this.cacheType = CacheTypes.Traditional;
        this.gcCode = "GC52BKF";
        this.name = "Kuh-Eule";
        this.available = true;
        this.archived = false;
        this.placed_by = "B.Eule";
        this.owner = "B.Eule";
        this.container = CacheSizes.small;
        this.url = "http://www.geocaching.com/seek/cache_details.aspx?guid=977edb69-bd72-403b-bc05-0dce3d1aafe5";
        this.difficulty = 1f;
        this.terrain = 1f;
        this.country = "Germany";
        this.state = "Niedersachsen";
        this.found = false;
        this.tbCount = 0;
        this.hint = "Nicht vom Bäcker";
        this.note = "";
        this.solver = "";
        try {
            this.dateHidden = DATE_PATTERN.parse("2014-04-12T07:00:00Z");
        } catch (ParseException e) {
            e.printStackTrace();
        }
        this.positiveList.add(Attributes.Wheelchair_accessible);

        this.shortDescription = "<p>Drive In. Eine nette Zusatzeule. Bewohner ist informiert. Dennoch oft  muggelig. Das Grundstück muss nicht betreten werden! <img alt=\"enlightened\" src=\"http://www.geocaching.com/static/js/CKEditor/4.1.2/plugins/smiley/images/lightbulb.gif\" title=\"enlightened\" style=\"height:20px;width:20px;\" /></p>\n" +
                "\n";

        this.longDescription = "\n";
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
