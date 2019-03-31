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
import com.badlogic.gdx.utils.LongMap;
import com.badlogic.gdx.utils.XmlStreamParser;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.sqlite.Database;
import de.longri.cachebox3.sqlite.dao.DaoFactory;
import de.longri.cachebox3.types.*;
import de.longri.cachebox3.utils.CharSequenceUtil;
import de.longri.cachebox3.utils.NamedRunnable;
import de.longri.gdx.sqlite.GdxSqliteCursor;
import de.longri.gdx.sqlite.GdxSqlitePreparedStatement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

import static de.longri.cachebox3.types.MutableCache.MASK_FAVORITE;
import static de.longri.cachebox3.types.MutableCache.MASK_FOUND;

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
        cache.setHint( this.hint);
        cache.setLongDescription( this.longDescription);
        cache.setShortDescription( this.shortDescription);
        cache.setFound( this.found);
        cache.setNumTravelbugs((short) this.tbCount);
        cache.setTmpNote(note);
        cache.setTmpSolver( solver);
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
        if (database != null) {
            CB.postAsync(new NamedRunnable("Import Conflict handler") {
                @Override
                public void run() {
                    //Read all 'BooleanStore' values from Database for conflict handling
                    String sql = "SELECT Id, BooleanStore FROM CacheCoreInfo";

                    LongMap<Short> booleanStoreMap = new LongMap<>();
                    GdxSqliteCursor cursor = database.myDB.rawQuery(sql);

                    if (cursor != null) {
                        cursor.moveToFirst();
                        while (cursor.isAfterLast() == false) {
                            booleanStoreMap.put(cursor.getLong(0), cursor.getShort(1));
                            cursor.next();
                        }
                    }


                    //prepare statements
                    final GdxSqlitePreparedStatement REPLACE_LOGS = database.myDB.prepare("INSERT OR REPLACE INTO Logs VALUES(?,?,?,?,?,?) ;");
                    final GdxSqlitePreparedStatement REPLACE_WAYPOINT = database.myDB.prepare("INSERT OR REPLACE INTO Waypoints VALUES(?,?,?,?,?,?,?,?,?) ;");
                    final GdxSqlitePreparedStatement REPLACE_WAYPOINT_TEXT = database.myDB.prepare("INSERT OR REPLACE INTO WaypointsText VALUES(?,?,?) ;");
                    final GdxSqlitePreparedStatement REPLACE_CACHE_CORE = database.myDB.prepare("INSERT OR REPLACE INTO CacheCoreInfo VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?) ;");
                    final GdxSqlitePreparedStatement REPLACE_CACHE_INFO = database.myDB.prepare("INSERT OR REPLACE INTO CacheInfo VALUES(?,?,?,?,?,?,?,?,?) ;");
                    final GdxSqlitePreparedStatement REPLACE_CACHE_TEXT = database.myDB.prepare("INSERT OR REPLACE INTO CacheText VALUES(?,?,?,?,?,?,?) ;");
                    final GdxSqlitePreparedStatement REPLACE_ATTRIBUTE = database.myDB.prepare("INSERT OR REPLACE INTO Attributes VALUES(?,?,?,?,?) ;");


                    while (true) {

                        boolean sleep = true;

                        AbstractCache cache = null;
                        synchronized (resolveCacheConflicts) {
                            if (resolveCacheConflicts.size > 0)
                                cache = resolveCacheConflicts.pop();
                        }

                        AbstractWaypoint wp = null;
                        synchronized (resolveWaypoitConflicts) {
                            if (resolveWaypoitConflicts.size > 0)
                                wp = resolveWaypoitConflicts.pop();
                        }


                        LogEntry entry = null;
                        synchronized (storeLogEntry) {
                            if (storeLogEntry.size > 0)
                                entry = storeLogEntry.pop();
                        }


                        if (cache != null) {
                            sleep = false;


                            //get boolean store from Cache and check Favorite nad Found
                            Short booleanStore = booleanStoreMap.get(cache.getId());
                            if (booleanStore != null) {
                                boolean inDbFavorite = MutableCache.getMaskValue(MASK_FOUND, booleanStore);
                                boolean inDbFound = MutableCache.getMaskValue(MASK_FAVORITE, booleanStore);
                                cache.setFavorite(inDbFavorite);
                                if (inDbFound) {
                                    cache.setFound( true);
                                }
                            }

                            try {
                                REPLACE_CACHE_CORE.bind(
                                        cache.getId(),
                                        cache.getLatitude(),
                                        cache.getLongitude(),
                                        cache.getSize() != null ? cache.getSize().ordinal() : 0,
                                        (int) (cache.getDifficulty() * 2),
                                        (int) (cache.getTerrain() * 2),
                                        cache.getType().ordinal(),
                                        (int) (cache.getRating() * 200),
                                        cache.getNumTravelbugs(),
                                        cache.getGcCode(),
                                        cache.getName(),
                                        cache.getPlacedBy(),
                                        cache.getOwner(),
                                        cache.getGcId(),
                                        cache.getBooleanStore(),
                                        cache.getFavoritePoints(),
                                        (int) (cache.getRating() * 2)
                                ).commit();
                            } catch (Exception e) {
                                log.error("Can't write Cache " +
                                                "GC-Code:{}\n",
                                        cache.getGcCode()
                                );
                            } finally {
                                REPLACE_CACHE_CORE.reset();
                            }


                            Date dateHidden = cache.getDateHidden();
                            if (dateHidden == null) dateHidden = new Date();
                            String dateString = Database.cbDbFormat.format(dateHidden);

                            try {
                                REPLACE_CACHE_INFO.bind(
                                        cache.getId(),
                                        dateString,
                                        Database.cbDbFormat.format(new Date()),
                                        cache.getTourName(),
                                        cache.getGPXFilename_ID(),
                                        0,// TODO handle Listing CheckSum
                                        cache.getState(),
                                        cache.getCountry(),
                                        cache.getApiState()
                                ).commit();
                            } catch (Exception e) {
                                log.error("Can't write Cache Info" +
                                                "GC-Code:{}\n",
                                        cache.getGcCode()
                                );
                            } finally {
                                REPLACE_CACHE_INFO.reset();
                            }

                            try {
                                REPLACE_CACHE_TEXT.bind(
                                        cache.getId(),
                                        cache.getUrl(),
                                        cache.getHint(),
                                        cache.getLongDescription(),
                                        cache.getTmpNote(),
                                        cache.getTmpSolver(),
                                        cache.getShortDescription()
                                ).commit();
                            } catch (Exception e) {
                                log.error("Can't write Cache Text" +
                                                "GC-Code:{}\n",
                                        cache.getGcCode()
                                );
                            } finally {
                                REPLACE_CACHE_TEXT.reset();
                            }


                            long AttributesPositive = 0;
                            long AttributesPositiveHigh = 0;
                            long AttributesNegative = 0;
                            long AttributesNegativeHigh = 0;

                            if (cache.getAttributesPositive() != null) {
                                AttributesPositive = cache.getAttributesPositive().getLow();
                                AttributesPositiveHigh = cache.getAttributesPositive().getHigh();
                            }
                            if (cache.getAttributesNegative() != null) {
                                AttributesNegative = cache.getAttributesNegative().getLow();
                                AttributesNegativeHigh = cache.getAttributesNegative().getHigh();
                            }

                            try {
                                REPLACE_ATTRIBUTE.bind(
                                        cache.getId(),
                                        AttributesPositive,
                                        AttributesNegative,
                                        AttributesPositiveHigh,
                                        AttributesNegativeHigh
                                ).commit();
                            } catch (Exception e) {
                                log.error("Can't write Cache Attribute" +
                                                "GC-Code:{}\n",
                                        cache.getGcCode()
                                );
                            } finally {
                                REPLACE_ATTRIBUTE.reset();
                            }

                            if (importHandler != null) {
                                importHandler.incrementCaches(cache.getType() == CacheTypes.Mystery ? cache.getGcCode().toString() : null);
                            }

                        }

                        if (wp != null) {
                            sleep = false;

                            //TODO handle waypoint conflict

                            try {
                                REPLACE_WAYPOINT.bind(
                                        wp.getCacheId(),
                                        wp.getGcCode(),
                                        wp.getLatitude(),
                                        wp.getLongitude(),
                                        wp.getType().ordinal(),
                                        wp.isStart(),
                                        wp.isSyncExcluded(),
                                        wp.isUserWaypoint(),
                                        wp.getTitle()
                                ).commit();
                            } catch (Exception e) {
                                log.error("Can't write Waypoint  with values: \n" +
                                                "GC-Code:{}\n",
                                        wp.getGcCode()
                                );
                            } finally {
                                REPLACE_WAYPOINT.reset();
                            }


                            try {
                                REPLACE_WAYPOINT_TEXT.bind(
                                        wp.getGcCode(),
                                        wp.getDescription(),
                                        wp.getClue()
                                ).commit();
                            } catch (Exception e) {
                                log.error("Can't write Waypoint Text with values: \n" +
                                                "GC-Code:{}\n" +
                                                "Description:{}\n" +
                                                "Clue:{}\n\n\n",
                                        wp.getGcCode(),
                                        wp.getDescription(),
                                        wp.getClue()
                                );
                            } finally {
                                REPLACE_WAYPOINT_TEXT.reset();
                            }

                            if (importHandler != null) importHandler.incrementWaypoints();

                        }

                        if (entry != null) {
                            sleep = false;


                            try {
                                REPLACE_LOGS.bind(
                                        entry.Id,
                                        entry.CacheId,
                                        Database.cbDbFormat.format(entry.Timestamp == null ? new Date() : entry.Timestamp),
                                        entry.Finder,
                                        entry.Type,
                                        entry.Comment
                                ).commit();
                            } catch (Exception e) {
                                log.error("Can't write Log-Entry with values: \n" +
                                                "ID:{}\n" +
                                                "CacheID:{}\n" +
                                                "Date:{}\n" +
                                                "Finder:{}\n" +
                                                "Type:{}\n" +
                                                "Comment:{}\n\n\n",
                                        entry.Id, entry.CacheId,
                                        Database.cbDbFormat.format(entry.Timestamp == null ? new Date() : entry.Timestamp),
                                        entry.Finder,
                                        entry.Type,
                                        entry.Comment
                                );
                            } finally {
                                REPLACE_LOGS.reset();
                            }
                            if (importHandler != null) importHandler.incrementLogs();
                        }


                        if (sleep) {
                            try {
                                Thread.sleep(250);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }

                        boolean cacheEmpty = false;
                        boolean waypointEmpty = false;
                        boolean logsEmpty = false;

                        synchronized (resolveCacheConflicts) {
                            cacheEmpty = resolveCacheConflicts.size == 0;
                        }

                        synchronized (resolveWaypoitConflicts) {
                            waypointEmpty = resolveWaypoitConflicts.size == 0;
                        }

                        synchronized (storeLogEntry) {
                            logsEmpty = storeLogEntry.size == 0;
                        }

                        if (PARSE_READY.get() && cacheEmpty && waypointEmpty && logsEmpty)
                            break;

                    }
                    CONFLICT_READY.set(true);

                    //release statements
                    REPLACE_LOGS.close();
                    REPLACE_WAYPOINT.close();
                    REPLACE_WAYPOINT_TEXT.close();
                    REPLACE_CACHE_CORE.close();
                    REPLACE_CACHE_INFO.close();
                    REPLACE_CACHE_TEXT.close();
                    REPLACE_ATTRIBUTE.close();
                }
            });

        } else {
            CONFLICT_READY.set(true);
        }
    }

    protected Date parseDate(char[] data, int offset, int length) {
        return CharSequenceUtil.parseDate(this.locale, data, offset, length, DATE_PATTERN1, DATE_PATTERN2, DATE_PATTERN3);
    }
}
