/*
 * Copyright 2013 Hannes Janetzek
 * Copyright 2016 Izumi Kawashima
 *
 * This file is part of the OpenScienceMap project (http://www.opensciencemap.org).
 *
 * This program is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */

package de.longri.cachebox3.gui.map.layer.cluster;


import de.longri.cachebox3.locator.LatLong;
import org.oscim.core.GeoPoint;
import org.oscim.core.MercatorProjection;
import org.oscim.core.Point;
import org.oscim.core.Tile;
import org.oscim.renderer.BucketRenderer;
import org.oscim.renderer.GLViewport;
import org.oscim.renderer.bucket.SymbolBucket;
import org.oscim.renderer.bucket.SymbolItem;
import org.oscim.utils.TimSort;
import org.oscim.utils.geom.GeometryUtils;

import java.util.Comparator;

public class ClusterRenderer extends BucketRenderer {

    public final ClusterSymbol mDefaultCluster;

    private final SymbolBucket mSymbolLayer;
    private final float[] mBox = new float[8];
    private final ClusterLayer<ClusterInterface> mClusterLayer;
    private final Point mMapPoint = new Point();

    /**
     * increase view to show items that are partially visible
     */
    protected int mExtents = 100;

    /**
     * flag to force update of Clusters
     */
    private boolean mUpdate;

    private InternalItem[] mItems;

    static class InternalItem {
        ClusterInterface item;
        boolean visible;
        boolean changes;
        float x, y;
        double px, py;
        float dy;

        @Override
        public String toString() {
            return "\n" + x + ":" + y + " / " + dy + " " + visible;
        }
    }

    public ClusterRenderer(ClusterLayer<ClusterInterface> ClusterLayer, ClusterSymbol defaultSymbol) {
        mSymbolLayer = new SymbolBucket();
        mClusterLayer = ClusterLayer;
        mDefaultCluster = defaultSymbol;
    }

    @Override
    public synchronized void update(GLViewport v) {
        if (!v.changed() && !mUpdate)
            return;

        mUpdate = false;

        double mx = v.pos.x;
        double my = v.pos.y;
        double scale = Tile.SIZE * v.pos.scale;

        //int changesInvisible = 0;
        //int changedVisible = 0;
        int numVisible = 0;

        mClusterLayer.map().viewport().getMapExtents(mBox, mExtents);

        long flip = (long) (Tile.SIZE * v.pos.scale) >> 1;

        if (mItems == null) {
            if (buckets.get() != null) {
                buckets.clear();
                compile();
            }
            return;
        }

        double angle = Math.toRadians(v.pos.bearing);
        float cos = (float) Math.cos(angle);
        float sin = (float) Math.sin(angle);

        /* check visibility */
        for (InternalItem it : mItems) {
            it.changes = false;
            it.x = (float) ((it.px - mx) * scale);
            it.y = (float) ((it.py - my) * scale);

            if (it.x > flip)
                it.x -= (flip << 1);
            else if (it.x < -flip)
                it.x += (flip << 1);

            if (!GeometryUtils.pointInPoly(it.x, it.y, mBox, 8, 0)) {
                if (it.visible) {
                    it.changes = true;
                    //changesInvisible++;
                }
                continue;
            }

            it.dy = sin * it.x + cos * it.y;

            if (!it.visible) {
                it.visible = true;
                //changedVisible++;
            }
            numVisible++;
        }

        //log.debug(numVisible + " " + changedVisible + " " + changesInvisible);

        /* only update when zoomlevel changed, new items are visible
         * or more than 10 of the current items became invisible */
        //if ((numVisible == 0) && (changedVisible == 0 && changesInvisible < 10))
        //    return;
        buckets.clear();

        if (numVisible == 0) {
            compile();
            return;
        }
        /* keep position for current state */
        mMapPosition.copy(v.pos);
        mMapPosition.bearing = -mMapPosition.bearing;

        sort(mItems, 0, mItems.length);
        //log.debug(Arrays.toString(mItems));
        for (InternalItem it : mItems) {
            if (!it.visible)
                continue;

            if (it.changes) {
                it.visible = false;
                continue;
            }

            ClusterSymbol cluster = it.item.getCluster();
            if (cluster == null)
                cluster = mDefaultCluster;

            SymbolItem s = SymbolItem.pool.get();
            if (cluster.isBitmap()) {
                s.set(it.x, it.y, cluster.getBitmap(), true);
            } else {
                s.set(it.x, it.y, cluster.getTextureRegion(), true);
            }
            s.offset = cluster.getHotspot();
            s.billboard = cluster.isBillboard();
            mSymbolLayer.pushSymbol(s);
        }

        buckets.set(mSymbolLayer);
        buckets.prepare();

        compile();
    }

    protected void populate(int size) {

        InternalItem[] tmp = new InternalItem[size];

        for (int i = 0; i < size; i++) {
            InternalItem it = new InternalItem();
            tmp[i] = it;
            it.item = mClusterLayer.createItem(i);

            /* pre-project points */
            MercatorProjection.project(it.item.getPoint(), mMapPoint);
            it.px = mMapPoint.x;
            it.py = mMapPoint.y;
        }
        synchronized (this) {
            mUpdate = true;
            mItems = tmp;
        }
    }

    public void update() {
        mUpdate = true;
    }

    static TimSort<InternalItem> ZSORT = new TimSort<InternalItem>();

    public static void sort(InternalItem[] a, int lo, int hi) {
        int nRemaining = hi - lo;
        if (nRemaining < 2) {
            return;
        }

        ZSORT.doSort(a, zComparator, lo, hi);
    }

    final static Comparator<InternalItem> zComparator = new Comparator<InternalItem>() {
        @Override
        public int compare(InternalItem a, InternalItem b) {
            if (a.visible && b.visible) {
                if (a.dy > b.dy) {
                    return -1;
                }
                if (a.dy < b.dy) {
                    return 1;
                }
            } else if (a.visible) {
                return -1;
            } else if (b.visible) {
                return 1;
            }

            return 0;
        }
    };

    public static Point project(LatLong p, Point reuse) {
        if(reuse == null) {
            reuse = new Point();
        }

        reuse.x = ((double)p.longitude + 180.0D) / 360.0D;
        double sinLatitude = Math.sin((double)p.latitude * 0.017453292519943295D);
        reuse.y = 0.5D - Math.log((1.0D + sinLatitude) / (1.0D - sinLatitude)) / 12.566370614359172D;
        return reuse;
    }
}
