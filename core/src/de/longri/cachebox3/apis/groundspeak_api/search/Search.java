/* 
 * Copyright (C) 2014 - 2017 team-cachebox.de
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
package de.longri.cachebox3.apis.groundspeak_api.search;

import com.badlogic.gdx.Net;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonParser;
import com.badlogic.gdx.utils.JsonStreamParser;
import com.badlogic.gdx.utils.async.AsyncExecutor;
import com.badlogic.gdx.utils.async.AsyncTask;
import de.longri.cachebox3.apis.groundspeak_api.ApiResultState;
import de.longri.cachebox3.apis.groundspeak_api.PostRequest;
import de.longri.cachebox3.callbacks.GenericCallBack;
import de.longri.cachebox3.events.EventHandler;
import de.longri.cachebox3.events.ImportProgressChangedEvent;
import de.longri.cachebox3.settings.Config;
import de.longri.cachebox3.sqlite.Database;
import de.longri.cachebox3.sqlite.dao.CacheDAO;
import de.longri.cachebox3.sqlite.dao.ImageDAO;
import de.longri.cachebox3.sqlite.dao.LogDAO;
import de.longri.cachebox3.sqlite.dao.WaypointDAO;
import de.longri.cachebox3.types.*;
import de.longri.cachebox3.utils.ICancel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.Date;

/**
 * Search Definitions
 *
 * @author Hubert
 * @author Longri
 */
public abstract class Search extends PostRequest {
    static Logger log = LoggerFactory.getLogger(Search.class);

    public int number;
    public boolean excludeHides = false;
    public boolean excludeFounds = false;
    public boolean available = false;
    public int cacheCount;
    int waypointCount;
    public int logCount;
    int imageCount;
    private boolean isLite;
    private long gpxFilenameId;
    private double actLat, actLon, actWpLat, actWpLon;
    private Cache actCache;
    private Waypoint actWayPoint;
    private LogEntry actLog;
    private Array<Attributes> attributes;
    private Attributes actAttribute;

    private final String GEOCACHES = "Geocaches";
    private final String LOGS = "GeocacheLogs";
    private final String WAYPOINTS = "AdditionalWaypoints";
    private final String ATTRIBUTES = "Attributes";
    private final String OWNER = "Owner";
    private final String IMAGES = "Images";

    private final String ATTRIBUTE_ID = "AttributeTypeID";
    private final String IS_ON = "IsOn";
    private final String NEW_CACHE = "NEW_CACHE";
    private final String NEW_LOG = "NEW_LOG";
    private final String NEW_WAY_POINT = "NEW_WAY_POINT";
    private final String CODE = "Code";
    private final String CACHE_TYPE = "CacheType";
    private final String CACHE_TYPE_ID = "GeocacheTypeId";
    private final String COUNTRY = "Country";
    private final String DATE_HIDDEN = "DateCreated";
    private final String DIFFICULTY = "Difficulty";
    private final String HINT = "EncodedHints";
    private final String FAVRITE_POINTS = "FavoritePoints";
    private final String FOUND = "HasbeenFoundbyUser";
    private final String ID = "ID";
    private final String LONG_DESC = "LongDescription";
    private final String NAME = "Name";
    private final String USER_NAME = "UserName";
    private final String PLACED_BY = "PlacedBy";
    private final String SHORT_DESC = "ShortDescription";
    private final String DESC = "Description";
    private final String TERRAIN = "Terrain";
    private final String URL = "Url";
    private final String LAT = "Latitude";
    private final String LON = "Longitude";
    private final String CACHE_LIMITS = "CacheLimits";
    private final String LOG_TEXT = "LogText";
    private final String VISIT_DATE = "VisitDate";
    private final String LOG_TYPE_ID = "WptLogTypeId";
    private final String ADDITIONAL_WAYPOINTS = "AdditionalWaypoints";
    private final String USER_WAYPOINTS = "UserWaypoints";
    private final String COMMENT = "Comment";
    private final String WAYPOINT_TYPE_ID = "WptTypeID";

    private final CacheDAO cacheDAO = new CacheDAO();
    private final LogDAO logDAO = new LogDAO();
    private final ImageDAO imageDAO = new ImageDAO();
    private final WaypointDAO waypointDAO = new WaypointDAO();

    protected int geocacheLogCount = 10;
    protected int trackableLogCount = 10;


    private int attributeID;
    private boolean isOn;

    private Array<String> arrayStack = new Array<>();
    private Array<String> objectStack = new Array<>();

    private int SWITCH = 0;
    private final int CACHE_ARRAY = 1;
    private final int ATTRIBUTE_ARRAY = 2;
    private final int LOG_ARRAY = 3;
    private final int CACHE_LIMITS_ARRAY = 4;
    private final int IMAGE_ARRAY = 5;
    private final int WAY_POINT_ARRAY = 6;
    private boolean isUserWaypoint = false;
    private final ICancel iCancel;

    /**
     * 0 = unknown, 1 = Basic Member, 2 = Premium Member
     */
    private byte apiState;

    /**
     * @param gcApiKey valid encrypted Api-Key
     * @param number   MaxPerPage size for this request
     * @param apiState 0 = unknown, 1 = Basic Member, 2 = Premium Member
     */
    Search(String gcApiKey, int number, byte apiState, ICancel iCancel) {
        super(gcApiKey, iCancel);
        this.iCancel = iCancel;
        if (number > 50)
            throw new RuntimeException("Max CacheCount per Page is 50, " + number + "will produce a API Error");

        this.number = number;
        this.apiState = apiState;
    }

    @Override
    protected String getCallUrl() {
        return "SearchForGeocaches?format=json";
    }

    @Override
    protected void handleHttpResponse(Net.HttpResponse httpResponse, final GenericCallBack<ApiResultState> readyCallBack) {
        final InputStream stream = httpResponse.getResultAsStream();
        long length = 1;


        //fire progress event for begin parsing
        ImportProgressChangedEvent.ImportProgress progress = new ImportProgressChangedEvent.ImportProgress();
        progress.progress = 1;
        progress.caches = 0;
        progress.wayPoints = 0;
        progress.logs = 0;
        progress.images = 0;
        progress.msg = "Start parsing result";
        EventHandler.fire(new ImportProgressChangedEvent(progress));


        try {
            String lengthString = httpResponse.getHeader("Content-Length");
            if (lengthString == null || lengthString.isEmpty()) {
                length = 1;
            } else {
                length = Long.parseLong(lengthString);
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }

        JsonStreamParser parser = new JsonStreamParser() {

            @Override
            public void startArray(String name) {

                if (iCancel != null && iCancel.cancel()) this.cancel();

                super.startArray(name);
                // System.out.println("Start array " + name);
                arrayStack.add(name);
                objectStack.add(name);
                if (ATTRIBUTES.equals(name)) {
                    attributes = new Array<>();
                    SWITCH = ATTRIBUTE_ARRAY;
                } else if (GEOCACHES.equals(name)) {
                    SWITCH = CACHE_ARRAY;
                } else if (CACHE_LIMITS.equals(name)) {
                    SWITCH = CACHE_LIMITS_ARRAY;
                } else if (LOGS.equals(name)) {
                    SWITCH = LOG_ARRAY;
                } else if (IMAGES.equals(name)) {
                    SWITCH = IMAGE_ARRAY;
                } else if (ADDITIONAL_WAYPOINTS.equals(name)) {
                    SWITCH = WAY_POINT_ARRAY;
                } else if (USER_WAYPOINTS.equals(name)) {
                    SWITCH = WAY_POINT_ARRAY;
                    isUserWaypoint = true;
                }
            }

            @Override
            public void endArray(String name) {
                if (iCancel != null && iCancel.cancel()) this.cancel();
                arrayStack.pop();
                isUserWaypoint = false;
                if (arrayStack.size > 0) {

                    String actArray = arrayStack.peek();

                    if (ATTRIBUTES.equals(actArray)) {
                        attributes = new Array<>();
                        SWITCH = ATTRIBUTE_ARRAY;
                    } else if (GEOCACHES.equals(actArray)) {
                        SWITCH = CACHE_ARRAY;
                    } else if (CACHE_LIMITS.equals(actArray)) {
                        SWITCH = CACHE_LIMITS_ARRAY;
                    } else if (LOGS.equals(actArray)) {
                        SWITCH = LOG_ARRAY;
                    } else if (IMAGES.equals(actArray)) {
                        SWITCH = IMAGE_ARRAY;
                    } else if (ADDITIONAL_WAYPOINTS.equals(name)) {
                        SWITCH = WAY_POINT_ARRAY;
                    } else if (USER_WAYPOINTS.equals(name)) {
                        SWITCH = WAY_POINT_ARRAY;
                    }
                } else {
                    SWITCH = 0;
                }


            }

            @Override
            public void startObject(String name) {
                if (iCancel != null && iCancel.cancel()) this.cancel();
                super.startObject(name);
                // System.out.println("Start Object " + name);

                switch (SWITCH) {
                    case CACHE_ARRAY:
                        if (actCache == null) {
                            // System.out.println("NEW_CACHE");
                            actCache = new Cache(0, 0, true);
                            name = NEW_CACHE;
                        }
                        break;
                    case ATTRIBUTE_ARRAY:

                        break;
                    case LOG_ARRAY:
                        if (actLog == null) {
                            // System.out.println("NEW_LOG_ENTRY");
                            actLog = new LogEntry();
                            actLog.CacheId = actCache.Id;
                            name = NEW_LOG;
                        }
                        break;
                    case WAY_POINT_ARRAY:
                        if (actWayPoint == null) {
                            //System.out.println("NEW_WayPoint");
                            actWayPoint = new Waypoint(0, 0, true);
                            name = NEW_WAY_POINT;
                        }
                        break;
                }


                objectStack.add(name);
            }

            @Override
            public void pop() {
                if (iCancel != null && iCancel.cancel()) this.cancel();
                super.pop();
                String name = objectStack.pop();
                // System.out.println("pop " + name);


                switch (SWITCH) {
                    case CACHE_ARRAY:
                        if (NEW_CACHE.equals(name)) {
                            //store cache
                            actCache.setApiState(apiState);

                            //add final Cache instance
                            writeCacheToDB(new Cache(actLat, actLon, actCache));

                            ImportProgressChangedEvent.ImportProgress progress = new ImportProgressChangedEvent.ImportProgress();
                            progress.progress = this.getProgress();
                            progress.caches = ++cacheCount;
                            progress.wayPoints = waypointCount;
                            progress.logs = logCount;
                            progress.images = imageCount;
                            progress.msg = "store Cache: " + actCache.toString();
                            EventHandler.fire(new ImportProgressChangedEvent(progress));

                            actCache = null;
                            log.debug("Stream parse new Cache StreamAvailable:{}/{}");
                        }
                        break;
                    case ATTRIBUTE_ARRAY:
                        actAttribute = Attributes.getAttributeEnumByGcComId(attributeID);
                        if (isOn) {
                            // System.out.println("add positive Attribute: " + actAttribute);
                            actCache.addAttributePositive(actAttribute);
                        } else {
                            // System.out.println("add negative Attribute: " + actAttribute);
                            actCache.addAttributeNegative(actAttribute);
                        }
                        break;
                    case LOG_ARRAY:
                        if (NEW_LOG.equals(name)) {
                            // System.out.println("add Log entry ");
                            writeLogToDB(actLog);
                            logCount++;
                            actLog = null;
                        }
                        break;
                    case WAY_POINT_ARRAY:
                        if (NEW_WAY_POINT.equals(name)) {
                            // System.out.println("add Waypoiint ");
                            actWayPoint.CacheId = actCache.Id;
                            if (isUserWaypoint) {
                                actWayPoint.IsUserWaypoint = true;
                                actWayPoint.setTitle("Corrected Coordinates (API)");
                                actWayPoint.setDescription("");
                                actWayPoint.Type = CacheTypes.Final;
                                actWayPoint.setGcCode("CO" + actCache.getGcCode().substring(2, actCache.getGcCode().length()));
                            }

                            //add final Waypointg instance
                            actCache.waypoints.add(new Waypoint(actWpLat, actWpLon, actWayPoint));
                            waypointCount++;
                            actWayPoint = null;
                        }
                        break;
                }
            }

            @Override
            public void string(String name, String value) {
                if (iCancel != null && iCancel.cancel()) this.cancel();
                super.string(name, value);

                switch (SWITCH) {
                    case CACHE_ARRAY:
                        if (LONG_DESC.equals(name)) {
                            actCache.setLongDescription(value);
                        } else if (CODE.equals(name)) {
                            actCache.setGcCode(value);
                            actCache.Id = Cache.GenerateCacheId(actCache.getGcCode());
                        } else if (COUNTRY.equals(name)) {
                            actCache.setCountry(value);
                        } else if (DATE_HIDDEN.equals(name)) {
                            actCache.setDateHidden(getDateFromLongString(value));
                        } else if (HINT.equals(name)) {
                            actCache.setHint(value);
                        } else if (ID.equals(name)) {
                            actCache.setGcId(value);
                        } else if (NAME.equals(name)) {
                            actCache.setName(value);
                        } else if (USER_NAME.equals(name)) {
                            actCache.setOwner(value);
                        } else if (PLACED_BY.equals(name)) {
                            actCache.setPlacedBy(value);
                        } else if (SHORT_DESC.equals(name)) {
                            actCache.setShortDescription(value);
                        } else if (URL.equals(name)) {
                            actCache.setUrl(value);
                        }
                        break;
                    case ATTRIBUTE_ARRAY:

                        break;
                    case LOG_ARRAY:
                        if (LOG_TEXT.equals(name)) {
                            actLog.Comment = value;
                        } else if (USER_NAME.equals(name)) {
                            actLog.Finder = value;
                        } else if (VISIT_DATE.equals(name)) {
                            actLog.Timestamp = getDateFromLongString(value);
                        }
                        break;
                    case WAY_POINT_ARRAY:
                        if (CODE.equals(name)) {
                            actWayPoint.setGcCode(value);
                        } else if (DESC.equals(name)) {
                            actWayPoint.setTitle(value);
                        } else if (COMMENT.equals(name)) {
                            actWayPoint.setDescription(value);
                        }
                }


            }

            @Override
            public void number(String name, double value, String stringValue) {
                if (iCancel != null && iCancel.cancel()) this.cancel();
                super.number(name, value, stringValue);

                switch (SWITCH) {
                    case CACHE_ARRAY:
                        if (DIFFICULTY.equals(name)) {
                            actCache.setDifficulty((float) value);
                        } else if (TERRAIN.equals(name)) {
                            actCache.setTerrain((float) value);
                        } else if (LAT.equals(name)) {
                            actLat = value;
                        } else if (LON.equals(name)) {
                            actLon = value;
                        }
                        break;
                    case ATTRIBUTE_ARRAY:
                        break;
                    case LOG_ARRAY:
                        break;
                    case WAY_POINT_ARRAY:
                        if (LAT.equals(name)) {
                            actWpLat = value;
                        } else if (LON.equals(name)) {
                            actWpLon = value;
                        }
                        break;
                }
            }

            @Override
            public void number(String name, long value, String stringValue) {
                if (iCancel != null && iCancel.cancel()) this.cancel();
                super.number(name, value, stringValue);

                switch (SWITCH) {
                    case CACHE_ARRAY:
                        if (CACHE_TYPE_ID.equals(name)) {
                            actCache.Type = getCacheType((int) value);
                        } else if (DIFFICULTY.equals(name)) {
                            actCache.setDifficulty((float) value);
                        } else if (TERRAIN.equals(name)) {
                            actCache.setTerrain((float) value);
                        } else if (FAVRITE_POINTS.equals(name)) {
                            actCache.setFavoritePoints((int) value);
                        } else if (ID.equals(name)) {
                            actCache.setGcId(Long.toString(value));
                        }
                        break;
                    case ATTRIBUTE_ARRAY:
                        if (ATTRIBUTE_ID.equals(name)) {
                            attributeID = (int) value;
                        }

                        break;
                    case LOG_ARRAY:
                        if (ID.equals(name)) {
                            actLog.Id = value;
                        } else if (LOG_TYPE_ID.equals(name)) {
                            actLog.Type = LogTypes.GC2CB_LogType((int) value);
                        }
                        break;
                    case WAY_POINT_ARRAY:
                        if (WAYPOINT_TYPE_ID.equals(name)) {
                            actWayPoint.Type = getCacheType((int) value);
                        }
                }


            }

            @Override
            public void bool(String name, boolean value) {
                if (iCancel != null && iCancel.cancel()) this.cancel();
                super.bool(name, value);

                switch (SWITCH) {
                    case CACHE_ARRAY:
                        if (FOUND.equals(name)) {
                            actCache.setFound(value);
                        }
                        break;
                    case ATTRIBUTE_ARRAY:
                        if (IS_ON.equals(name)) {
                            isOn = value;
                        }
                        break;
                    case LOG_ARRAY:

                        break;
                }


            }

        };
        parser.parse(stream, length);
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                log.debug("Ready parse Json! Wait for Async DB writer");
                asyncExecutor.dispose();
                log.debug("Async DB writer is ready! Callback!");
                readyCallBack.callBack(ApiResultState.IO);
            }
        });
        thread.start();
    }

    /**
     * see: https://api.groundspeak.com/LiveV6/geocaching.svc/help/operations/SearchForGeocaches
     *
     * @param json
     */
    protected void getRequest(Json json) {
        json.writeValue("AccessToken", this.gcApiKey);
        json.writeValue("MaxPerPage", this.number);
        json.writeValue("StartIndex", 0);
        json.writeValue("IsLite", this.isLite);
        json.writeValue("TrackableLogCount", trackableLogCount);
        json.writeValue("GeocacheLogCount", geocacheLogCount);

        if (this.available) {
            json.writeObjectStart("GeocacheExclusions");
            json.writeValue("Archived", false);
            json.writeValue("Available", true);
            json.writeObjectEnd();
        }
        if (this.excludeHides) {
            json.writeObjectStart("NotHiddenByUsers");
            json.writeArrayStart("UserNames");
            json.writeValue(Config.GcLogin.getValue());
            json.writeArrayEnd();
            json.writeObjectEnd();
        }

        if (this.excludeFounds) {
            json.writeObjectStart("NotFoundByUsers");
            json.writeArrayStart("UserNames");
            json.writeValue(Config.GcLogin.getValue());
            json.writeArrayEnd();
            json.writeObjectEnd();
        }
    }

    public void setIsLite(boolean isLite) {
        this.isLite = isLite;
    }


    public void postRequest(GenericCallBack<ApiResultState> callBack, long gpxFilenameId) {
        this.gpxFilenameId = gpxFilenameId;
        this.post(callBack);
    }


    static int getCacheSize(int containerTypeId) {
        switch (containerTypeId) {
            case 1:
                return 0; // Unknown
            case 2:
                return 1; // Micro
            case 3:
                return 3; // Regular
            case 4:
                return 4; // Large
            case 5:
                return 5; // Virtual
            case 6:
                return 0; // Other
            case 8:
                return 2;
            default:
                return 0;

        }
    }

    static CacheTypes getCacheType(int apiTyp) {
        switch (apiTyp) {
            case 2:
                return CacheTypes.Traditional;
            case 3:
                return CacheTypes.Multi;
            case 4:
                return CacheTypes.Virtual;
            case 5:
                return CacheTypes.Letterbox;
            case 6:
                return CacheTypes.Event;
            case 8:
                return CacheTypes.Mystery;
            case 9:
                return CacheTypes.Cache; // Project APE Cache???
            case 11:
                return CacheTypes.Camera;
            case 12:
                return CacheTypes.Cache; // Locationless (Reverse) Cache
            case 13:
                return CacheTypes.CITO; // Cache In Trash Out Event
            case 137:
                return CacheTypes.Earth;
            case 453:
                return CacheTypes.MegaEvent;
            case 452:
                return CacheTypes.ReferencePoint;
            case 1304:
                return CacheTypes.Cache; // GPS Adventures Exhibit
            case 1858:
                return CacheTypes.Wherigo;

            case 217:
                return CacheTypes.ParkingArea;
            case 220:
                return CacheTypes.Final;
            case 219:
                return CacheTypes.MultiStage;
            case 221:
                return CacheTypes.Trailhead;
            case 218:
                return CacheTypes.MultiQuestion;
            case 7005:
                return CacheTypes.Giga;

            default:
                return CacheTypes.Undefined;
        }
    }

    private static final String DATE_START = "Date(";

    private synchronized Date getDateFromLongString(String value) {
        Date date = new Date();
        try {
            int date1 = value.indexOf(DATE_START);
            int date2 = value.indexOf("-");
            String dateString = value.substring(date1 + DATE_START.length(), date2);
            if (dateString.startsWith("\"")) dateString = dateString.substring(1);
            date = new Date(Long.valueOf(dateString));
        } catch (Exception exc) {
            log.error("ParseDate from value:'{}'", value, exc);
        }
        return date;
    }

    protected void writeLogToDB(final LogEntry logEntry) {
        asyncExecutor.submit(new AsyncTask<Void>() {
            @Override
            public Void call() throws Exception {
                logDAO.WriteToDatabase(logEntry);
                return null;
            }
        });
    }

    protected void writeImagEntryToDB(final ImageEntry imageEntry) {
        imageDAO.WriteToDatabase(imageEntry, false);
        //TODO start download ?
    }


    AsyncExecutor asyncExecutor = new AsyncExecutor(20);

    protected void writeCacheToDB(final Cache cache) {

        asyncExecutor.submit(new AsyncTask<Void>() {
            @Override
            public Void call() throws Exception {
                Cache aktCache = Database.Data.Query.GetCacheById(cache.Id);

                if (aktCache != null && aktCache.isLive())
                    aktCache = null;

                if (aktCache == null) {
                    aktCache = cacheDAO.getFromDbByCacheId(cache.Id);
                }
                // Read Detail Info of Cache if not available
                if ((aktCache != null) && (aktCache.detail == null)) {
                    aktCache.loadDetail();
                }
                // If Cache into DB, extract saved rating
                if (aktCache != null) {
                    cache.Rating = aktCache.Rating;
                }

                // Falls das Update nicht klappt (Cache noch nicht in der DB) Insert machen
                if (!cacheDAO.UpdateDatabase(cache)) {
                    cacheDAO.WriteToDatabase(cache);
                }

                // Notes von Groundspeak überprüfen und evtl. in die DB an die vorhandenen Notes anhängen
                if (cache.getTmpNote() != null) {
                    String oldNote = Database.getNote(cache.Id);
                    String newNote = "";
                    if (oldNote == null) {
                        oldNote = "";
                    }
                    String begin = "<Import from Geocaching.com>";
                    String end = "</Import from Geocaching.com>";
                    int iBegin = oldNote.indexOf(begin);
                    int iEnd = oldNote.indexOf(end);
                    if ((iBegin >= 0) && (iEnd > iBegin)) {
                        // Note from Groundspeak already in Database
                        // -> Replace only this part in whole Note
                        newNote = oldNote.substring(0, iBegin - 1) + System.getProperty("line.separator"); // Copy the old part of Note before
                        // the beginning of the groundspeak
                        // block
                        newNote += begin + System.getProperty("line.separator");
                        newNote += cache.getTmpNote();
                        newNote += System.getProperty("line.separator") + end;
                        newNote += oldNote.substring(iEnd + end.length(), oldNote.length());
                    } else {
                        newNote = oldNote + System.getProperty("line.separator");
                        newNote += begin + System.getProperty("line.separator");
                        newNote += cache.getTmpNote();
                        newNote += System.getProperty("line.separator") + end;
                    }
                    cache.setTmpNote(newNote);
                    Database.setNote(cache.Id, cache.getTmpNote());
                }

                // Delete LongDescription from this Cache! LongDescription is Loading by showing DescriptionView direct from DB
                cache.setLongDescription("");


                for (int i = 0, n = cache.waypoints.size; i < n; i++) {
                    // must Cast to Full Waypoint. If Waypoint, is wrong created!
                    Waypoint waypoint = cache.waypoints.get(i);
                    boolean update = true;

                    // don't refresh wp if aktCache.wp is user changed
                    if (aktCache != null) {
                        if (aktCache.waypoints != null) {
                            for (int j = 0, m = aktCache.waypoints.size; j < m; j++) {
                                Waypoint wp = aktCache.waypoints.get(j);
                                if (wp.getGcCode().equalsIgnoreCase(waypoint.getGcCode())) {
                                    if (wp.IsUserWaypoint)
                                        update = false;
                                    break;
                                }
                            }
                        }
                    }

                    if (update) {
                        // do not store replication information when importing caches with GC api
                        if (!waypointDAO.UpdateDatabase(waypoint, false)) {
                            waypointDAO.WriteToDatabase(waypoint, false); // do not store replication information here
                        }
                    }

                }

                if (aktCache == null) {
                    Database.Data.Query.add(cache);
                } else {
                    Database.Data.Query.removeValue(Database.Data.Query.GetCacheById(cache.Id), false);
                    Database.Data.Query.add(cache);
                }
                return null;
            }
        });


    }


//TODO try ASYNC write to DB

//    private void writeLogToDB(final LogEntry logEntry) {
//        CB.postAsync(new Runnable() {
//            @Override
//            public void run() {
//                importedLogs++;
//                logDAO.writeToDatabase(logEntry);
//            }
//        });
//    }
//
//    private void writeImagEntryToDB(final ImageEntry imageEntry) {
//        CB.postAsync(new Runnable() {
//            @Override
//            public void run() {
//                imageDAO.writeToDatabase(imageEntry, false);
//                //TODO start download ?
//            }
//        });
//    }
//
//    private void writeCacheToDB(final Cache cache) {
//
//        CB.postAsync(new Runnable() {
//            @Override
//            public void run() {
//                importedCaches++;
//
//                Cache aktCache = Database.Data.Query.GetCacheById(cache.Id);
//
//                if (aktCache != null && aktCache.isLive())
//                    aktCache = null;
//
//                if (aktCache == null) {
//                    aktCache = cacheDAO.getFromDbByCacheId(cache.Id);
//                }
//                // Read Detail Info of Cache if not available
//                if ((aktCache != null) && (aktCache.detail == null)) {
//                    aktCache.loadDetail();
//                }
//                // If Cache into DB, extract saved rating
//                if (aktCache != null) {
//                    cache.Rating = aktCache.Rating;
//                }
//
//                // Falls das Update nicht klappt (Cache noch nicht in der DB) Insert machen
//                if (!cacheDAO.updateDatabase(cache)) {
//                    cacheDAO.writeToDatabase(cache);
//                }
//
//                // Notes von Groundspeak überprüfen und evtl. in die DB an die vorhandenen Notes anhängen
//                if (cache.getTmpNote() != null) {
//                    String oldNote = Database.getNote(cache.Id);
//                    String newNote = "";
//                    if (oldNote == null) {
//                        oldNote = "";
//                    }
//                    String begin = "<Import from Geocaching.com>";
//                    String end = "</Import from Geocaching.com>";
//                    int iBegin = oldNote.indexOf(begin);
//                    int iEnd = oldNote.indexOf(end);
//                    if ((iBegin >= 0) && (iEnd > iBegin)) {
//                        // Note from Groundspeak already in Database
//                        // -> Replace only this part in whole Note
//                        newNote = oldNote.substring(0, iBegin - 1) + System.getProperty("line.separator"); // Copy the old part of Note before
//                        // the beginning of the groundspeak
//                        // block
//                        newNote += begin + System.getProperty("line.separator");
//                        newNote += cache.getTmpNote();
//                        newNote += System.getProperty("line.separator") + end;
//                        newNote += oldNote.substring(iEnd + end.length(), oldNote.length());
//                    } else {
//                        newNote = oldNote + System.getProperty("line.separator");
//                        newNote += begin + System.getProperty("line.separator");
//                        newNote += cache.getTmpNote();
//                        newNote += System.getProperty("line.separator") + end;
//                    }
//                    cache.setTmpNote(newNote);
//                    Database.setNote(cache.Id, cache.getTmpNote());
//                }
//
//                // Delete LongDescription from this Cache! LongDescription is Loading by showing DescriptionView direct from DB
//                cache.setLongDescription("");
//
//
//                for (int i = 0, n = cache.waypoints.size; i < n; i++) {
//                    // must Cast to Full Waypoint. If Waypoint, is wrong createt!
//                    Waypoint waypoint = cache.waypoints.get(i);
//                    boolean update = true;
//
//                    // dont refresh wp if aktCache.wp is user changed
//                    if (aktCache != null) {
//                        if (aktCache.waypoints != null) {
//                            for (int j = 0, m = aktCache.waypoints.size; j < m; j++) {
//                                Waypoint wp = aktCache.waypoints.get(j);
//                                if (wp.getGcCode().equalsIgnoreCase(waypoint.getGcCode())) {
//                                    if (wp.IsUserWaypoint)
//                                        update = false;
//                                    break;
//                                }
//                            }
//                        }
//                    }
//
//                    if (update) {
//                        // do not store replication information when importing caches with GC api
//                        if (!waypointDAO.updateDatabase(waypoint, false)) {
//                            waypointDAO.writeToDatabase(waypoint, false); // do not store replication information here
//                        }
//                    }
//
//                }
//
//                if (aktCache == null) {
//                    Database.Data.Query.add(cache);
//                } else {
//                    Database.Data.Query.removeValue(Database.Data.Query.GetCacheById(cache.Id), false);
//                    Database.Data.Query.add(cache);
//                }
//            }
//        });
//    }
}