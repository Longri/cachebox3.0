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
import de.longri.cachebox3.types.*;
import de.longri.cachebox3.utils.CharSequenceUtil;
import de.longri.cachebox3.utils.NamedRunnable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Longri on 04.04.2018.
 */
public abstract class AbstractGpxStreamImporter extends XmlStreamParser {

    private final Logger log = LoggerFactory.getLogger(AbstractGpxStreamImporter.class);

    protected enum WptTypes {
        Cache, Waypoint, UNKNOWN
    }

    private final Locale locale = Locale.getDefault();

    private final char[] DATE_PATTERN1 = "yyyy-MM-dd'T'HH:mm:ss.S".toCharArray();
    private final char[] DATE_PATTERN2 = "yyyy-MM-dd'T'HH:mm:ss".toCharArray();
    private final char[] DATE_PATTERN3 = "yyyy-MM-dd'T'HH:mm:ss'Z'".toCharArray();

    private final Array<AbstractCache> resolveCacheConflicts = new Array<>();
    private final Array<AbstractWaypoint> resolveWaypoitConflicts = new Array<>();
    private final AtomicBoolean PARSE_READY = new AtomicBoolean(true);
    private final AtomicBoolean CONFLICT_READY = new AtomicBoolean(true);
    private final Database database;
    private final ImportHandler importHandler;

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
    protected Date dateHidden;


    public AbstractGpxStreamImporter(Database database, ImportHandler importHandler) {
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
        cache.setFound(this.found);
        if (this.dateHidden != null) {
            cache.setDateHidden(this.dateHidden);
        }

        for (Attributes att : positiveAttributes)
            cache.addAttributePositive(att);

        for (Attributes att : negativeAttributes)
            cache.addAttributeNegative(att);


        resolveCacheConflicts.add(cache);
        resetValues();
    }

    private void createWaypoint() {
        //TODO
        resetValues();
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

    protected Date parseDate(char[] data, int offset, int length) {
        return CharSequenceUtil.parseDate(this.locale, data, offset, length, DATE_PATTERN1, DATE_PATTERN2, DATE_PATTERN3);
    }
}
