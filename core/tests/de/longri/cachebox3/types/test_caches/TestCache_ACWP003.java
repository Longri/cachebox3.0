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

import de.longri.cachebox3.types.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Created by Longri on 11.04.2018.
 */
public class TestCache_ACWP003 extends AbstractTestCache {


    @Override
    protected void setValues() {
        this.latitude = 50.891667;
        this.longitude = 9.891667;
        this.cacheType = CacheTypes.Multi;
        this.gcCode = "ACWP003";
        this.name = "Test-Cache 3 für Wegpunkte";
        this.available = true;
        this.archived = false;
        this.placed_by = "Test Owner";
        this.owner = "Test Owner";
        this.container = CacheSizes.regular;
        this.url = "http://team-cachebox.de/cache.php?id=ACWP003";
        this.difficulty = 1.5f;
        this.terrain = 2.5f;
        this.country = "Country";
        this.state = "State";
        this.found = false;
        this.tbCount = 0;
        this.hint = "Final: Im Loch";
        this.favoritePoints = 0;
        this.note = "";
        this.solver = "";
        try {
            this.dateHidden = DATE_PATTERN.parse("2014-06-24T08:00:00Z");
        } catch (ParseException e) {
            e.printStackTrace();
        }

        this.shortDescription = "Kurzbeschreibung";
        this.longDescription = "Langbeschreibung";
    }

    @Override
    protected boolean addWaypoints() {
        AbstractWaypoint wp1 = new MutableWaypoint(50.895, 9.895, this.id);
        wp1.setGcCode("S1WP003");
        wp1.setType(CacheTypes.MultiStage);
        wp1.setTitle("WP 1 von ACWP003");
        wp1.setDescription("Dieser Wegpunkt muss dem Cache ACWP003 zugeordnet werden");
        wp1.setClue("");
        wp1.setUserWaypoint(false);
        wp1.setStart(false);
        this.waypoints.add(wp1);
        return true;
    }


    @Override
    protected boolean addLogs() throws ParseException {

        SimpleDateFormat DATE_PATTERN2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());

        LogEntry logEntry1 = new LogEntry();
        logEntry1.CacheId = this.id;
        logEntry1.Finder = "GSAK";
        logEntry1.Type = LogTypes.note;
        logEntry1.Comment = "User Note\n    ";
        logEntry1.Timestamp = DATE_PATTERN2.parse("2014-07-19T08:00:00");
        logEntry1.Id = -3L;
        this.logEntries.add(logEntry1);

        LogEntry logEntry2 = new LogEntry();
        logEntry2.CacheId = this.id;
        logEntry2.Finder = "GSAK";
        logEntry2.Type = LogTypes.note;
        logEntry2.Comment = "User Note$~Log Section\n    ";
        logEntry2.Timestamp = DATE_PATTERN2.parse("2014-07-19T08:00:00");
        logEntry2.Id = -2L;
        this.logEntries.add(logEntry2);
        return true;
    }
}
