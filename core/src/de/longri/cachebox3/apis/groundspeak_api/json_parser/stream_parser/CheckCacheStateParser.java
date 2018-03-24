/*
 * Copyright (C) 2017 - 2018 team-cachebox.de
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
package de.longri.cachebox3.apis.groundspeak_api.json_parser.stream_parser;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonStreamParser;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.apis.groundspeak_api.ApiResultState;
import de.longri.cachebox3.sqlite.Database;
import de.longri.cachebox3.types.AbstractCache;
import de.longri.cachebox3.utils.ICancel;
import de.longri.cachebox3.utils.NamedRunnable;

import java.io.InputStream;
import java.util.concurrent.atomic.AtomicBoolean;

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


    public ApiResultState parse(final Database database, InputStream stream, final Array<AbstractCache> caches, final ICancel icancel, final ProgressIncrement progressIncrement) {


        final ApiResultState[] retValue = {ApiResultState.UNKNOWN};

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
                        retValue[0] = ApiResultState.API_ERROR;
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
                    AbstractCache abstractCache = getCache(caches, cacheCode);

                    if (abstractCache.isArchived() != archived
                            || abstractCache.isAvailable() != available
                            || abstractCache.getNumTravelbugs() != trackableCount) {

                        //we must replace imutable Cache with mutable
                        abstractCache = replaceMutable(database, caches, cacheCode);
                        abstractCache.isChanged.set(false);
                        abstractCache.setArchived(archived);
                        abstractCache.setAvailable(available);
                        abstractCache.setNumTravelbugs(trackableCount);
                    }

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
        if (icancel != null) {
            CB.postAsync(new NamedRunnable("CheckCacheStateParser") {
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
        }

        parser.parse(stream);
        chkCancel.set(false);

        return retValue[0];
    }


    private static AbstractCache getCache(Array<AbstractCache> caches, String gcCode) {

        //TODO improve, store last search idx

        for (int i = 0, n = caches.size; i < n; i++) {
            if (caches.get(i).getGcCode().equals(gcCode)) return caches.get(i);
        }
        return null;
    }

    public static AbstractCache replaceMutable(Database database, Array<AbstractCache> caches, String gcCode) {
        int idx = 0;
        for (int n = caches.size; idx < n; idx++) {
            if (caches.get(idx).getGcCode().equals(gcCode)) break;
        }
        AbstractCache mutable = caches.get(idx).getMutable(database);
        caches.set(idx, mutable);
        return mutable;
    }

}
