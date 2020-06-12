package de.longri.cachebox3.live;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.apis.GroundspeakAPI;
import de.longri.cachebox3.events.CacheListChangedEvent;
import de.longri.cachebox3.events.EventHandler;
import de.longri.cachebox3.locator.Coordinate;
import de.longri.cachebox3.locator.Descriptor;
import de.longri.cachebox3.settings.Config;
import de.longri.cachebox3.sqlite.Database;
import de.longri.cachebox3.types.AbstractCache;
import de.longri.cachebox3.types.Category;
import de.longri.cachebox3.types.GpxFilename;
import de.longri.cachebox3.utils.IChanged;
import de.longri.cachebox3.utils.NamedRunnable;
import org.json.JSONArray;
import org.json.JSONTokener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.util.Collection;
import java.util.Date;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

public class LiveMapQue {
    private static final Logger log = LoggerFactory.getLogger(LiveMapQue.class);
    private static final String LIVE_CACHE_NAME = "Live_Request";
    private static final String LIVE_CACHE_EXTENSION = ".txt";
    private static final byte DEFAULT_ZOOM_14 = 14;
    // private static final int MAX_REQUEST_CACHE_RADIUS_14 = 1060;
    private static final byte DEFAULT_ZOOM_13 = 13;
    // private static final int MAX_REQUEST_CACHE_RADIUS_13 = 2120;
    private static final int MAX_REQUEST_CACHE_COUNT = 50; //
    private static LiveMapQue liveMapQue;
    private static CopyOnWriteArrayList<IChanged> downloadActiveListeners;
    private final Array<Descriptor> descriptorStack;
    private final AtomicBoolean downloadIsActive;
    private Descriptor lastLo, lastRu;
    private Byte count = 0;
    private CacheListLive cacheListLive;
    private Live_Radius radius;
    private byte usedZoom;
    private GpxFilename gpxFilename;
    private final LoopThread loopThread = new LoopThread(2000) {

        protected boolean cancelLoop() {
            boolean cancel = descriptorStack.isEmpty();
            if (cancel) {
                // cachelist change with live caches is in ViewManager
                EventHandler.fire(new CacheListChangedEvent());
                log.debug("cancel loop thread");
            }
            return cancel;
        }

        protected void loop() {
            if (downloadIsActive.get()) return; // only one download at a time
            log.debug("download for one descriptor per loop");
            CB.postAsync(new NamedRunnable("") {
                @Override
                public void run() {
                    Descriptor descriptor;
                    do {
                        // ? only use, if on screen (ShowMap.getInstance().normalMapView.center / descriptor)
                        // ? perhaps dont' use lastIn but sort on distance
                        if (descriptorStack.notEmpty())
                            descriptor = descriptorStack.pop();
                        else descriptor = null;
                    } while (descriptor != null && cacheListLive.contains(descriptor));

                    if (descriptor != null) {
                        log.debug("Download caches from " + descriptor);

                        downloadIsActive.set(true);
                        fireDownloadActiveChanged();
                        int request_radius = usedZoom == DEFAULT_ZOOM_14 ? 1300 : 2600;
                    /*
                    double lon1 = DEG_RAD * tileXToLongitude(descriptor.getZoom(), descriptor.getX());
                    double lat1 = DEG_RAD * tileYToLatitude(descriptor.getZoom(), descriptor.getY());
                    double lon2 = DEG_RAD * tileXToLongitude(descriptor.getZoom(), descriptor.getX() + 1);
                    double lat2 = DEG_RAD * tileYToLatitude(descriptor.getZoom(), descriptor.getY() + 1);
                    request_radius = (int) (WGS84_MAJOR_AXIS * Math.acos(Math.sin(lat1) * Math.sin(lat2) + Math.cos(lat1) * Math.cos(lat2) * Math.cos((lon2 - lon1))) / 2 + 0.5); // round
                    */

                        GroundspeakAPI.Query q = new GroundspeakAPI.Query()
                                .setMaxToFetch(MAX_REQUEST_CACHE_COUNT)
                                .setDescriptor(descriptor)
                                .searchInCircle(descriptor.getCenterCoordinate(), request_radius);
                        if (Config.liveExcludeFounds.getValue()) q.excludeFinds();
                        if (Config.liveExcludeOwn.getValue()) q.excludeOwn();
                        q.resultWithLiteFields();
                        Array<GroundspeakAPI.GeoCacheRelated> apiCaches = null;
                        if (fileExistsMaxAge(getLocalCachePath(descriptor), Config.liveCacheTime.getEnumValue().getLifetime())) {
                            apiCaches = loadDescLiveFromCache(q);
                        }
                        if (apiCaches == null) {
                            if (gpxFilename == null) {
                                Category category = CB.getCategories().getCategory(Database.Data, "API-Import");
                                gpxFilename = category.addGpxFilename("API-Import");
                            }
                            apiCaches = GroundspeakAPI.getInstance().searchGeoCaches(q);
                        }

                        Array<AbstractCache> tmp = new Array<>();
                        for (GroundspeakAPI.GeoCacheRelated c : apiCaches)
                            tmp.add(c.cache);
                        final Array<AbstractCache> geoCachesToRemove = cacheListLive.addAndReduce(descriptor, tmp);
                        if (geoCachesToRemove == null) {
                            log.error("descriptor already in cachelistLive: should not happen here!");
                        } else {
                            if (geoCachesToRemove.size > 0) {
                                new Thread(() -> {
                                    synchronized (Database.Data.cacheList) {
                                        Database.Data.cacheList.removeAll(geoCachesToRemove, false);
                                    }
                                }).start();
                            }
                            if (cacheListLive.getNoOfGeoCachesForDescriptor(descriptor) > 0 || geoCachesToRemove.size > 0) {
                                // cachelist change with live caches is in ViewManager
                                EventHandler.fire(new CacheListChangedEvent());
                            }
                        }
                        downloadIsActive.set(false);
                        fireDownloadActiveChanged();
                    } else {
                        log.debug("no descriptor for download");
                        downloadIsActive.set(false);
                        fireDownloadActiveChanged();
                    }
                }
            });
        }
    };

    private LiveMapQue() {
        descriptorStack = new Array<>();
        downloadIsActive = new AtomicBoolean(false);
        downloadActiveListeners = new CopyOnWriteArrayList<>();

        Config.liveRadius.addChangedEventListener(() -> {
            radius = Config.liveRadius.getEnumValue();
            if (radius == Live_Radius.Zoom_13) {
                usedZoom = DEFAULT_ZOOM_13;
            } else {
                usedZoom = DEFAULT_ZOOM_14;
            }
        });

        radius = Config.liveRadius.getEnumValue();

        if (radius == Live_Radius.Zoom_13) {
            usedZoom = DEFAULT_ZOOM_13;
        } else {
            usedZoom = DEFAULT_ZOOM_14;
        }

        cacheListLive = new CacheListLive(Config.liveMaxCount.getValue(), usedZoom);
        Config.liveMaxCount.addChangedEventListener(() -> cacheListLive = new CacheListLive(Config.liveMaxCount.getValue(), usedZoom));

        log.debug("A new LiveMapQue");

    }

    public static LiveMapQue getInstance() {
        if (liveMapQue == null) liveMapQue = new LiveMapQue();
        return liveMapQue;
    }

    private boolean fileExistsMaxAge(String localCachePath, int lifetime) {
        FileHandle abstractFile = new FileHandle(localCachePath);
        if (!abstractFile.exists())
            return false;

        int age = (int) ((new Date().getTime() - abstractFile.lastModified()) / 100000);

        if (age > lifetime)
            return false;
        return true;
    }

    public void addDownloadActiveListener(IChanged listener) {
        if (!downloadActiveListeners.contains(listener)) downloadActiveListeners.add(listener);
    }

    public void removeDownloadActiveListener(IChanged listener) {
        downloadActiveListeners.remove(listener);
    }

    private void fireDownloadActiveChanged() {
        for (IChanged listener : downloadActiveListeners) {
            listener.isChanged();
        }
    }

    private Array<GroundspeakAPI.GeoCacheRelated> loadDescLiveFromCache(GroundspeakAPI.Query query) {
        String path = getLocalCachePath(query.getDescriptor());
        String result;
        FileHandle fh = new FileHandle(path);
        try {
            BufferedReader br = fh.reader(1000);
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();
            while (line != null) {
                sb.append(line);
                sb.append(System.lineSeparator());
                line = br.readLine();
            }
            result = sb.toString();
            br.close();
            if (result.length() > 0) {
                JSONArray json = (JSONArray) new JSONTokener(result).nextValue();
                if (gpxFilename == null) {
                    Category category = CB.getCategories().getCategory(Database.Data, "API-Import");
                    gpxFilename = category.addGpxFilename("API-Import");
                }
                return GroundspeakAPI.getInstance().getGeoCacheRelateds(json, query.getFields(), null);
            }
        } catch (Exception e) {
            log.error("LiveMapQue", "loadDescLiveFromCache", e);
        }
        return null;
    }

    public String getLocalCachePath(Descriptor desc) {
        if (desc == null)
            return "";
        String tileCacheFolder = Config.tileCacheFolder.getValue();
        if (Config.tileCacheFolderLocal.getValue().length() > 0)
            tileCacheFolder = Config.tileCacheFolderLocal.getValue();
        return tileCacheFolder + "/" + LIVE_CACHE_NAME + "/" + desc.getZoom() + "/" + desc.getX() + "/" + desc.getY() + LIVE_CACHE_EXTENSION;
    }

    public void quePosition(Coordinate coord) {
        if (Config.liveMapEnabled.getValue()) {
            if (coord != null && coord.isValid()) {
                final Descriptor descriptor = new Descriptor(coord.getLatitude(), coord.getLongitude(), usedZoom);
                if (cacheListLive.contains(descriptor)) {
                    log.trace("Live caches for " + descriptor + " already there.");
                } else {
                    if (!GroundspeakAPI.getInstance().isDownloadLimitExceeded()) {
                        if (!descriptorStack.contains(descriptor, false)) {
                            descriptorStack.add(descriptor);
                            log.debug("Add " + descriptor + " to download stack. (" + descriptorStack.size + ")");
                        }
                        loopThread.start();
                    }
                }
            }
        }
    }

    public void queScreen(Descriptor lo, Descriptor ru) {
        // get geocaches for complete screen, only called if in car-mode
        if (GroundspeakAPI.getInstance().isDownloadLimitExceeded())
            return;

        // check last request don't Double!
        if (lastLo != null && lastRu != null) {
            // all Descriptor are into the last request?
            if (lastLo.getX() == lo.getX() && lastRu.getX() == ru.getX() &&
                    lastLo.getY() == lo.getY() && lastRu.getY() == ru.getY()) {
                // Still run every 15th!
                if (count++ < 15)
                    return;
                count = 0;
            }

        }

        lastLo = lo;
        lastRu = ru;

        Array<Descriptor> descList = new Array<>();
        for (int i = lo.getX(); i <= ru.getX(); i++) {
            for (int j = lo.getY(); j <= ru.getY(); j++) {
                Descriptor desc = new Descriptor(i, j, lo.getZoom());

                Array<Descriptor> descAddList = adjustZoom(desc);

                for (int k = 0; k < descAddList.size; k++) {
                    if (!descList.contains(descAddList.get(k), false))
                        descList.add(descAddList.get(k));
                }
            }
        }

        // remove descriptors that are already in cacheListLive
        Set<Long> alreadyThere = cacheListLive.getDescriptorsHashCodes();
        for (Descriptor descriptor : descList) {
            if (alreadyThere.contains(descriptor.getHashCode())) {
                descList.removeValue(descriptor, false);
                log.debug("removed " + descriptor);
            }
        }

        synchronized (descriptorStack) {
            descriptorStack.clear();
            descriptorStack.addAll(descList);
            if ((lo.getData() != null) && (lo.getData() instanceof Coordinate)) {
                Coordinate center = (Coordinate) lo.getData();
                if (center != null) {
                    final Descriptor mapCenterDesc = new Descriptor(center.getLatitude(), center.getLongitude(), lo.getZoom());
                    descriptorStack.sort((item1, item2) -> Integer.compare(item1.getDistance(mapCenterDesc), item2.getDistance(mapCenterDesc)));
                }
            }
        }

        loopThread.start();

    }

    private Array<Descriptor> adjustZoom(Descriptor descriptor) {
        int zoomDiff = usedZoom - descriptor.getZoom();
        int pow = (int) Math.pow(2, Math.abs(zoomDiff));
        Array<Descriptor> ret = new Array<>();
        if (zoomDiff > 0) {
            Descriptor def = new Descriptor(descriptor.getX() * pow, descriptor.getY() * pow, usedZoom);
            int count = pow / 2;
            for (int i = 0; i <= count; i++) {
                for (int j = 0; j <= count; j++) {
                    ret.add(new Descriptor(def.getX() + i, def.getY() + j, usedZoom));
                }
            }
        } else {
            ret.add(new Descriptor(descriptor.getX() / pow, descriptor.getY() / pow, usedZoom));
        }
        return ret;
    }

    public void setCenterDescriptor(Coordinate center) {
        cacheListLive.setCenterDescriptor(new Descriptor(center.getLatitude(), center.getLongitude(), usedZoom));
    }

    public boolean getDownloadIsActive() {
        return downloadIsActive.get() || !descriptorStack.isEmpty();
        // the last entry in descriptorStack is already popped, but not downloaded
    }

    public Collection<Array<AbstractCache>> getAllCacheLists() {
        return cacheListLive.getAllCacheLists();
    }

    public void clearDescriptorStack() {
        descriptorStack.clear();
    }

    public void cancelDownloads() {
        descriptorStack.clear();
    }

    public enum Live_Radius {
        Zoom_13, Zoom_14
    }
}


