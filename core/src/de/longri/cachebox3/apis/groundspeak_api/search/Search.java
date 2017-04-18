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
import com.badlogic.gdx.utils.JsonReader;
import de.longri.cachebox3.apis.groundspeak_api.PostRequest;
import de.longri.cachebox3.callbacks.GenericCallBack;
import de.longri.cachebox3.settings.Config;
import de.longri.cachebox3.types.*;
import de.longri.cachebox3.utils.lists.CB_List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    int geocacheLogCount = 10;
    int trackableLogCount = 10;
    private boolean isLite;
    private CB_List<Cache> cacheList;
    private CB_List<LogEntry> logList;
    private CB_List<ImageEntry> imageList;
    private long gpxFilenameId;

    /**
     * 0 = unknown, 1 = Basic Member, 2 = Premium Member
     */
    private byte apiState;

    /**
     * @param gcApiKey valid encrypted Api-Key
     * @param number   MaxPerPage size for this request
     * @param apiState 0 = unknown, 1 = Basic Member, 2 = Premium Member
     */
    Search(String gcApiKey, int number, byte apiState) {
        super(gcApiKey);
        this.number = number;
        this.apiState = apiState;
    }

    @Override
    protected String getCallUrl() {
        return "SearchForGeocaches?format=json";
    }

    private double actLat, actLon;
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
    private final String COMMENT = "Comment";
    private final String WAYPOINT_TYPE_ID = "WptTypeID";


    int attributeID;
    boolean isOn;

    private Array<String> arrayStack = new Array<>();
    private Array<String> objectStack = new Array<>();

    private int SWITCH = 0;
    private final int CACHE_ARRAY = 1;
    private final int ATTRIBUTE_ARRAY = 2;
    private final int LOG_ARRAY = 3;
    private final int CACHE_LIMITS_ARRAY = 4;
    private final int IMAGE_ARRAY = 5;
    private final int WAY_POINT_ARRAY = 6;

    @Override
    protected void handleHttpResponse(Net.HttpResponse httpResponse, final GenericCallBack<Integer> readyCallBack) {
        (new JsonReader() {

            @Override
            protected void startArray(String name) {
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
                }
            }

            @Override
            public void endArray(String name) {
                // System.out.println("End array " + name);
                arrayStack.pop();

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
                    }
                } else {
                    SWITCH = 0;
                }


            }

            protected void startObject(String name) {
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

            protected void pop() {
                super.pop();
                String name = objectStack.pop();
                // System.out.println("pop " + name);


                switch (SWITCH) {
                    case CACHE_ARRAY:
                        if (NEW_CACHE.equals(name)) {
                            //store cache
                            actCache.setApiState(apiState);

                            //add final Cache instance
                            cacheList.add(new Cache(actLat, actLon, actCache));
                            actCache = null;
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
                            logList.add(actLog);
                            actLog = null;
                        }
                        break;
                    case WAY_POINT_ARRAY:
                        if (NEW_WAY_POINT.equals(name)) {
                            // System.out.println("add Waypoiint ");
                            actWayPoint.CacheId = actCache.Id;
                            //add final Waypointg instance
                            actCache.waypoints.add(new Waypoint(actLat, actLon, actWayPoint));
                            actWayPoint = null;
                        }
                        break;
                }
            }

            protected void string(String name, String value) {
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

            protected void number(String name, double value, String stringValue) {
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
                            actLat = value;
                        } else if (LON.equals(name)) {
                            actLon = value;
                        }
                        break;
                }
            }

            protected void number(String name, long value, String stringValue) {
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

            protected void bool(String name, boolean value) {
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

        }).parse(httpResponse.getResultAsStream());


        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                readyCallBack.callBack(NO_ERROR);
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
        json.writeValue("TrackableLogCount", this.trackableLogCount);
        json.writeValue("GeocacheLogCount", this.geocacheLogCount);

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


    public void postRequest(GenericCallBack<Integer> callBack, CB_List<Cache> cacheList, CB_List<LogEntry> logList,
                            CB_List<ImageEntry> imageList, long gpxFilenameId) {
        setLists(cacheList, logList, imageList, gpxFilenameId);
        this.post(callBack);
    }

    public void setLists(CB_List<Cache> cacheList, CB_List<LogEntry> logList, CB_List<ImageEntry> imageList, long gpxFilenameId) {
        this.cacheList = cacheList;
        this.logList = logList;
        this.imageList = imageList;
        this.gpxFilenameId = gpxFilenameId;
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

    static Date getDateFromLongString(String value) {
        Date date = new Date();
        try {
            int date1 = value.indexOf("/Date(");
            int date2 = value.indexOf("-");
            String dateString = (String) value.subSequence(date1 + 6, date2);
            date = new Date(Long.valueOf(dateString));
        } catch (Exception exc) {
            log.error("ParseDate", exc);
        }
        return date;
    }
}