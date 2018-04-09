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
import de.longri.cachebox3.types.*;
import de.longri.cachebox3.utils.CharSequenceUtil;
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
    private final ActiveQName URL = this.registerName("url");
    private final ActiveQName GS_CACHE = this.registerName("groundspeak:cache");
    private final ActiveQName TITLE = this.registerName("groundspeak:name");
    private final ActiveQName ID = this.registerName("id");
    private final ActiveQName AVAIABLE = this.registerName("available");
    private final ActiveQName ARCHIVED = this.registerName("archived");
    private final ActiveQName GS_TRAVELBUGS = this.registerName("groundspeak:travelbugs");
    private final ActiveQName GS_PLACED_BY = this.registerName("groundspeak:placed_by");
    private final ActiveQName GS_OWNER = this.registerName("groundspeak:owner");
    private final ActiveQName GS_CONTAINER = this.registerName("groundspeak:container");
    private final ActiveQName GS_ATTRIBUTE = this.registerName("groundspeak:attribute");
    private final ActiveQName GS_ATTRIBUTE_INC = this.registerName("inc");
    private final ActiveQName GS_DIFFICULTY = this.registerName("groundspeak:difficulty");
    private final ActiveQName GS_TERRAIN = this.registerName("groundspeak:terrain");
    private final ActiveQName GS_COUNTRY = this.registerName("groundspeak:country");
    private final ActiveQName GS_STATE = this.registerName("groundspeak:state");
    private final ActiveQName GS_SHORT_DESCRIPTION = this.registerName("groundspeak:short_description");
    private final ActiveQName GS_LONG_DESCRIPTION = this.registerName("groundspeak:long_description");
    private final ActiveQName GS_HINT = this.registerName("groundspeak:encoded_hints");
    private final ActiveQName SYM = this.registerName("sym");
    private final ActiveQName TIME = this.registerName("time");
    private final ActiveQName CMT = this.registerName("cmt");
    private final ActiveQName TRAVELBUG = this.registerName("groundspeak:travelbug");
    private final ActiveQName LOGS = this.registerName("groundspeak:logs");
    private final ActiveQName LOG = this.registerName("groundspeak:log");
    private final ActiveQName LOG_TYPE = this.registerName("groundspeak:type");
    private final ActiveQName LOG_FINDER = this.registerName("groundspeak:finder");
    private final ActiveQName LOG_COMMENT = this.registerName("groundspeak:text");
    private final ActiveQName LOG_Date = this.registerName("groundspeak:date");


    public GroundspeakGpxFileImporter(Database database, ImportHandler importHandler) {
        super(database, importHandler);
    }


    @Override
    protected void startElement(ActiveQName name, StartElement element) {
        if (name.equals(WPT)) {
            // get latitude and longitude
            latitude = parseDouble(element.getAttributeByName(LAT));
            longitude = parseDouble(element.getAttributeByName(LON));
        } else if (WPT.isActive() && name.equals(GS_CACHE)) {
            // get available flags
            archived = parseBool(element.getAttributeByName(ARCHIVED));
            available = parseBool(element.getAttributeByName(AVAIABLE));
        } else if (GS_CACHE.isActive() && name.equals(GS_ATTRIBUTE)) {
            // add Attribute
            int id = parseInteger(element.getAttributeByName(ID));
            int inc = parseInteger(element.getAttributeByName(GS_ATTRIBUTE_INC));
            Attributes att = Attributes.getAttributeEnumByGcComId(id);
            if (att != null && att != Attributes.Default) {
                if (inc > 0) {
                    positiveAttributes.add(att);
                } else {
                    negativeAttributes.add(att);
                }
            }
        } else if (name.equals(TRAVELBUG)) {
            tbCount++;
        } else if (LOGS.isActive() && name.equals(LOG)) {
            logId = parseInteger(element.getAttributeByName(ID));
        }
    }


    @Override
    protected void endElement(ActiveQName name, EndElement element) {
        if (WPT.isActive() && name.equals(WPT)) {
            // end of Cache or Waypoint, create new Cache/Waypoint from Values
            createNewWPT();

            // clear values for next entry
            resetValues();
        } else if (LOGS.isActive() && name.equals(LOG)) {
            createNewLogEntry();
        }
    }

    @Override
    protected void data(ActiveQName name, Characters element) {
        if (WPT.isActive()) {
            if (TYPE.isActive()) {
                type = CacheTypes.parseString(element.getData());
            } else if (NAME.isActive()) {
                gcCode = element.getData();
                id = AbstractCache.GenerateCacheId(gcCode);
            } else if (URL.isActive()) {
                url = element.getData();
            } else if (SYM.isActive()) {
                found = element.getData().equals("Geocache Found");
            } else if (TIME.isActive()) {
                dateHidden = element.getData();
            } else if (CMT.isActive()) {
                shortDescription = element.getData();
            } else if (GS_CACHE.isActive()) {
                if (LOG.isActive()) {
                    if (LOG_TYPE.isActive()) {
                        logType = LogTypes.parseString(element.getData());
                    } else if (LOG_FINDER.isActive()) {
                        logFinder = element.getData();
                    } else if (LOG_COMMENT.isActive()) {
                        String data = replace(element.getData());
                        if ( logComment != null) {
                            logComment += data;
                        } else {
                            logComment = data;
                        }
                    } else if (LOG_Date.isActive()) {
                        logDate = element.getData();
                    }
                } else if (TITLE.isActive() && !GS_TRAVELBUGS.isActive()) {
                    title = element.getData();
                } else if (GS_PLACED_BY.isActive()) {
                    placed_by = element.getData();
                } else if (GS_OWNER.isActive()) {
                    owner = element.getData();
                } else if (GS_CONTAINER.isActive()) {
                    container = CacheSizes.parseString(element.getData());
                } else if (GS_DIFFICULTY.isActive()) {
                    int dif = Integer.parseInt(element.getData());
                    difficulty = (float) (dif / 2.0);
                } else if (GS_TERRAIN.isActive()) {
                    int ter = Integer.parseInt(element.getData());
                    terrain = (float) (ter / 2.0);
                } else if (GS_COUNTRY.isActive()) {
                    country = element.getData();
                } else if (GS_STATE.isActive()) {
                    state = element.getData();
                } else if (GS_SHORT_DESCRIPTION.isActive()) {
                    String data = replace(element.getData());
                    if (shortDescription != null) {
                        shortDescription += data;
                    } else {
                        shortDescription = data;
                    }
                } else if (GS_LONG_DESCRIPTION.isActive()) {
                    String data = replace(element.getData());
                    if (longDescription != null) {
                        longDescription += data;
                    } else {
                        longDescription = data;
                    }
                } else if (GS_HINT.isActive()) {
                    hint = element.getData();
                }
            }
        }

    }

    private String replace(String data) {
        return data.replaceAll("<", "&lt;").replaceAll(">", "&gt;");
    }


}
