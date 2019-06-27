/*
 * Copyright (C) 2014 team-cachebox.de
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
package de.longri.cachebox3.apis;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import de.longri.cachebox3.locator.Coordinate;
import de.longri.cachebox3.settings.Config;
import de.longri.cachebox3.sqlite.Import.DescriptionImageGrabber;
import de.longri.cachebox3.translation.Translation;
import de.longri.cachebox3.types.*;
import de.longri.cachebox3.utils.ICancel;
import de.longri.cachebox3.utils.http.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import static de.longri.cachebox3.sqlite.Import.DescriptionImageGrabber.Segmentize;
import static de.longri.cachebox3.types.MutableCache.IS_FULL;
import static de.longri.cachebox3.types.MutableCache.IS_LITE;
import static java.lang.Thread.sleep;

public class GroundspeakAPI {
    public static final int OK = 0;
    public static final int ERROR = -1;
    static final Logger log = LoggerFactory.getLogger(GroundspeakAPI.class);
    public static String LastAPIError = "";
    public static int APIError;

    private static UserInfos me;
    private static Webb netz;
    private static long startTs;
    private static long lastTimeLimitFetched;
    private static int nrOfApiCalls;
    private static int retryCount;
    private static boolean active = false;

    // with API from 15. april 2019 :
    // Geocache daily limit per user is now tracked by unique geocache codes (e.g. the same call to GetGeocache on GCK25B will count as one)
    private static Webb getNetz() {
        if (netz == null) {
            netz = Webb.create();
            netz.setDefaultHeader(Webb.HDR_AUTHORIZATION, "bearer " + GetSettingsAccessToken());
            Webb.setTimeout(Config.socket_timeout.getValue());
            startTs = System.currentTimeMillis();
            nrOfApiCalls = 0;
            retryCount = 0;
            active = false;
        }

        if (System.currentTimeMillis() - startTs > 60000) {
            // reset nrOfApiCalls after one minute
            // perhaps can avoid retry for 429 by checking nrOfApiCalls. ( not implemented yet )
            startTs = System.currentTimeMillis();
            nrOfApiCalls = 0;
            retryCount = 0;
        }

        nrOfApiCalls++;
        APIError = 0;
        return netz;
    }

    public static void setAuthorization() {
        getNetz().setDefaultHeader(Webb.HDR_AUTHORIZATION, "bearer " + GetSettingsAccessToken());
        me = null;
    }

    // API 1.0 see https://api.groundspeak.com/documentation and https://api.groundspeak.com/api-docs/index

    private static boolean retry(Exception ex) {
        // Alternate: implement own RetryManager for 429
        if (ex instanceof WebbException) {
            WebbException we = (WebbException) ex;
            Response re = we.getResponse();
            if (re != null) {
                JSONObject ej;
                APIError = re.getStatusCode();
                if (APIError == 429) {
                    // 429 is only for nr of calls per minute
                    if (retryCount == 0) {
                        log.debug("API-Limit exceeded: " + nrOfApiCalls + " Number of Calls within " + ((System.currentTimeMillis() - startTs) / 1000) + " seconds.");
                        // Difference 61000 is one second more than one minute. (60000 = one minute gives still 429 Exception)
                        try {
                            long ta = 61000 - (System.currentTimeMillis() - startTs);
                            if (ta > 0)
                                sleep(ta);
                        } catch (InterruptedException ignored) {
                            LastAPIError = "Aborted by user";
                        }
                        startTs = System.currentTimeMillis();
                        nrOfApiCalls = 0;
                        retryCount++; //important hint: on successful execution (no Exception), retryCount must be reset to 0 else there is no retry for the next failure.
                    } else {
                        startTs = System.currentTimeMillis();
                        nrOfApiCalls = 0;
                        retryCount = 0;
                        LastAPIError = "******* Aborting: After retry API-Limit is still exceeded.";
                    }
                } else {
                    // 401 = Not Authorized
                    // 403 = limit exceeded: want to get more caches than remain (lite / full) : get limits for good message
                    // 404 = Not Found
                    try {
                        ej = new JSONObject(new JSONTokener((String) re.getErrorBody()));
                        if (ej != null) {
                            LastAPIError = ej.optString("errorMessage", "" + APIError);
                        } else {
                            LastAPIError = ex.getLocalizedMessage();
                        }
                    } catch (Exception exc) {
                        LastAPIError = ex.getLocalizedMessage();
                        log.error(APIError + ":" + LastAPIError);
                    }
                }
            } else {
                // re == null
                APIError = ERROR;
                LastAPIError = ex.getLocalizedMessage();
                log.error(APIError + ":" + LastAPIError);
            }
        } else {
            // no WebbException
            APIError = ERROR;
            LastAPIError = ex.getLocalizedMessage();
            log.error(APIError + ":" + LastAPIError, ex);
        }
        return retryCount > 0;
    }

    public static Array<GeoCacheRelated> searchGeoCaches(Query query) {
        // fetch/update geocaches consumes a lite or full cache
        Array<GeoCacheRelated> fetchResults = new Array<>();
        log.debug("searchGeoCaches start " + query.toString());
        try {

            Array<String> fields = query.getFields();
            boolean onlyLiteFields = query.containsOnlyLiteFields(fields);
            int maxCachesPerHttpCall = (onlyLiteFields ? 50 : 5); // API 1.0 says may take 50, but not in what time, and with 10 I got out of memory
            if (query.descriptor == null) {
                if (onlyLiteFields) {
                    fetchMyCacheLimits();
                    if (me.remainingLite < me.remaining) {
                        onlyLiteFields = false;
                    }
                }
            } else {
                // for Live on map
                maxCachesPerHttpCall = 50;
            }
            int skip = 0;
            int take = Math.min(query.maxToFetch, maxCachesPerHttpCall);

            do {
                boolean doRetry;
                do {
                    doRetry = false;
                    try {
                        if (query.maxToFetch < skip + take)
                            take = query.maxToFetch - skip;
                        Response<JSONArray> r = query.putQuery(getNetz()
                                .get(getUrl(1, "geocaches/search"))
                                .param("skip", skip)
                                .param("take", take)
                                .param("lite", onlyLiteFields)
                                .ensureSuccess()
                        ).asJsonArray();

                        retryCount = 0;

                        JSONArray fetchedCaches = r.getBody();

                        if (query.descriptor != null) {
                            writeSearchResultsToDisc(fetchedCaches, query.descriptor);
                        }
                        fetchResults.addAll(getGeoCacheRelateds(fetchedCaches, fields, null));

                        if (fetchedCaches.length() < take || take < maxCachesPerHttpCall) {
                            take = 0; // we got all
                        } else {
                            skip = skip + take;
                        }

                    } catch (Exception ex) {
                        doRetry = retry(ex);
                        if (!doRetry) {
                            log.debug("searchGeoCaches with exception: " + LastAPIError);
                            fetchMyCacheLimits();
                            return fetchResults;
                        }
                    }
                }
                while (doRetry);
            } while (take > 0 && skip < query.maxToFetch);

        } catch (Exception e) {
            APIError = ERROR;
            LastAPIError = e.getLocalizedMessage();
            log.error("searchGeoCaches", e);
            return fetchResults;
        }
        log.debug("searchGeoCaches ready with " + fetchResults.size + " Caches.");
        fetchMyCacheLimits();
        return fetchResults;
    }

    private static void writeSearchResultsToDisc(JSONArray fetchedCaches, Descriptor descriptor) {
        /* todo
        Writer writer = null;
        try {
            String Path = descriptor.getLocalCachePath(LiveMapQue.LIVE_CACHE_NAME) + LiveMapQue.LIVE_CACHE_EXTENSION;
            if (FileIO.createDirectory(Path)) {
                writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(Path), "utf-8"));
                writer.write(fetchedCaches.toString());
            }
        } catch (IOException ex) {
            // report
        } finally {
            try {
                writer.close();
            } catch (Exception ex) {
            }
        }
        */
    }

    public static Array<GeoCacheRelated> updateStatusOfGeoCaches(Array<AbstractCache> caches) {
        // fetch/update geocaches consumes a lite or full cache
        Query query = new Query().resultForStatusFields().setMaxToFetch(caches.size);
        return updateGeoCaches(query, caches);
    }

    public static Array<GeoCacheRelated> updateGeoCache(AbstractCache cache) {
        Array<AbstractCache> caches = new Array<>();
        caches.add(cache);
        // not .onlyActiveGeoCaches() : must be updated to the latest status
        Query query = new Query()
                .resultWithFullFields()
                .resultWithLogs(30)
                //.resultWithImages(30) // todo maybe remove, cause not used from DB
                ;
        return updateGeoCaches(query, caches);
    }

    public static Array<GeoCacheRelated> fetchGeoCache(Query query, String GcCode) {
        AbstractCache cache = new MutableCache(0, 0);
        cache.setGcCode(GcCode);
        Array<AbstractCache> caches = new Array<>();
        caches.add(cache);
        return updateGeoCaches(query, caches);
    }

    public static Array<GeoCacheRelated> fetchGeoCaches(Query query, String CacheCodes) {
        Array<AbstractCache> caches = new Array<>();
        for (String GcCode : CacheCodes.split(",")) {
            MutableCache cache = new MutableCache(0, 0);
            cache.setGcCode(GcCode);
            caches.add(cache);
        }
        return updateGeoCaches(query, caches);
    }

    public static Array<GeoCacheRelated> updateGeoCaches(Query query, Array<AbstractCache> caches) {
        // fetch/update geocaches consumes a lite or full cache
        Array<GeoCacheRelated> fetchResults = new Array<>();
        try {

            Array<String> fields = query.getFields();
            boolean onlyLiteFields = query.containsOnlyLiteFields(fields);
            int maxCachesPerHttpCall = (onlyLiteFields ? 50 : 5); // API 1.0 says may take 50, but not in what time, and with 10 Full I got out of memory
            if (onlyLiteFields) {
                fetchMyCacheLimits();
                if (me.remainingLite < me.remaining) {
                    onlyLiteFields = false;
                }
            }

            int skip = 0;
            int take = Math.min(query.maxToFetch, maxCachesPerHttpCall);

            do {
                // preparing the next block of max 50 caches to update
                ObjectMap<String, AbstractCache> mapOfCaches = new ObjectMap<>();
                StringBuilder CacheCodes = new StringBuilder();
                int took = 0;
                for (int i = skip; i < Math.min(skip + take, caches.size); i++) {
                    AbstractCache cache = caches.get(i);
                    if (cache.getGcCode().toString().toLowerCase().startsWith("gc")) {
                        mapOfCaches.put(cache.getGcCode().toString(), cache);
                        CacheCodes.append(",").append(cache.getGcCode());
                        took++;
                    }
                }
                if (took == 0) return fetchResults; // no gc in the block
                skip = skip + take;

                boolean doRetry;
                do {
                    doRetry = false;
                    try {
                        Response<JSONArray> r = query.putQuery(getNetz()
                                .get(getUrl(1, "geocaches"))
                                .param("referenceCodes", CacheCodes.substring(1))
                                .param("lite", onlyLiteFields)
                                .ensureSuccess())
                                .asJsonArray();

                        retryCount = 0;

                        fetchResults.addAll(getGeoCacheRelateds(r.getBody(), fields, mapOfCaches));

                    } catch (Exception ex) {
                        doRetry = retry(ex);
                        if (!doRetry) {
                            if (APIError == 404) {
                                // one bad GCCode (not starting with GC) causes Error 404: will hopefully be changed in an update after 11.26.2018
                                // a not existing GCCode seems to be ignored, what is ok
                                doRetry = false;
                                log.error("searchGeoCaches - skipped block cause: " + LastAPIError);
                            } else {
                                fetchMyCacheLimits();
                                return fetchResults;
                            }
                        }
                    }
                }
                while (doRetry);
            } while (skip < caches.size);

        } catch (Exception e) {
            APIError = ERROR;
            LastAPIError = e.getLocalizedMessage();
            log.error("updateGeoCaches", e);
            return fetchResults;
        }
        fetchMyCacheLimits();
        return fetchResults;
    }

    public static Array<GeoCacheRelated> getGeoCacheRelateds(JSONArray fetchedCaches, Array<String> fields, ObjectMap<String, AbstractCache> mapOfCaches) {
        Array<GeoCacheRelated> fetchResults = new Array<>();
        for (int ii = 0; ii < fetchedCaches.length(); ii++) {
            JSONObject fetchedCache = (JSONObject) fetchedCaches.get(ii);
            AbstractCache originalCache;
            if (mapOfCaches == null) {
                originalCache = null;
            } else {
                originalCache = mapOfCaches.get(fetchedCache.optString("referenceCode"));
            }
            AbstractCache cache = createGeoCache(fetchedCache, fields, originalCache);
            if (cache != null) {
                Array<LogEntry> logs = createLogs(cache, fetchedCache.optJSONArray("geocacheLogs"));
                Array<ImageEntry> images = createImageList(fetchedCache.optJSONArray("images"), cache.getGcCode().toString(), false);
                images = addDescriptionImageList(images, cache);
                fetchResults.add(new GeoCacheRelated(cache, logs, images));
            }
        }
        return fetchResults;
    }

    public static Array<PQ> fetchPocketQueryList() {

        Array<PQ> pqList = new Array<>();

        try {

            int skip = 0;
            int take = 50;
            String fields = "referenceCode,name,lastUpdatedDateUtc,count";

            do {
                boolean doRetry;
                do {
                    doRetry = false;
                    try {
                        Response<JSONArray> r = getNetz()
                                .get(getUrl(1, "users/me/lists"))
                                .param("types", "pq")
                                .param("fields", fields)
                                .param("skip", skip)
                                .param("take", take)
                                .ensureSuccess()
                                .asJsonArray();

                        retryCount = 0;
                        skip = skip + take;

                        JSONArray response = r.getBody();

                        for (int ii = 0; ii < response.length(); ii++) {
                            JSONObject jPQ = (JSONObject) response.get(ii);
                            PQ pq = new PQ();
                            pq.GUID = jPQ.optString("referenceCode", "");
                            if (pq.GUID.length() > 0) {
                                pq.name = jPQ.optString("name", "");
                                try {
                                    String dateCreated = jPQ.optString("lastUpdatedDateUtc", "");
                                    pq.lastGenerated = DateFromString(dateCreated);
                                } catch (Exception exc) {
                                    log.error("fetchPocketQueryList/lastGenerated", exc);
                                    pq.lastGenerated = new Date();
                                }
                                pq.cacheCount = jPQ.getInt("count");
                                pq.sizeMB = -1;
                                pqList.add(pq);
                            }
                        }

                        if (response.length() < take) {
                            APIError = OK;
                            return pqList;
                        }
                    } catch (Exception ex) {
                        doRetry = retry(ex);
                        if (!doRetry) {
                            // APIError from retry
                            return pqList;
                        }
                    }
                }
                while (doRetry);
            } while (true);

        } catch (Exception e) {
            APIError = ERROR;
            LastAPIError = e.getLocalizedMessage();
            log.error("fetchPocketQueryList", e);
            return pqList;
        }
    }

    public static void fetchPocketQuery(PQ pocketQuery, String pqFolder) {
        InputStream inStream = null;
        BufferedOutputStream outStream = null;
        try {
            inStream = getNetz()
                    .get(getUrl(1, "lists/" + pocketQuery.GUID + "/geocaches/zipped"))
                    .ensureSuccess()
                    .asStream()
                    .getBody();
            // String dateString = new SimpleDateFormat("yyyyMMddHHmmss").format(pocketQuery.lastGenerated);
            String local = pqFolder + "/" + pocketQuery.GUID + ".zip";
            // FileOutputStream localFile = new FileOutputStream(local);
            OutputStream localFile = Gdx.files.absolute(local).write(false);
            outStream = new BufferedOutputStream(localFile);
            WebbUtils.copyStream(inStream, outStream);
            APIError = OK;
        } catch (Exception e) {
            log.error("fetchPocketQuery", e);
            APIError = ERROR;
            LastAPIError = e.getLocalizedMessage();
        } finally {
            try {
                if (outStream != null)
                    outStream.close();
                if (inStream != null)
                    inStream.close();
            } catch (Exception ignored) {
            }
        }
    }

    public static int UploadDraftOrLog(String cacheCode, int wptLogTypeId, Date dateLogged, String note, boolean directLog) {
        log.info("UploadDraftOrLog");

        if (isAccessTokenInvalid()) return ERROR; // should be checked in advance

        try {
            if (directLog) {
                if (note.length() == 0) {
                    LastAPIError = Translation.get("emptyLog").toString();
                    return ERROR;
                }
                log.debug("is Log");
                getNetz()
                        .post(getUrl(1, "geocachelogs"))
                        .body(new JSONObject()
                                .put("geocacheCode", cacheCode)
                                .put("type", wptLogTypeId)
                                .put("loggedDate", getUTCDate(dateLogged))
                                .put("text", prepareNote(note))
                        )
                        .ensureSuccess()
                        .asVoid();
            } else {
                log.debug("is draft");
                getNetz()
                        .post(getUrl(1, "logdrafts"))
                        .body(new JSONObject()
                                .put("geocacheCode", cacheCode)
                                .put("logType", wptLogTypeId)
                                .put("loggedDateUtc", getUTCDate(dateLogged))
                                .put("note", prepareNote(note))
                        )
                        .ensureSuccess()
                        .asVoid();
            }
            LastAPIError = "";
            log.info("UploadDraftOrLog done");
            return OK;
        } catch (Exception e) {
            retry(e);
            log.error("UploadDraftOrLog geocacheCode: " + cacheCode + " logType: " + wptLogTypeId + ".\n" + LastAPIError, e);
            return ERROR;
        }
    }

    public static Array<LogEntry> fetchGeoCacheLogs(AbstractCache cache, boolean all, ICancel iCancel) {
        Array<LogEntry> logList = new Array<>();

        Array<String> friendList = new Array<>();
        if (!all) {
            String friends = Config.Friends.getValue().replace(", ", "|").replace(",", "|");
            for (String f : friends.split("\\|")) {
                friendList.add(f.toLowerCase(Locale.US));
            }
        }

        int start = 0;
        int count = 50;

        while (iCancel == null || !iCancel.cancel())
        // Schleife, solange bis entweder keine Logs mehr geladen werden oder bis Logs aller Freunde geladen sind.
        {
            boolean doRetry;
            do {
                doRetry = false;
                try {
                    JSONArray geocacheLogs = getNetz()
                            .get(getUrl(1, "geocaches/" + cache.getGcCode() + "/geocachelogs"))
                            .param("fields", "owner.username,loggedDate,text,type,referenceCode")
                            .param("skip", start)
                            .param("take", count)
                            .ensureSuccess()
                            .asJsonArray()
                            .getBody();

                    retryCount = 0;

                    for (int ii = 0; ii < geocacheLogs.length(); ii++) {
                        JSONObject geocacheLog = (JSONObject) geocacheLogs.get(ii);
                        if (!all) {
                            String finder = getStringValue(geocacheLog, "owner", "username");
                            if (finder.length() == 0 || !friendList.contains(finder.toLowerCase(Locale.US), false)) {
                                continue;
                            }
                            friendList.removeValue(finder.toLowerCase(Locale.US), false);
                        }

                        logList.add(createLog(geocacheLog, cache));
                    }

                    // all logs loaded or all friends found
                    if ((geocacheLogs.length() < count) || (!all && (friendList.size == 0))) {
                        APIError = OK;
                        return logList;
                    }

                } catch (Exception e) {
                    doRetry = retry(e);
                    if (!doRetry) {
                        return logList;
                    }
                    log.error("fetchGeoCacheLogs", e);
                }
            }
            while (doRetry);
            // die nächsten Logs laden
            start += count;
        }
        APIError = ERROR;
        LastAPIError = "Loading Logs canceled";
        return (logList);
    }

    public static Array<ImageEntry> downloadImageListForGeocache(String cacheCode, boolean withLogImages) {

        Array<ImageEntry> imageEntries = new Array<>();
        LastAPIError = "";

        if (cacheCode == null || isAccessTokenInvalid()) {
            APIError = ERROR;
            return imageEntries;
        }

        int skip = 0;
        int take = 50;

        // todo implement loop for more than 50 imagelinks (if it ever will be necessary)
        do {
            try {
                Response<JSONArray> r = getNetz()
                        .get(getUrl(1, "geocaches/" + cacheCode + "/images"))
                        .param("fields", "url,description,referenceCode")
                        .param("skip", skip)
                        .param("take", take)
                        .ensureSuccess()
                        .asJsonArray();

                retryCount = 0;
                // is only, if implemented fetch of more than 50 images (loop)
                imageEntries.addAll(createImageList(r.getBody(), cacheCode, withLogImages));

                return imageEntries;

            } catch (Exception ex) {
                if (!retry(ex)) {
                    return imageEntries;
                }
            }
        } while (true);
    }

    public static Array<Trackable> downloadUsersTrackables() {
        Array<Trackable> tbList = new Array<>();
        if (isAccessTokenInvalid()) return tbList;
        LastAPIError = "";
        int skip = 0;
        int take = 50;

        try {
            boolean ready = false;
            do {
                JSONArray jTrackables = getNetz()
                        .get(getUrl(1, "trackables"))
                        .param("fields", "referenceCode,trackingNumber,iconUrl,name,goal,description,releasedDate,owner.username,holder.username,currentGeocacheCode,type,inHolderCollection")
                        .param("skip", skip)
                        .param("take", take)
                        .ensureSuccess().asJsonArray().getBody();

                for (int ii = 0; ii < jTrackables.length(); ii++) {
                    JSONObject jTrackable = (JSONObject) jTrackables.get(ii);
                    if (!jTrackable.optBoolean("inHolderCollection", false)) {
                        Trackable tb = createTrackable(jTrackable);
                        log.debug("downloadUsersTrackables: add " + tb.getName());
                        tbList.add(tb);
                    } else {
                        log.debug("downloadUsersTrackables: not in HolderCollection" + jTrackable.optString("name", ""));
                    }
                }

                ready = jTrackables.length() < take;
                skip = skip + take;
            }
            while (!ready);
            log.info("downloadUsersTrackables done \n");
            return tbList;
        } catch (Exception ex) {
            retry(ex);
            log.error("downloadUsersTrackables " + LastAPIError, ex);
            return tbList;
        }
    }

    public static Trackable fetchTrackable(String TBCode) {
        log.info("fetchTrackable for " + TBCode);
        LastAPIError = "";
        APIError = 0;
        if (isAccessTokenInvalid()) return null;
        try {
            Trackable tb = createTrackable(getNetz()
                    .get(getUrl(1, "trackables/" + TBCode))
                    .param("fields", "referenceCode,trackingNumber,iconUrl,name,goal,description,releasedDate,owner.username,holder.username,currentGeocacheCode,type")
                    .ensureSuccess()
                    .asJsonObject()
                    .getBody()
            );

            if (!tb.getTBCode().toLowerCase().equals(TBCode.toLowerCase())) {
                // fetched by TrackingCode, the result for trackingcode is always empty, except for owner
                tb.setTrackingCode(TBCode);
            }
            return tb;
        } catch (Exception ex) {
            if (ex instanceof WebbException) {
                WebbException we = (WebbException) ex;
                APIError = we.getResponse().getStatusCode();
                JSONObject ej = (JSONObject) we.getResponse().getErrorBody();
                LastAPIError = ej.optString("errorMessage", "" + APIError);
            } else {
                LastAPIError = ex.getLocalizedMessage();
            }
            log.error("fetchTrackable \n"
                            + LastAPIError
                            + "\n for " + getUrl(1, "trackables/" + TBCode + "?fields=url,description")
                    , ex);
            return null;
        }
    }

    public static int uploadTrackableLog(Trackable TB, String cacheCode, int LogTypeId, Date dateLogged, String note) {
        return uploadTrackableLog(TB.getTBCode(), TB.getTrackingCode(), cacheCode, LogTypeId, dateLogged, note);
    }

    public static int uploadTrackableLog(String TBCode, String TrackingNummer, String cacheCode, int LogTypeId, Date dateLogged, String note) {
        log.info("uploadTrackableLog");
        if (cacheCode == null) cacheCode = "";
        if (isAccessTokenInvalid()) return ERROR;
        try {
            getNetz()
                    .post(getUrl(1, "trackablelogs"))
                    .body(new JSONObject()
                            .put("trackingNumber", TrackingNummer) // code only found on the trackable itself (only needed for creating a log)
                            .put("trackableCode", TBCode) // identifier of the related trackable, required for creation
                            .put("geocacheCode", cacheCode)
                            .put("loggedDate", getUTCDate(dateLogged))
                            .put("text", prepareNote(note))
                            .put("typeId", LogTypeId) // see Trackable Log Types https://api.groundspeak.com/documentation#trackable-log-types
                    )
                    .ensureSuccess()
                    .asVoid();
            LastAPIError = "";
            return OK;
        } catch (Exception ex) {
            LastAPIError += ex.getLocalizedMessage();
            LastAPIError += "\n for " + getUrl(1, "trackablelogs");
            LastAPIError += "\n APIKey: " + GetSettingsAccessToken();
            LastAPIError += "\n trackingNumber: " + TrackingNummer;
            LastAPIError += "\n trackableCode: " + TBCode;
            LastAPIError += "\n geocacheCode: " + cacheCode;
            LastAPIError += "\n loggedDate: " + getUTCDate(dateLogged);
            LastAPIError += "\n text: " + prepareNote(note);
            LastAPIError += "\n typeId: " + LogTypeId;
            log.error("uploadTrackableLog \n" + LastAPIError, ex);
            return ERROR;
        }
    }

    public static int AddToWatchList(String gcCode) {
        if (!isAccessTokenInvalid()) {
            try {
                getNetz().post(getUrl(1, "lists/" + fetchWatchListCode() + "/geocaches"))
                        .body(new JSONObject().put("referenceCode", gcCode))
                        .ensureSuccess()
                        .asVoid()
                ;
            } catch (Exception ex) {
                retry(ex);
                return ERROR;
            }
            return OK;
        }
        return ERROR;
    }

    public static int RemoveFromWatchList(String gcCode) {
        if (!isAccessTokenInvalid()) {
            try {
                getNetz().delete(getUrl(1, "lists/" + fetchWatchListCode() + "/geocaches/" + gcCode)).ensureSuccess().asVoid();
            } catch (Exception ex) {
                retry(ex);
                return ERROR;
            }
            return OK;
        }
        return ERROR;
    }

    private static String fetchWatchListCode() {
        JSONArray wl = getNetz()
                .get(getUrl(1, "users/me/lists?types=wl&fields=referenceCode"))
                .ensureSuccess()
                .asJsonArray()
                .getBody();
        return ((JSONObject) wl.get(0)).optString("referenceCode", "");
    }

    public static String fetchFriends() {
        if (!isAccessTokenInvalid()) {
            int skip = 0;
            int take = 50;
            try {
                StringBuilder friends = new StringBuilder();
                boolean ready = false;
                do {
                    JSONArray jFriends = getNetz().get(getUrl(1, "friends"))
                            .param("fields", "username")
                            .param("skip", skip)
                            .param("take", take)
                            .ensureSuccess().asJsonArray().getBody();
                    for (int ii = 0; ii < jFriends.length(); ii++) {
                        friends.append(((JSONObject) jFriends.get(ii)).optString("username", "")).append(",");
                    }
                    skip = skip + take;
                    if (jFriends.length() < take) ready = true;
                }
                while (!ready);
                if (friends.length() > 0)
                    return friends.substring(0, friends.length() - 1);
                else
                    return "";
            } catch (Exception ex) {
                retry(ex);
                return "";
            }
        }
        return "";
    }

    public static void uploadCorrectedCoordinates(String GcCode, double lat, double lon) {
        try {
            getNetz().put(getUrl(1, "geocaches/" + GcCode + "/correctedcoordinates"))
                    .body(new JSONObject().put("latitude", lat).put("longitude", lon))
                    .ensureSuccess().asVoid();
        } catch (Exception ex) {
            retry(ex);
        }
    }

    public static int uploadCacheNote(String cacheCode, String notes) {
        log.info("uploadCacheNote for " + cacheCode);
        LastAPIError = "";
        if (cacheCode == null || cacheCode.length() == 0) return ERROR;
        if (!isPremiumMember()) return ERROR;
        try {
            getNetz()
                    .put(getUrl(1, "geocaches/" + cacheCode + "/notes"))
                    .body(new JSONObject().put("note", prepareNote(notes)))
                    .ensureSuccess()
                    .asVoid();
            log.info("uploadCacheNote done");
            return OK;
        } catch (Exception ex) {
            LastAPIError = ex.getLocalizedMessage();
            LastAPIError += "\n for " + getUrl(1, "geocaches/" + cacheCode + "/notes");
            LastAPIError += "\n APIKey: " + GetSettingsAccessToken();
            LastAPIError += "\n geocacheCode: " + cacheCode;
            LastAPIError += "\n note: " + prepareNote(notes) + "\n";
            LastAPIError += ((WebbException) ex).getResponse().getErrorBody().toString();
            log.error("UpdateCacheNote \n" + LastAPIError, ex);
            return ERROR;
        }
    }

    private static String prepareNote(String note) {
        return note.replace("\r", "");
    }

    public static boolean isAccessTokenInvalid() {
        return (fetchMyUserInfos().memberShipType == MemberShipTypes.Unknown);
    }

    public static boolean isPremiumMember() {
        return fetchMyUserInfos().memberShipType == MemberShipTypes.Premium;
    }

    public static boolean isDownloadLimitExceeded() {
        // do'nt want to access Web for this info (GL.postAsync)
        if (me == null) return false;
        return me.remaining <= 0 && me.remainingLite <= 0;
    }

    public static UserInfos fetchMyUserInfos() {
        if (me == null || me.memberShipType == MemberShipTypes.Unknown) {
            log.debug("fetchMyUserInfos called. Must fetch. Active now: " + active);
            do {
                if (active) {
                    // a try to handle quickly following calls (by another thread)
                    int waitedForMillis = 0;
                    do {
                        try {
                            sleep(1000);
                            if (me != null) return me;
                        } catch (InterruptedException ignored) {
                        }
                        waitedForMillis = waitedForMillis + 1;
                    }
                    while (active || waitedForMillis == 60);
                    if (waitedForMillis == 60) {
                        log.debug("avoid endless loop");
                    }
                }
                active = true;
                me = fetchUserInfos("me");
                if (me.memberShipType == MemberShipTypes.Unknown) {
                    me.findCount = -1;
                    // we need a new AccessToken
                    // API_ErrorEventHandlerList.handleApiKeyError(API_ErrorEventHandlerList.API_ERROR.INVALID);
                    log.error("fetchMyUserInfos: Need a new Access Token");
                }
                active = false;
            }
            while (active);
        }
        return me;
    }

    public static void fetchMyCacheLimits() {
        if (System.currentTimeMillis() - lastTimeLimitFetched > 60000) {
            // update one time per minute may be enough
            me = fetchUserInfos("me");
            lastTimeLimitFetched = System.currentTimeMillis();
        }
    }

    public static UserInfos fetchUserInfos(String UserCode) {
        LastAPIError = "";
        APIError = 0;
        UserInfos ui = new UserInfos();
        do {
            try {
                JSONObject response = getNetz()
                        .get(getUrl(1, "users/" + UserCode))
                        .param("fields", "username,membershipLevelId,findCount,geocacheLimits")
                        .ensureSuccess()
                        .asJsonObject()
                        .getBody();
                retryCount = 0;
                ui.username = response.optString("username", "");
                ui.memberShipType = MemberShipTypesFromInt(response.optInt("membershipLevelId", -1));
                ui.findCount = response.optInt("findCount", -1);
                JSONObject geocacheLimits = response.optJSONObject("geocacheLimits");
                if (geocacheLimits != null) {
                    ui.remaining = geocacheLimits.optInt("fullCallsRemaining", -1);
                    ui.remainingLite = geocacheLimits.optInt("liteCallsRemaining", -1);
                    ui.remainingTime = geocacheLimits.optInt("fullCallsSecondsToLive", -1);
                    ui.remainingLiteTime = geocacheLimits.optInt("liteCallsSecondsToLive", -1);
                }
                return ui;
            } catch (Exception ex) {
                if (!retry(ex)) {
                    return ui;
                }
            }
        }
        while (true);
    }

    public static String GetSettingsAccessToken() {
        /* */
        String act;
        if (Config.UseTestUrl.getValue()) {
            act = Config.AccessTokenForTest.getValue();
        } else {
            act = Config.AccessToken.getValue();
        }

        // for ACB we added an additional A in settings
        if ((act.startsWith("A"))) {
            // log.debug("Access Token = " + act.substring(1, act.length()));
            return act.substring(1);
        } else
            log.error("no Access Token");
        return "";
        /* */
    }

    static String getUrl(int version, String command) {
        String ApiUrl = "https://api.groundspeak.com/";
        String StagingApiUrl = "https://staging.api.groundspeak.com/";
        String mPath;
        switch (version) {
            case 0:
                mPath = "LiveV6/geocaching.svc/";
                break;
            case 1:
                mPath = "v1.0/";
                break;
            default:
                mPath = "";
        }
        String url;
        if (Config.UseTestUrl.getValue()) {
            url = StagingApiUrl + mPath;
        } else {
            url = ApiUrl + mPath;
        }
        return url + command;
    }

    private static MemberShipTypes MemberShipTypesFromInt(int value) {
        switch (value) {
            case 1:
                return MemberShipTypes.Basic;
            case 2:
                return MemberShipTypes.Charter;
            case 3:
                return MemberShipTypes.Premium;
            default:
                return MemberShipTypes.Unknown;
        }
    }

    private static Trackable createTrackable(JSONObject API1Trackable) {
        try {
            Trackable tb = new Trackable();
            log.debug(API1Trackable.toString());
            tb.setArchived(false);
            tb.setTBCode(API1Trackable.optString("referenceCode", ""));
            // trackingNumber	string	unique number used to prove discovery of trackable. only returned if user matches the holderCode
            // will not be stored (Why)
            tb.setTrackingCode(API1Trackable.optString("trackingNumber", ""));
            tb.setCurrentGeocacheCode(API1Trackable.optString("currentGeocacheCode", ""));
            if (tb.getCurrentGeocacheCode().equals("null")) tb.setCurrentGeocacheCode("");
            tb.setCurrentGoal(API1Trackable.optString("goal"));
            tb.setCurrentOwnerName(getStringValue(API1Trackable, "holder", "username"));
            String releasedDate = API1Trackable.optString("releasedDate", "");
            try {
                tb.setDateCreated(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(releasedDate));
            } catch (Exception e) {
                tb.setDateCreated(new Date());
            }
            tb.setDescription(API1Trackable.optString("description", ""));
            tb.setIconUrl(API1Trackable.optString("iconUrl", ""));
            if (tb.getIconUrl().startsWith("http:")) {
                tb.setIconUrl("https:" + tb.getIconUrl().substring(5));
            }
            tb.setName(API1Trackable.optString("name", ""));
            tb.setOwnerName(getStringValue(API1Trackable, "owner", "username"));
            tb.setTypeName(API1Trackable.optString("type", ""));
            return tb;
        } catch (Exception e) {
            log.error("createTrackable(JSONObject API1Trackable)", e);
            return null;
        }
    }

    private static AbstractCache createGeoCache(JSONObject API1Cache, Array<String> fields, AbstractCache cache) {
        // see https://api.groundspeak.com/documentation#geocache
        // see https://api.groundspeak.com/documentation#lite-geocache
        if (cache == null) {
            cache = new MutableCache(0, 0);
            cache.setApiState(IS_LITE);
        }
        if (cache.getWaypoints() != null) {
            cache.getWaypoints().clear();
            // no merging of waypoints here
        } else {
            cache.setWaypoints(new Array<>());
        }
        String tmp;
        try {
            for (String field : fields) {
                int withDot = field.indexOf(".");
                String switchValue;
                String subValue; // no need till now
                if (withDot > 0) {
                    switchValue = field.substring(0, withDot);
                    subValue = field.substring(withDot + 1);
                } else {
                    switchValue = field;
                }
                switch (switchValue) {
                    case "referenceCode":
                        cache.setGcCode(API1Cache.optString(field, ""));
                        if (cache.getGcCode().length() == 0) {
                            log.error("Get no GCCode");
                            return null;
                        }
                        cache.setUrl("https://coord.info/" + cache.getGcCode());
                        cache.setId(MutableCache.GenerateCacheId(cache.getGcCode().toString()));
                        break;
                    case "name":
                        cache.setName(API1Cache.optString(switchValue, ""));
                        break;
                    case "difficulty":
                        cache.setDifficulty((float) API1Cache.optDouble(switchValue, 1));
                        break;
                    case "terrain":
                        cache.setTerrain((float) API1Cache.optDouble(switchValue, 1));
                        break;
                    case "favoritePoints":
                        cache.setFavoritePoints(API1Cache.optInt(switchValue, 0));
                        break;
                    case "trackableCount":
                        cache.setNumTravelbugs((short) API1Cache.optInt(switchValue, 0));
                        break;
                    case "placedDate":
                        cache.setDateHidden(DateFromString(API1Cache.optString(switchValue, "")));
                        break;
                    case "geocacheType":
                        // switch subValue
                        cache.setType(CacheTypeFromID(API1Cache.optJSONObject(switchValue).optInt("id", 0)));
                        break;
                    case "geocacheSize":
                        // switch subValue
                        cache.setSize(CacheSizeFromID(API1Cache.optJSONObject(switchValue).optInt("id", 0)));
                        break;
                    case "location":
                        JSONObject location = API1Cache.optJSONObject(switchValue);
                        // switch subValue
                        cache.setCountry(location.optString("country", ""));
                        cache.setState(location.optString("state", ""));
                        break;
                    case "status":
                        String status = API1Cache.optString(switchValue, "");
                        if (status.equals("Archived")) {
                            cache.setArchived(true);
                            cache.setAvailable(false);
                        } else if (status.equals("Disabled")) {
                            cache.setArchived(false);
                            cache.setAvailable(false);
                        } else if (status.equals("Unpublished")) {
                            cache.setArchived(false);
                            cache.setAvailable(false);
                        } else {
                            // Active, Locked
                            cache.setArchived(false);
                            cache.setAvailable(true);
                        }
                        break;
                    case "owner":
                        cache.setOwner(getStringValue(API1Cache, switchValue, "username"));
                        break;
                    case "ownerAlias":
                        cache.setPlacedBy(API1Cache.optString(switchValue, ""));
                        break;
                    case "postedCoordinates":
                        // handled within userdata
                        break;
                    case "userData":
                        JSONObject userData = API1Cache.optJSONObject(switchValue);
                        // switch subValue
                        if (userData != null) {
                            // foundDate
                            cache.setFound(userData.optString("foundDate", "").length() != 0);
                            // correctedCoordinates
                            JSONObject correctedCoordinates = userData.optJSONObject("correctedCoordinates");
                            if (correctedCoordinates != null) {
                                if (Config.UseCorrectedFinal.getValue()) {
                                    JSONObject postedCoordinates = API1Cache.optJSONObject("postedCoordinates");
                                    cache.setLatLon(postedCoordinates.optDouble("latitude", 0), postedCoordinates.optDouble("longitude", 0));
                                    Array<AbstractWaypoint> waypoints = cache.getWaypoints();
                                    waypoints.add(new MutableWaypoint(
                                            "!?" + cache.getGcCode().toString().substring(2),
                                            CacheTypes.Final,
                                            "",
                                            correctedCoordinates.optDouble("latitude", 0),
                                            correctedCoordinates.optDouble("longitude", 0),
                                            cache.getId(),
                                            "",
                                            "Final GSAK Corrected"));
                                } else {
                                    cache.setLatLon(correctedCoordinates.optDouble("latitude", 0), correctedCoordinates.optDouble("longitude", 0));
                                    cache.setHasCorrectedCoordinates(true);
                                }
                            } else {
                                JSONObject postedCoordinates = API1Cache.optJSONObject("postedCoordinates");
                                if (postedCoordinates != null) {
                                    cache.setLatLon(postedCoordinates.optDouble("latitude", 0), postedCoordinates.optDouble("longitude", 0));
                                } else {
                                    cache.setLatLon(0, 0);
                                }
                            }
                            cache.setTmpNote(userData.optString("note", ""));
                        } else {
                            cache.setFound(false);
                            JSONObject postedCoordinates = API1Cache.optJSONObject("postedCoordinates");
                            if (postedCoordinates != null) {
                                cache.setLatLon(postedCoordinates.optDouble("latitude", 0), postedCoordinates.optDouble("longitude", 0));
                            } else {
                                cache.setLatLon(0, 0);
                            }
                            cache.setTmpNote("");
                        }
                        break;
                    case "hints":
                        cache.setHint(API1Cache.optString(switchValue, ""));
                        break;
                    case "attributes":
                        JSONArray attributes = API1Cache.optJSONArray(switchValue);
                        cache.setAttributesPositive(new DLong(0, 0));
                        cache.setAttributesNegative(new DLong(0, 0));
                        if (attributes != null) {
                            for (int j = 0; j < attributes.length(); j++) {
                                JSONObject attribute = attributes.optJSONObject(j);
                                if (attribute != null) {
                                    Attributes att = Attributes.getAttributeEnumByGcComId(attribute.optInt("id", 0));
                                    if (attribute.optBoolean("isOn", false)) {
                                        cache.addAttributePositive(att);
                                    } else {
                                        cache.addAttributeNegative(att);
                                    }
                                }
                            }
                        }
                        break;
                    case "longDescription":
                        tmp = API1Cache.optString(switchValue, "");
                        if (tmp.length() > 0) {
                            // containsHtml lieferte in meinen Beispielen immer false, scheint aber nun ok
                            if (!tmp.contains("<")) {
                                tmp = tmp.replaceAll("(\r\n|\n\r|\r|\n)", "<br />");
                            }
                            cache.setLongDescription(tmp);
                            cache.setApiState(IS_FULL);
                        }
                        break;
                    case "shortDescription":
                        tmp = API1Cache.optString(switchValue, "");
                        if (tmp.length() > 0) {
                            // containsHtml lieferte in meinen Beispielen immer false, scheint aber nun ok
                            if (!tmp.contains("<")) {
                                tmp = tmp.replaceAll("(\r\n|\n\r|\r|\n)", "<br />");
                            }
                            cache.setShortDescription(tmp);
                            cache.setApiState(IS_FULL); // got a cache without LongDescription
                        }
                        break;
                    case "additionalWaypoints":
                        addWayPoints(cache, API1Cache.optJSONArray(switchValue));
                        break;
                    case "userWaypoints":
                        addUserWayPoints(cache, API1Cache.optJSONArray(switchValue));
                        break;
                    default:
                        // Remind the programmer
                        log.error("createGeoCache: " + switchValue + " not handled");
                }
            }

            return cache;
        } catch (Exception e) {
            log.error("createGeoCache(JSONObject API1Cache)", e);
            return null;
        }
    }

    private static void addWayPoints(AbstractCache cache, JSONArray wpts) {
        if (wpts != null) {
            for (int j = 0; j < wpts.length(); j++) {
                JSONObject wpt = wpts.optJSONObject(j);
                double lat = 0;
                double lon = 0;
                JSONObject coordinates = wpt.optJSONObject("coordinates");
                if (coordinates != null) {
                    lat = coordinates.optDouble("latitude", 0);
                    lon = coordinates.optDouble("longitude", 0);
                }
                MutableWaypoint waypoint = new MutableWaypoint(lat, lon, cache.getId());
                waypoint.setTitle(wpt.optString("name", ""));
                waypoint.setDescription(wpt.optString("description", ""));
                waypoint.setType(CacheTypeFromID(wpt.optInt("typeId", 0)));
                waypoint.setGcCode(wpt.optString("prefix", "XX") + cache.getGcCode().toString().substring(2));
                cache.getWaypoints().add(waypoint);
            }
        }
    }

    private static void addUserWayPoints(AbstractCache cache, JSONArray wpts) {
        if (wpts != null) {
            for (int j = 0; j < wpts.length(); j++) {
                JSONObject wpt = wpts.optJSONObject(j);
                boolean CoordinateOverride = wpt.optString("description", "").equals("Coordinate Override");
                boolean isCorrectedCoordinates = wpt.optBoolean("isCorrectedCoordinates", false);
                if (CoordinateOverride || isCorrectedCoordinates) {
                    double lat = 0;
                    double lon = 0;
                    JSONObject coordinates = wpt.optJSONObject("coordinates");
                    if (coordinates != null) {
                        lat = coordinates.optDouble("latitude", 0);
                        lon = coordinates.optDouble("longitude", 0);
                    }
                    MutableWaypoint waypoint = new MutableWaypoint(lat, lon, cache.getId());
                    waypoint.setTitle("Corrected Coordinates (API)");
                    waypoint.setDescription(wpt.optString("description", ""));
                    waypoint.setType(CacheTypes.Final);
                    waypoint.setGcCode("CO" + cache.getGcCode().toString().substring(2));
                    cache.getWaypoints().add(waypoint);
                }
            }
        }
    }

    private static Array<LogEntry> createLogs(AbstractCache cache, JSONArray geocacheLogs) {
        Array<LogEntry> logList = new Array<>();
        if (geocacheLogs != null) {
            for (int ii = 0; ii < geocacheLogs.length(); ii++) {
                logList.add(createLog((JSONObject) geocacheLogs.get(ii), cache));
            }
        }
        return logList;
    }

    private static LogEntry createLog(JSONObject geocacheLog, AbstractCache cache) {
        LogEntry logEntry = new LogEntry();
        logEntry.CacheId = cache.getId();
        logEntry.Comment = geocacheLog.optString("text", "");
        logEntry.Finder = getStringValue(geocacheLog, "owner", "username");
        String dateCreated = geocacheLog.optString("loggedDate", "");
        try {
            logEntry.Timestamp = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(dateCreated);
        } catch (Exception e) {
            logEntry.Timestamp = new Date();
        }
        logEntry.Type = LogTypes.parseString(geocacheLog.optString("type", ""));
        String referenceCode = geocacheLog.optString("referenceCode", "");
        logEntry.Id = generateLogId(referenceCode);
        return logEntry;
    }

    private static long generateLogId(String referenceCode) {
        referenceCode = referenceCode.substring(2); // ohne "GL"
        if (referenceCode.charAt(0) > 'F' || referenceCode.length() > 6) {
            return Base31(referenceCode);
        } else {
            return Base16(referenceCode);
        }
    }

    private static long Base16(String s) {
        String base16chars = "0123456789ABCDEF";
        long r = 0;
        long f = 1;
        for (int i = s.length() - 1; i >= 0; i--) {
            r = r + base16chars.indexOf(s.charAt(i)) * f;
            f = f * 16;
        }
        return r;
    }

    private static long Base31(String s) {
        String base31chars = "0123456789ABCDEFGHJKMNPQRTVWXYZ";
        long r = -411120;
        long f = 1;
        for (int i = s.length() - 1; i >= 0; i--) {
            r = r + base31chars.indexOf(s.charAt(i)) * f;
            f = f * 31;
        }
        return r;
    }

    private static Array<ImageEntry> createImageList(JSONArray jImages, String GcCode, boolean withLogImages) {
        Array<ImageEntry> imageEntries = new Array<>();

        if (jImages != null) {
            for (int ii = 0; ii < jImages.length(); ii++) {

                JSONObject jImage = (JSONObject) jImages.get(ii);
                String Description = jImage.optString("description", "");
                String uri = jImage.optString("url", "");
                String referenceCode = jImage.optString("referenceCode", "GC");
                boolean isCacheImage = referenceCode.startsWith("GC");

                if (uri.length() > 0) {
                    if (isCacheImage || withLogImages) {
                        ImageEntry imageEntry = new ImageEntry();
                        imageEntry.CacheId = AbstractCache.GenerateCacheId(GcCode);
                        imageEntry.Description = Description;
                        imageEntry.GcCode = GcCode;
                        imageEntry.ImageUrl = uri.replace("img.geocaching.com/gc/cache", "img.geocaching.com/cache");

                        imageEntry.IsCacheImage = false; // means it is not retrieved from description examination todo check somehow if it is a spoiler or what it is used for
                        imageEntry.LocalPath = ""; // create at download / read from DB
                        // imageEntry.LocalPath =  DescriptionImageGrabber.BuildDescriptionImageFilename(GcCode, URI.create(uri));
                        imageEntry.Name = ""; // does not exist in API 1.0
                        imageEntries.add(imageEntry);
                    }
                }

            }
        }
        return imageEntries;
    }

    private static Array<ImageEntry> addDescriptionImageList(Array<ImageEntry> imageList, AbstractCache cache) {

        Array<String> DescriptionImages = getDescriptionsImages(cache);
        for (String url : DescriptionImages) {
            // do not take those from spoilers or
            boolean isNotInImageList = true;
            for (ImageEntry im : imageList) {
                if (im.ImageUrl.equalsIgnoreCase(url)) {
                    isNotInImageList = false;
                    break;
                }
            }
            if (isNotInImageList) {
                ImageEntry imageEntry = new ImageEntry();
                imageEntry.CacheId = cache.getId();
                imageEntry.GcCode = cache.getGcCode().toString();
                imageEntry.Name = "";
                imageEntry.Description = url.substring(url.lastIndexOf("/") + 1);
                imageEntry.ImageUrl = url;
                imageEntry.IsCacheImage = true;
                imageEntry.LocalPath = ""; // create at download / read from DB
                imageList.add(imageEntry);
            }
        }
        return imageList;
    }

    private static Array<String> getDescriptionsImages(AbstractCache cache) {

        Array<String> images = new Array<>();

        URI baseUri;
        try {
            baseUri = URI.create(cache.getUrl().toString());
        } catch (Exception exc) {
            baseUri = null;
        }

        if (baseUri == null) {
            cache.setUrl("http://www.geocaching.com/seek/cache_details.aspx?wp=" + cache.getGcCode());
            try {
                baseUri = URI.create(cache.getUrl().toString());
            } catch (Exception exc) {
                return images;
            }
        }

        Array<DescriptionImageGrabber.Segment> imgTags = Segmentize(getString(cache.getShortDescription()), "<img", ">");
        imgTags.addAll(Segmentize(getString(cache.getLongDescription()), "<img", ">"));

        for (int i = 0, n = imgTags.size; i < n; i++) {
            DescriptionImageGrabber.Segment img = imgTags.get(i);
            int srcStart = -1;
            int srcEnd = -1;
            int srcIdx = img.text.toLowerCase().indexOf("src=");
            if (srcIdx != -1)
                srcStart = img.text.indexOf('"', srcIdx + 4);
            if (srcStart != -1)
                srcEnd = img.text.indexOf('"', srcStart + 1);

            if (srcIdx != -1 && srcStart != -1 && srcEnd != -1) {
                String src = img.text.substring(srcStart + 1, srcEnd);
                try {
                    URI imgUri = URI.create(src);

                    images.add(imgUri.toString());

                } catch (Exception exc) {
                }
            }
        }
        return images;
    }

    public static String getString(CharSequence value) {
        if (value == null)
            return "";
        else
            return value.toString();
    }


    private static CacheTypes CacheTypeFromID(int id) {
        switch (id) {
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
                return CacheTypes.APE;
            case 11:
                return CacheTypes.Camera;
            case 13:
                return CacheTypes.CITO;
            case 137:
                return CacheTypes.Earth;
            case 453:
                return CacheTypes.MegaEvent;
            case 1304:
                return CacheTypes.AdventuresExhibit;
            case 1858:
                return CacheTypes.Wherigo;
            case 3773:
                return CacheTypes.HQ;
            case 7005:
                return CacheTypes.Giga;
            case 217:
                return CacheTypes.ParkingArea;
            case 218:
                return CacheTypes.MultiQuestion;
            case 219:
                return CacheTypes.MultiStage;
            case 220:
                return CacheTypes.Final;
            case 221:
                return CacheTypes.Trailhead;
            case 452:
                return CacheTypes.ReferencePoint;
            default:
                return CacheTypes.Undefined;
        }
    }

    private static CacheSizes CacheSizeFromID(int id) {
        switch (id) {
            case 1:
                return CacheSizes.other; // not chosen
            case 2:
                return CacheSizes.micro;
            case 8:
                return CacheSizes.small;
            case 3:
                return CacheSizes.regular; //	Medium
            case 4:
                return CacheSizes.large;
            case 5:
                return CacheSizes.other; //	Virtual
            case 6:
                return CacheSizes.other;
            default:
                return CacheSizes.other;
        }
    }

    private static Date DateFromString(String d) {
        String ps = "yyyy-MM-dd'T'HH:mm:ss";
        if (d.endsWith("Z"))
            ps = ps + "'Z'";
        try {
            return new SimpleDateFormat(ps).parse(d);
        } catch (Exception e) {
            log.error("DateFromString", e);
            return new Date();
        }
    }

    private static String getUTCDate(Date date) {
        // check "2001-09-28T00:00:00"
        log.debug("getUTCDate In:" + new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").format(date));
        long utc = date.getTime();
        TimeZone tzp = TimeZone.getTimeZone("GMT");
        utc = utc - tzp.getOffset(utc);
        date.setTime(utc);
        String ret = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").format(date);
        log.debug("getUTCDate Out:" + ret);
        return ret;
    }

    private static String getStringValue(JSONObject jObject, String from, String KeyName) {
        JSONObject fromObject = jObject.optJSONObject(from);
        if (fromObject != null) {
            return fromObject.optString(KeyName, "");
        } else {
            return "";
        }
    }

    private enum MemberShipTypes {Unknown, Basic, Charter, Premium}

    public static class PQ implements Serializable, Comparable {
        private static final long serialVersionUID = 8308386638170255124L;
        public String name;
        public int cacheCount;
        public Date lastGenerated;
        public double sizeMB;
        public Date lastImported;
        public String GUID;

        @Override
        public int compareTo(Object o) {
            return name.compareTo(((PQ) o).name);
        }
    }

    public static class UserInfos {
        public String username;
        public MemberShipTypes memberShipType;
        public int findCount;
        // geocacheLimits
        public int remaining;
        public int remainingLite;
        public int remainingTime;
        public int remainingLiteTime;

        public UserInfos() {
            username = "";
            memberShipType = MemberShipTypes.Unknown;
            findCount = 0;
            remaining = -1;
            remainingLite = -1;
            remainingTime = -1;
            remainingLiteTime = -1;
        }
    }

    public static class GeoCacheRelated {
        public AbstractCache cache;
        public Array<LogEntry> logs;
        public Array<ImageEntry> images;
        // trackables

        public GeoCacheRelated(AbstractCache cache, Array<LogEntry> logs, Array<ImageEntry> images) {
            this.cache = cache;
            this.logs = logs;
            this.images = images;
        }
    }

    public static class Query {
        private static final String LiteFields = "referenceCode,favoritePoints,userData,name,difficulty,terrain,placedDate,geocacheType.id,geocacheSize.id,location,postedCoordinates,status,owner.username,ownerAlias";
        private static final String NotLiteFields = "hints,attributes,longDescription,shortDescription,additionalWaypoints,userWaypoints";
        private static final String StatusFields = "referenceCode,favoritePoints,status,trackableCount";
        private StringBuilder qString;
        private StringBuilder fieldsString;
        private StringBuilder expandString;
        private int maxToFetch;
        private Descriptor descriptor;


        public Query() {
            qString = new StringBuilder();
            fieldsString = new StringBuilder();
            expandString = new StringBuilder();
            maxToFetch = 1;
        }

        @Override
        public String toString() {
            return qString.toString();
        }

        public Query setMaxToFetch(int maxToFetch) {
            this.maxToFetch = maxToFetch;
            return this;
        }

        public Query searchInCircleOf100Miles(Coordinate center) {
            addSearchFilter("location:[" + center.getLatitude() + "," + center.getLongitude() + "]"); // == +radius:100mi
            return this;
        }

        public Query searchInCircle(Coordinate center, int radiusInMeters) {
            if (radiusInMeters > 160934) radiusInMeters = 160934; // max 100 miles
            addSearchFilter("location:[" + center.getLatitude() + "," + center.getLongitude() + "]");
            addSearchFilter("radius:" + radiusInMeters + "m");
            return this;
        }

        public Query searchForTitle(String containsIgnoreCase) {
            addSearchFilter("name:" + containsIgnoreCase);
            return this;
        }

        public Query searchForOwner(String userName) {
            addSearchFilter("hby:" + userName);
            return this;
        }

        public Query excludeOwn() {
            addSearchFilter("hby:" + "not(" + fetchMyUserInfos().username + ")");
            return this;
        }

        public Query excludeFinds() {
            addSearchFilter("fby:" + "not(" + fetchMyUserInfos().username + ")");
            return this;
        }

        public Query onlyActiveGeoCaches() {
            addSearchFilter("ia:true");
            return this;
        }

        public Query onlyTheseGeoCaches(String commaSeparatedListOfGCCodes) {
            addSearchFilter("code:" + commaSeparatedListOfGCCodes);
            return this;
        }

        public Query notTheseGeoCaches(String commaSeparatedListOfGCCodes) {
            addSearchFilter("code:" + "not(" + commaSeparatedListOfGCCodes + ")");
            return this;
        }

        public Query publishedDate(Date date, String when) {
            String before = "";
            String after = "";
            SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd");
            if (when == "<=") {
                before = f.format(date);
            } else if (when == ">=") {
                after = f.format(date);
            } else if (when == "=") {
                before = f.format(date);
                after = before;
            }
            addSearchFilter("pd:[" + after + "," + before + "]"); // inclusive
            return this;
        }

        private void addSearchFilter(String filter) {
            qString.append('+').append(filter);
        }

        public Query resultWithLiteFields() {
            addResultField(LiteFields);
            return this;
        }

        public Query resultWithFullFields() {
            addResultField(LiteFields);
            addResultField(NotLiteFields);
            return this;
        }

        public Query resultForStatusFields() {
            addResultField(StatusFields);
            return this;
        }

        private void addResultField(String field) {
            fieldsString.append(",").append(field);
        }

        public Query addExpandField(String field, int count) {
            expandString.append(",").append(field + ":" + count);
            return this;
        }

        public Query resultWithLogs(int count) {
            expandString.append(",").append("geocachelogs:" + count);
            return this;
        }

        public Query resultWithImages(int count) {
            expandString.append(",").append("images:" + count);
            return this;
        }

        public Query resultWithTrackables(int count) {
            expandString.append(",").append("trackables:" + count);
            return this;
        }

        public Descriptor getDescriptor() {
            return descriptor;
        }

        public Query setDescriptor(Descriptor descriptor) {
            this.descriptor = descriptor;
            return this;
        }

        public boolean isSearch() {
            return qString.length() > 0;
        }

        public boolean containsOnlyLiteFields(Array<String> fields) {
            boolean onlyLiteFields = true;
            for (String s : fields) {
                if (NotLiteFields.contains(s)) {
                    onlyLiteFields = false;
                    break;
                }
            }
            return onlyLiteFields;
        }

        public Array<String> getFields() {
            Array<String> result = new Array<>();
            String fs = fieldsString.toString();
            if (fs.length() > 0)
                result.addAll(fs.substring(1).split(","));
            return result;
        }

        public Request putQuery(Request r) {
            String qs = qString.toString();
            if (qs.length() > 0) r.param("q", qs.substring(1));
            String fs = fieldsString.toString();
            if (fs.length() > 0) r.param("fields", fs.substring(1));
            String es = expandString.toString();
            if (es.length() > 0) r.param("expand", es.substring(1));
            return r;
        }
    }

    public class Descriptor {
        //todo is dummy for Descriptor for life api
    }
}
