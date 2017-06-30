package de.longri.cachebox3.apis.groundspeak_api.json_parser.stream_parser;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonStreamParser;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.types.Cache;
import de.longri.cachebox3.utils.ICancel;

import java.io.InputStream;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Longri on 30.06.2017.
 */
public class CheckCacheStateParser {

    /**
     * Called with every handled Cache
     */
    public interface ProgressIncrement {
        void increment();
    }


    private final static String ARCHIVED = "Archived";
    private final static String AVAILABLE = "Available";
    private final static String CACHECODE = "CacheCode";
    private final static String CACHENAME = "CacheName";
    private final static String CACHETYPE = "CacheType";
    private final static String PREMIUM = "Premium";
    private final static String TRACKABLECOUNT = "TrackableCount";


    public int parse(InputStream stream, final Array<Cache> caches, final ICancel icancel, final ProgressIncrement progressIncrement) {


        final AtomicInteger retValue = new AtomicInteger(0);

        // Parse JSON Result
        final JsonStreamParser parser = new JsonStreamParser() {
            boolean GeocacheStatusesArray = false;

            int cacheIndex = 0;
            boolean archived, available, premium, newCache;
            String cacheCode, cacheName;
            int cacheType, trackableCount;

            @Override
            public void startArray(String name) {
                if (name.equals("GeocacheStatuses")) {
                    GeocacheStatusesArray = true;
                }
            }

            public void number(String name, long value, String stringValue) {
                super.number(name, value, stringValue);
                if (newCache) {
                    if (CACHETYPE.equals(name)) {
                        cacheType = (int) value;
                    } else if (TRACKABLECOUNT.equals(name)) {
                        trackableCount = (int) value;
                    }
                } else if (name.equals("StatusCode")) {
                    if (value != 0) {
                        retValue.set(-1);
                        cancel();
                    }
                }
            }

            @Override
            public void startObject(String name) {
                super.startObject(name);
                if (GeocacheStatusesArray) {
                    archived = false;
                    available = false;
                    premium = false;
                    cacheCode = null;
                    cacheName = null;
                    cacheType = -1;
                    trackableCount = -1;
                    newCache = true;
                }
            }

            @Override
            public void pop() {
                super.pop();
                if (GeocacheStatusesArray && newCache) {
                    Cache cache = getCache(caches, cacheCode);
                    cache.setArchived(archived);
                    cache.setAvailable(available);
                    cache.NumTravelbugs = trackableCount;
                    if (progressIncrement != null) progressIncrement.increment();
                    newCache = false;
                }
            }

            public void string(String name, String value) {
                super.string(name, value);
                if (newCache) {
                    if (CACHECODE.equals(name)) {
                        cacheCode = value;
                    } else if (CACHENAME.equals(name)) {
                        cacheName = value;
                    }
                }
            }

            public void bool(String name, boolean value) {
                super.bool(name, value);
                if (newCache) {
                    if (ARCHIVED.equals(name)) {
                        archived = value;
                    } else if (AVAILABLE.equals(name)) {
                        available = value;
                    } else if (PREMIUM.equals(name)) {
                        premium = value; // TODO handle premium
                    }
                }
            }
        };

        final AtomicBoolean chkCancel = new AtomicBoolean(true);
        CB.postAsync(new Runnable() {
            @Override
            public void run() {
                while (chkCancel.get()) {
                    if (icancel.cancel()) {
                        parser.cancel();
                    }
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        parser.parse(stream);
        chkCancel.set(false);
//        JsonValue root = new JsonReader().parse(result);
//        JsonValue status = root.getChild("Status");
//        if (status.getInt("StatusCode") == 0) {
//            result = "";
//            JsonValue geocacheStatuses = root.getChild("GeocacheStatuses");
//            for (int ii = 0; ii < geocacheStatuses.length(); ii++) {
//                JSONObject jCache = (JSONObject) geocacheStatuses.get(ii);
//
//                Iterator<Cache> iterator = caches.iterator();
//                do {
//                    Cache tmp = iterator.next();
//                    if (jCache.getString("CacheCode").equals(tmp.getGcCode())) {
//                        tmp.setArchived(jCache.getBoolean("Archived"));
//                        tmp.setAvailable(jCache.getBoolean("Available"));
//                        tmp.NumTravelbugs = jCache.getInt("TrackableCount");
//                        // weitere Infos in diesem Json record
//                        // CacheName (getString)
//                        // CacheType (getDouble / getLong ?)
//                        // Premium   (getBoolean)
//                        break;
//                    }
//                } while (iterator.hasNext());
//
//            }

        return retValue.get();
    }


    private static Cache getCache(Array<Cache> caches, String gcCode) {
        for (int i = 0, n = caches.size; i < n; i++) {
            if (caches.get(i).getGcCode().equals(gcCode)) return caches.get(i);
        }
        return null;
    }

}
