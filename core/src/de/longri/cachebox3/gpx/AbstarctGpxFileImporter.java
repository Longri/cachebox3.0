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
import de.longri.cachebox3.sqlite.dao.LogDAO;
import de.longri.cachebox3.types.*;
import de.longri.cachebox3.utils.NamedRunnable;
import de.longri.cachebox3.utils.XmlStreamEventParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.stream.XMLStreamException;
import java.io.FileNotFoundException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Longri on 01.04.18.
 */
public abstract class AbstarctGpxFileImporter extends XmlStreamEventParser {

    private final Logger log = LoggerFactory.getLogger(AbstarctGpxFileImporter.class);

    private final SimpleDateFormat DATE_PATTERN_1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.S", Locale.getDefault());
    private final SimpleDateFormat DATE_PATTERN_3 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());
    private final SimpleDateFormat DATE_PATTERN_2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());

    private final Array<AbstractCache> resolveCacheConflicts = new Array<>();
    private final Array<AbstractWaypoint> resolveWaypoitConflicts = new Array<>();
    private final Array<LogEntry> storeLogEntry = new Array<>();
    private final AtomicBoolean PARSE_READY = new AtomicBoolean(true);
    private final AtomicBoolean CONFLICT_READY = new AtomicBoolean(true);
    private final Database database;
    private final ImportHandler importHandler;

    //Cache values
    double latitude;
    double longitude;
    CacheTypes type;
    String gcCode;
    String title;
    long id;
    boolean available;
    boolean archived;
    String placed_by;
    String owner;
    CacheSizes container;
    Array<Attributes> positiveAttributes = new Array<>();
    Array<Attributes> negativeAttributes = new Array<>();
    String url;
    float difficulty;
    float terrain;
    String country;
    String state;
    String shortDescription;
    String longDescription;
    String hint;
    boolean found;
    String dateHidden;
    String gsakParent;
    int tbCount;

    int logId;
    String logDate;
    String logFinder;
    String logComment;
    LogTypes logType;

    public AbstarctGpxFileImporter(Database database, ImportHandler importHandler) {
        super();
        this.database = database;
        this.importHandler = importHandler;
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


    protected void resetValues() {
        latitude = 0;
        longitude = 0;
        type = null;
        gcCode = null;
        title = null;
        id = 0;
        available = false;
        archived = false;
        placed_by = null;
        owner = null;
        container = null;
        positiveAttributes.clear();
        negativeAttributes.clear();
        url = null;
        difficulty = 0;
        terrain = 0;
        country = null;
        state = null;
        shortDescription = null;
        longDescription = null;
        hint = null;
        found = false;
        dateHidden = null;
        gsakParent = null;
        tbCount = 0;

        resetLogValues();
    }

    private void resetLogValues() {
        logId = 0;
        logDate = null;
        logFinder = null;
        logComment = null;
        logType = LogTypes.unknown;
    }

    protected void createNewWPT() {
        if (type.isCache()) createCache();
        else createWaypoint();
    }

    protected void createCache() {
        AbstractCache cache = new MutableCache(this.latitude, this.longitude);
        cache.setType(this.type);
        cache.setGcCode(this.gcCode);
        cache.setName(this.title);
        cache.setId(this.id);
        cache.setArchived(this.archived);
        cache.setAvailable(this.available);
        cache.setPlacedBy(this.placed_by);
        cache.setOwner(this.owner);
        cache.setSize(this.container);
        cache.setUrl(this.url);
        cache.setDifficulty(this.difficulty);
        cache.setTerrain(this.terrain);
        cache.setCountry(this.country);
        cache.setState(this.state);
        cache.setHint(database, this.hint);
        cache.setLongDescription(database, this.longDescription);
        cache.setShortDescription(database, this.shortDescription);
        cache.setFound(this.found);
        cache.setNumTravelbugs(this.tbCount);
        if (this.dateHidden != null) {
            // try to parse
            try {
                Date hidden = parseDate(this.dateHidden);
                cache.setDateHidden(hidden);
            } catch (Exception e) {
                log.error("Parse hidden wptDate string", e);
            }
        }

        for (Attributes att : positiveAttributes)
            cache.addAttributePositive(att);

        for (Attributes att : negativeAttributes)
            cache.addAttributeNegative(att);


        resolveCacheConflicts.add(cache);
    }

    protected void createWaypoint() {
        long cacheId;

        if (gsakParent != null) {
            cacheId = AbstractCache.GenerateCacheId(gsakParent);
        } else {
            cacheId = AbstractCache.GenerateCacheId("GC" + this.gcCode.substring(2, this.gcCode.length()));
        }

        AbstractWaypoint waypoint = new MutableWaypoint(this.latitude, this.longitude, cacheId);
        waypoint.setDescription(this.shortDescription);
        waypoint.setType(this.type);
        waypoint.setGcCode(this.gcCode);

        resolveWaypoitConflicts.add(waypoint);
        resetValues();
    }

    protected void createNewLogEntry() {
        LogEntry newLogEntry = new LogEntry();
        newLogEntry.CacheId = this.id;
        newLogEntry.Id = this.logId;
        newLogEntry.Finder = this.logFinder;
        newLogEntry.Comment = this.logComment;
        newLogEntry.Type = this.logType;
        if (this.logDate != null) {
            // try to parse
            try {
                Date date = parseDate(this.logDate);
                newLogEntry.Timestamp = date;
            } catch (Exception e) {
                log.error("Parse hidden wptDate string", e);
            }
        }
        storeLogEntry.add(newLogEntry);
        resetLogValues();
    }

    private void handleConflictsAndStoreToDB() {
        CB.postAsync(new NamedRunnable("Import Conflict handler") {
            @Override
            public void run() {
                while (!PARSE_READY.get() || resolveCacheConflicts.size > 0
                        || resolveWaypoitConflicts.size > 0
                        || storeLogEntry.size > 0) {

                    boolean sleep = true;

                    if (resolveCacheConflicts.size > 0) {
                        sleep = false;
                        AbstractCache cache = resolveCacheConflicts.pop();

                        //TODO handle cache conflict
                        DaoFactory.CACHE_DAO.writeToDatabase(database, cache, false);
                    }
                    if (resolveWaypoitConflicts.size > 0) {
                        sleep = false;
                        AbstractWaypoint waypoint = resolveWaypoitConflicts.pop();

                        //TODO handle waypoint conflict
                        DaoFactory.WAYPOINT_DAO.writeToDatabase(database, waypoint, false);
                    }

                    if (storeLogEntry.size > 0) {
                        sleep = false;
                        LogDAO dao = new LogDAO();
                        Array<LogEntry> writeList = new Array<>();
                        while (storeLogEntry.size > 0)
                            writeList.add(storeLogEntry.pop());
                        dao.writeToDB(database, writeList);
                    }

                    if (sleep) {
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

    private Date parseDate(String text) throws Exception {
        Date date = parseDateWithFormat(DATE_PATTERN_1, text);
        if (date != null) {
            return date;
        } else {
            date = parseDateWithFormat(DATE_PATTERN_2, text);
            if (date != null) {
                return date;
            } else {
                date = parseDateWithFormat(DATE_PATTERN_3, text);
                if (date != null) {
                    return date;
                } else {
                    throw new ParseException("Illegal wptDate format", 0);
                }
            }
        }
    }

    private Date parseDateWithFormat(SimpleDateFormat df, String text) {
        // TODO write an own parser, original works but to match.

        Date date = null;
        try {
            date = df.parse(text);
        } catch (ParseException e) {
        }
        return date;
    }
}
