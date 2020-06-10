package de.longri.cachebox3.live;


import com.badlogic.gdx.utils.Array;
import de.longri.cachebox3.locator.Descriptor;
import de.longri.cachebox3.types.AbstractCache;

import java.util.Collection;
import java.util.HashMap;
import java.util.Set;

/**
 * This list holds the Live loaded Caches with a maximum capacity and the Descriptor for Live request.
 *
 * @author Longri
 */
public class CacheListLive {
    private final HashMap<Long, Array<AbstractCache>> geoCachesPerDescriptor = new HashMap<>();
    private final int maxCapacity;
    private int noOfGeoCaches;
    private Descriptor mapCenterDesc;
    private final byte usedZoom;

    /**
     * Constructor
     *
     * @param _maxCapacity ?
     */
    public CacheListLive(int _maxCapacity, byte _usedZoom) {
        maxCapacity = _maxCapacity;
        usedZoom = _usedZoom;
        noOfGeoCaches = 0;
    }

    public Array<AbstractCache> addAndReduce(Descriptor descriptor, Array<AbstractCache> caches) {
        synchronized (geoCachesPerDescriptor) {
            if (geoCachesPerDescriptor.containsKey(descriptor.getHashCode())) {
                return null;
            } else {
                Array<AbstractCache> cleanedCaches = removeGeoCachesNotInDesctriptorsArea(descriptor, caches);
                geoCachesPerDescriptor.put(descriptor.getHashCode(), cleanedCaches);
                noOfGeoCaches = noOfGeoCaches + cleanedCaches.size;
                return removeGeoCachesForNotToExceedCapacityLimit();
            }
        }
    }

    private Array<AbstractCache> removeGeoCachesNotInDesctriptorsArea(Descriptor descriptor, Array<AbstractCache> geoCachesToClean) {
        int zoom = descriptor.getZoom();
        Array<AbstractCache> cleanedCaches = new Array<>();
        for (AbstractCache geoCache : geoCachesToClean) {
            Descriptor descriptorOfGeoCache = new Descriptor(geoCache.getLatitude(), geoCache.getLongitude(), zoom);
            if (descriptorOfGeoCache.equals(descriptor)) cleanedCaches.add(geoCache);
        }
        return cleanedCaches;
    }

    private Array<AbstractCache> removeGeoCachesForNotToExceedCapacityLimit() {
        Array<AbstractCache> removedCaches = new Array<>();
        while (noOfGeoCaches > maxCapacity && geoCachesPerDescriptor.keySet().size() > 1) {
            Descriptor descriptor = getFarestDescriptorFromMapCenter();
            if (descriptor != null) {
                removedCaches.addAll(geoCachesPerDescriptor.get(descriptor.getHashCode()));
                geoCachesPerDescriptor.remove(descriptor.getHashCode());
            }
        }
        noOfGeoCaches = noOfGeoCaches - removedCaches.size;
        return removedCaches;
    }

    private Descriptor getFarestDescriptorFromMapCenter() {
        if (mapCenterDesc == null)
            return null;

        int descX = mapCenterDesc.getX();
        int descY = mapCenterDesc.getY();

        int tmpDistance = 0;
        Descriptor tmpDesc = null;
        for (Long l2 : geoCachesPerDescriptor.keySet()) {
            Array<AbstractCache> cl2 = geoCachesPerDescriptor.get(l2);
            Descriptor desc2 = new Descriptor(cl2.get(0).getLatitude(), cl2.get(0).getLongitude(), usedZoom);
            int distance = Math.abs(descX - desc2.getX()) + Math.abs(descY - desc2.getY());
            if (distance > tmpDistance) {
                tmpDistance = distance;
                tmpDesc = desc2;
            }
        }
        return tmpDesc;
    }

    public int getSize() {
        return noOfGeoCaches;
    }

    public void setCenterDescriptor(Descriptor descriptor) {
        mapCenterDesc = descriptor;
    }

    public boolean contains(Descriptor descriptor) {
        return geoCachesPerDescriptor.containsKey(descriptor.getHashCode());
    }

    public Collection<Array<AbstractCache>> getAllCacheLists() {
        return geoCachesPerDescriptor.values();
    }

    public Set<Long> getDescriptorsHashCodes() {
        return geoCachesPerDescriptor.keySet();
    }

    /*
    public Array<Cache> getCachesOfDescriptor(Descriptor descriptor) {
        return geoCachesPerDescriptor.get(descriptor.getHashCode());
    }
     */

    public int getNoOfGeoCachesForDescriptor(Descriptor descriptor) {
        long hc = descriptor.getHashCode();
        if (geoCachesPerDescriptor.containsKey(hc)){
            return geoCachesPerDescriptor.get(hc).size;
        }
        else {
            return 0;
        }
    }
}
