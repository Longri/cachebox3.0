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
package de.longri.cachebox3.gpx;

import de.longri.cachebox3.sqlite.Database;
import de.longri.cachebox3.types.CacheTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.longri.cachebox3.utils.ActiveQName;

import javax.xml.stream.events.Characters;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;

/**
 * Created by Longri on 29.03.2018.
 */
public class GroundspeakGpxFileImporter extends AbstarctGpxFileImporter {
    private final static Logger log = LoggerFactory.getLogger(GroundspeakGpxFileImporter.class);

    // ActiveQNames
    private final ActiveQName WPT = this.registerName("wpt");
    private final ActiveQName LAT = this.registerName("lat");
    private final ActiveQName LON = this.registerName("lon");
    private final ActiveQName TYPE = this.registerName("type");
    private final ActiveQName NAME = this.registerName("name");
    private final ActiveQName GS_CACHE = this.registerName("groundspeak:cache");
    private final ActiveQName TITLE = this.registerName("groundspeak:name");
    private final ActiveQName ID = this.registerName("id");
    private final ActiveQName AVAIABLE = this.registerName("available");
    private final ActiveQName ARCHIVED = this.registerName("archived");
    private final ActiveQName GS_TRAVELBUGS = this.registerName("groundspeak:travelbugs");
    private final ActiveQName GS_PLACED_BY = this.registerName("groundspeak:placed_by");
    private final ActiveQName GS_OWNER = this.registerName("groundspeak:owner");


    public GroundspeakGpxFileImporter(Database database, ImportHandler importHandler) {
        super(database, importHandler);
    }


    @Override
    protected void startElement(ActiveQName name, StartElement element) {
        if (name.equals(WPT)) {
            // get latitude and longitude
            latitude = parseDouble(element.getAttributeByName(LAT));
            longitude = parseDouble(element.getAttributeByName(LON));
        } else if (WPT.isActive() && element.getName().equals(GS_CACHE)) {
            // get id and available flags
            id = parseLong(element.getAttributeByName(ID));
            archived = parseBool(element.getAttributeByName(ARCHIVED));
            available = parseBool(element.getAttributeByName(AVAIABLE));
        }
    }


    @Override
    protected void endElement(ActiveQName name, EndElement element) {
        if (WPT.isActive() && name.equals(WPT)) {
            // end of Cache or Waypoint, create new Cache/Waypoint from Values
            createNewWPT();

            // clear values for next entry
            resetValues();

        }
    }

    @Override
    protected void data(ActiveQName name, Characters element) {
        if (WPT.isActive()) {
            if (TYPE.isActive()) {
                type = CacheTypes.parseString(element.getData());
            } else if (NAME.isActive()) {
                gcCode = element.getData();
            } else if (GS_CACHE.isActive()) {
                if (TITLE.isActive() && !GS_TRAVELBUGS.isActive()) {
                    title = element.getData();
                } else if (GS_PLACED_BY.isActive()) {
                    placed_by = element.getData();
                }else if (GS_OWNER.isActive()) {
                    owner = element.getData();
                }
            }
        }

    }


}
