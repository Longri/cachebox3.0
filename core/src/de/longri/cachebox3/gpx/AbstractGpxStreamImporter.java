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
import com.badlogic.gdx.utils.XmlStreamParser;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.sqlite.Database;
import de.longri.cachebox3.sqlite.dao.DaoFactory;
import de.longri.cachebox3.sqlite.dao.LogDAO;
import de.longri.cachebox3.types.*;
import de.longri.cachebox3.utils.CharSequenceUtil;
import de.longri.cachebox3.utils.NamedRunnable;
import de.longri.gdx.sqlite.GdxSqliteCursor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

import static de.longri.cachebox3.types.ImmutableCache.MASK_FAVORITE;
import static de.longri.cachebox3.types.ImmutableCache.MASK_FOUND;

/**
 * Created by Longri on 04.04.2018.
 */
public abstract class AbstractGpxStreamImporter extends XmlStreamParser {

    private final Logger log = LoggerFactory.getLogger(AbstractGpxStreamImporter.class);

    private final Locale locale = Locale.getDefault();

    private final char[] CREATOR = "creator".toCharArray();
    private final char[] GROUNDSPEAK = "Groundspeak".toCharArray();
    private final char[] OPENCACHING = "Opencaching".toCharArray();
    private final char[] GSAK = "GSAK".toCharArray();
    private final char[] VERSION = "version".toCharArray();
    private final char[] CACHEBOX = "Cachebox".toCharArray();
    private final char[] DATE_PATTERN1 = "yyyy-MM-dd'T'HH:mm:ss.S".toCharArray();
    private final char[] DATE_PATTERN2 = "yyyy-MM-dd'T'HH:mm:ss".toCharArray();
    private final char[] DATE_PATTERN3 = "yyyy-MM-dd'T'HH:mm:ss'Z'".toCharArray();

    private final Array<AbstractCache> resolveCacheConflicts = new Array<>();
    private final Array<AbstractWaypoint> resolveWaypoitConflicts = new Array<>();
    private final Array<LogEntry> storeLogEntry = new Array<>();
    private final AtomicBoolean PARSE_READY = new AtomicBoolean(true);
    private final AtomicBoolean CONFLICT_READY = new AtomicBoolean(true);
    private final Database database;
    private final ImportHandler importHandler;
    private final String GSAK_CORRECTED_COORDS = "Final GSAK Corrected";

    //Cache values
    protected double latitude;
    protected double longitude;
    protected CacheTypes type;
    protected String gcCode;
    protected String title;
    protected long id;
    protected boolean available;
    protected boolean archived;
    protected String placed_by;
    protected String owner;
    protected CacheSizes container;
    protected Array<Attributes> positiveAttributes = new Array<>();
    protected Array<Attributes> negativeAttributes = new Array<>();
    protected String url;
    protected float difficulty;
    protected float terrain;
    protected String country;
    protected String state;
    protected String shortDescription;
    protected String longDescription;
    protected String hint;
    protected boolean found;
    protected int tbCount;
    protected Date wpDate;
    protected String wpTitle;

    protected String gsakParent;

    protected long logId;
    protected Date logDate;
    protected String logFinder;
    protected String logComment;
    protected LogTypes logType;
    protected boolean hasCorrectedCoord;
    protected double correctedLatitude;
    protected double correctedLongitude;
    protected String note;
    protected int favPoints;
    protected String solver;
    protected String wpClue;


    public AbstractGpxStreamImporter(Database database, ImportHandler importHandler) {
        this.database = database;
        this.importHandler = importHandler;
    }


    protected abstract void registerGroundspeakHandler();

    protected abstract void registerOpenCachingHandler();

    protected abstract void registerGsakHandler();

    protected abstract void registerGsakHandler_1_1();

    protected abstract void registerGenerallyHandler();

    protected abstract void registerCacheboxHandler();

    public void doImport(FileHandle gpxFile) {

        if (gpxFile == null)
            throw new RuntimeException("Can't import NULL");
        if (!gpxFile.exists())
            throw new RuntimeException("Can't import non exist File");
        if (!gpxFile.file().canRead())
            throw new RuntimeException("Can't import non readable File");

        this.registerValueHandler("/gpx",
                new ValueHandler() {

                    String version = "";

                    @Override
                    protected void handleValue(char[] valueName, char[] data, int offset, int length) {
                        if (CharSequenceUtil.equals(VERSION, valueName)) {
                            version = new String(data, offset, length);
                        }

                        if (CharSequenceUtil.equals(CREATOR, valueName)) {
                            registerGenerallyHandler();
                            if (CharSequenceUtil.contains(data, offset, length, GROUNDSPEAK, 0, GROUNDSPEAK.length)) {
                                registerGroundspeakHandler();
                            } else if (CharSequenceUtil.contains(data, offset, length, OPENCACHING, 0, OPENCACHING.length)) {
                                registerOpenCachingHandler();
                            } else if (CharSequenceUtil.contains(data, offset, length, GSAK, 0, GSAK.length)) {
                                if (version.equals("1.0")) {
                                    registerGsakHandler();
                                } else {
                                    registerGsakHandler_1_1();
                                }

                            } else if (CharSequenceUtil.contains(data, offset, length, CACHEBOX, 0, CACHEBOX.length)) {
                                registerCacheboxHandler();
                            }
                        }
                    }
                }, VERSION, CREATOR);


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
        } catch (Exception e) {
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

        //release Data/Value Handler
        endTagHandlerMap.clear();
        dataHandlerMap.clear();
        valueHandlerMap.clear();
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
        gsakParent = null;
        logId = 0;
        wpDate = null;
        tbCount = 0;
        wpTitle = null;
        hasCorrectedCoord = false;
        correctedLatitude = 0;
        correctedLongitude = 0;
        note = null;
        favPoints = -1;
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

    private void createCache() {
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
        cache.setFound(database, this.found);
        cache.setNumTravelbugs(this.tbCount);
        cache.setTmpNote(note);
        cache.setTmpSolver(database, solver);
        if (favPoints >= 0) cache.setFavoritePoints(favPoints);


        if (this.wpDate != null) {
            cache.setDateHidden(this.wpDate);
        }

        for (Attributes att : positiveAttributes)
            cache.addAttributePositive(att);

        for (Attributes att : negativeAttributes)
            cache.addAttributeNegative(att);

        resolveCacheConflicts.add(cache);

        if (hasCorrectedCoord) {
            // create final WP with Corrected Coords
            String newGcCode = Database.createFreeGcCode(database, cache.getGcCode().toString());

            // Check if "Final GSAK Corrected" exist
            Array<AbstractWaypoint> wplist = DaoFactory.WAYPOINT_DAO.getWaypointsFromCacheID(database, this.id, false);

            for (int i = 0; i < wplist.size; i++) {
                AbstractWaypoint wp = wplist.get(i);
                if (wp.getType() == CacheTypes.Final) {
                    if (CharSequenceUtil.equals(wp.getTitle(), GSAK_CORRECTED_COORDS)) {
                        newGcCode = wp.getGcCode().toString();
                        break;
                    }
                }
            }

            // "Final GSAK Corrected" is used for recognition of finals from GSAK on gpx - Import
            AbstractWaypoint finalWP = new MutableWaypoint(correctedLatitude, correctedLongitude, this.id);
            finalWP.setType(CacheTypes.Final);
            finalWP.setGcCode(newGcCode);
            finalWP.setTitle(GSAK_CORRECTED_COORDS);
            resolveWaypoitConflicts.add(finalWP);
        }

        resetValues();
    }

    private void createWaypoint() {

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
        waypoint.setTitle(this.wpTitle);
        waypoint.setClue(this.wpClue);

        resolveWaypoitConflicts.add(waypoint);
        resetValues();
    }

    protected void createNewLogEntry() {
        LogEntry newLogEntry = new LogEntry();
        newLogEntry.CacheId = this.id;
        newLogEntry.Id = this.logId;
        newLogEntry.Timestamp = this.logDate;
        newLogEntry.Finder = this.logFinder;
        newLogEntry.Comment = this.logComment;
        newLogEntry.Type = this.logType;

        storeLogEntry.add(newLogEntry);
        resetLogValues();
    }

    private void handleConflictsAndStoreToDB() {
        CB.postAsync(new NamedRunnable("Import Conflict handler") {
            @Override
            public void run() {
                database.beginTransaction();
                while (!PARSE_READY.get() || resolveCacheConflicts.size > 0
                        || resolveWaypoitConflicts.size > 0
                        || storeLogEntry.size > 0) {

                    boolean sleep = true;

                    if (resolveCacheConflicts.size > 0) {
                        sleep = false;
                        AbstractCache cache = resolveCacheConflicts.pop();

                        //get boolean store from Cache and check Favorite nad Found
                        GdxSqliteCursor cursor = database.rawQuery("SELECT BooleanStore FROM CacheCoreInfo WHERE id=" + cache.getId());
                        boolean update = false;
                        if (cursor != null) {
                            update = true;
                            cursor.moveToFirst();
                            short booleanStore = cursor.getShort(0);
                            cursor.close();

                            boolean inDbFavorite = ImmutableCache.getMaskValue(MASK_FOUND, booleanStore);
                            boolean inDbFound = ImmutableCache.getMaskValue(MASK_FAVORITE, booleanStore);
                            cache.setFavorite(database, inDbFavorite);
                            if (inDbFound) {
                                cache.setFound(database, true);
                            }
                        }

                        if (update) {
                            DaoFactory.CACHE_DAO.updateDatabase(database, cache, false);
                        } else {
                            DaoFactory.CACHE_DAO.writeToDatabase(database, cache, false);
                        }
                        if (importHandler != null) {
                            importHandler.incrementCaches(cache.getType() == CacheTypes.Mystery ? cache.getGcCode().toString() : null);
                        }
                    } else if (resolveWaypoitConflicts.size > 0) {
                        sleep = false;
                        AbstractWaypoint waypoint = resolveWaypoitConflicts.pop();

                        //TODO handle waypoint conflict
                        DaoFactory.WAYPOINT_DAO.writeToDatabase(database, waypoint, false);
                        if (importHandler != null) importHandler.incrementWaypoints();
                    } else if (storeLogEntry.size > 0) {
                        sleep = false;
                        LogDAO dao = new LogDAO();
                        Array<LogEntry> writeList = new Array<>();
                        while (storeLogEntry.size > 0) {
                            writeList.add(storeLogEntry.pop());
                            if (importHandler != null) importHandler.incrementLogs();
                        }
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
                database.endTransaction();
            }
        });
    }

    protected Date parseDate(char[] data, int offset, int length) {
        return CharSequenceUtil.parseDate(this.locale, data, offset, length, DATE_PATTERN1, DATE_PATTERN2, DATE_PATTERN3);
    }
}