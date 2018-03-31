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

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.sqlite.Database;
import de.longri.cachebox3.sqlite.dao.DaoFactory;
import de.longri.cachebox3.types.AbstractCache;
import de.longri.cachebox3.types.AbstractWaypoint;
import de.longri.cachebox3.types.CacheTypes;
import de.longri.cachebox3.types.MutableCache;
import de.longri.cachebox3.utils.NamedRunnable;
import de.longri.cachebox3.utils.XmlStreamEventParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import java.io.FileNotFoundException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Longri on 29.03.2018.
 */
public class GroundspeakGpxFileImporter extends XmlStreamEventParser {
    private final static Logger log = LoggerFactory.getLogger(GroundspeakGpxFileImporter.class);

    // QNames
    private final QName WPT = new QName("wpt");
    private final QName LAT = new QName("lat");
    private final QName LON = new QName("lon");
    private final QName TYPE = new QName("type");
    private final QName NAME = new QName("name");

    // active tag
    private boolean tag_wpt;
    private boolean tag_type;
    private boolean tag_name;

    //Cache values
    double latitude;
    double longitude;
    CacheTypes type;
    String gcCode;

    private final Array<AbstractCache> resolveCacheConflicts = new Array<>();
    private final Array<AbstractWaypoint> resolveWaypoitConflicts = new Array<>();
    private Database database;
    private final AtomicBoolean PARSE_READY = new AtomicBoolean(true);
    private final AtomicBoolean CONFLICT_READY = new AtomicBoolean(true);


    public GroundspeakGpxFileImporter(Database database, ImportHandler importHandler) {
        super();
        this.database = database;

    }

    public void doImport(FileHandle gpxFile) {

        // wait, if parser working now
        while (!PARSE_READY.get() || !CONFLICT_READY.get()) {
            try {
                Thread.sleep(250);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        PARSE_READY.set(false);
        CONFLICT_READY.set(false);
        handleConflictsAndStoreToDB(); // start async Task

        try {
            this.parse(gpxFile);
        } catch (FileNotFoundException e) {
            log.error("parse Gpx", e);
        } catch (XMLStreamException e) {
            log.error("parse Gpx", e);
        }
        PARSE_READY.set(true);

        //wait, for conflict handler is ready
        while (!CONFLICT_READY.get()) {
            try {
                Thread.sleep(250);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void startElement(StartElement element) {
        if (element.getName().equals(WPT)) {
            tag_wpt = true;
            // get latitude and longitude
            latitude = parseDouble(element.getAttributeByName(LAT));
            longitude = parseDouble(element.getAttributeByName(LON));
        } else if (tag_wpt && element.getName().equals(TYPE)) {
            tag_type = true;
        } else if (tag_wpt && element.getName().equals(NAME)) {
            tag_name = true;
        }
    }

    @Override
    protected void endElement(EndElement element) {
        if (tag_wpt && element.getName().equals(WPT)) {
            // end of Cache or Waypoint, create new Cache/Waypoint from Values
            createNewWPT();

            // clear values for next entry
            resetValues();

            tag_wpt = false;
        } else if (tag_type && element.getName().equals(TYPE)) {
            tag_type = false;
        } else if (tag_name && element.getName().equals(NAME)) {
            tag_name = false;
        }
    }

    @Override
    protected void data(Characters element) {
        if (tag_wpt) {
            if (tag_type) {
                type = CacheTypes.parseString(element.getData());
            } else if (tag_name) {
                gcCode = element.getData();
            }
        }
    }

    private void resetValues() {
        latitude = 0;
        longitude = 0;
        type = CacheTypes.Undefined;
        gcCode = null;
    }

    private void createNewWPT() {
        if (type.isCache()) createCache();
        else createWaypoint();
    }

    private void createCache() {
        AbstractCache cache = new MutableCache(this.latitude, this.longitude);
        cache.setType(this.type);
        cache.setGcCode(this.gcCode);


        resolveCacheConflicts.add(cache);
    }

    private void createWaypoint() {

    }


    private void handleConflictsAndStoreToDB() {
        CB.postAsync(new NamedRunnable("Import Conflict handler") {
            @Override
            public void run() {
                while (!PARSE_READY.get() || resolveCacheConflicts.size > 0 || resolveWaypoitConflicts.size > 0) {

                    if (resolveCacheConflicts.size > 0) {
                        AbstractCache cache = resolveCacheConflicts.pop();

                        //TODO handle cache conflict
                        DaoFactory.CACHE_DAO.writeToDatabase(database, cache);
                    } else if (resolveWaypoitConflicts.size > 0) {
                        AbstractWaypoint waypoint = resolveWaypoitConflicts.pop();

                        //TODO handle waypoint conflict
                        DaoFactory.WAYPOINT_DAO.writeToDatabase(database, waypoint);
                    } else {
                        try {
                            Thread.sleep(250);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                }
                CONFLICT_READY.set(true);
            }
        });
    }
}
