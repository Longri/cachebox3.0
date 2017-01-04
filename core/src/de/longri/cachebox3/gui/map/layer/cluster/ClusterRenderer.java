/*
 * Copyright (C) 2016-2017 team-cachebox.de
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
package de.longri.cachebox3.gui.map.layer.cluster;


import de.longri.cachebox3.gui.map.layer.WaypointLayer;
import de.longri.cachebox3.locator.Coordinate;
import de.longri.cachebox3.locator.LatLong;
import de.longri.cachebox3.locator.geocluster.Cluster;
import de.longri.cachebox3.logging.Logger;
import de.longri.cachebox3.logging.LoggerFactory;
import org.oscim.backend.canvas.Bitmap;
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

    public static final Logger log = LoggerFactory.getLogger(BucketRenderer.class);

    public final Bitmap mDefaultBitmap;

    private final SymbolBucket mSymbolLayer;
    private final float[] mBox = new float[8];
    private final WaypointLayer mWaypointLayer;
    private final Point mMapPoint = new Point();

    private int lastZoomLevel = -1;

    /**
     * flag to force update of Clusters
     */
    private boolean mUpdate;

    private InternalItem[] mItems;

    static class InternalItem {
        Coordinate item;
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

    public ClusterRenderer(WaypointLayer waypointLayer, Bitmap defaultSymbol) {
        mSymbolLayer = new SymbolBucket();
        mWaypointLayer = waypointLayer;
        mDefaultBitmap = defaultSymbol;
    }


    private boolean chekZoomLevelChanged(GLViewport v) {
        mMapPosition.copy(v.pos);
        if (mMapPosition.getZoomLevel() != lastZoomLevel) {
            lastZoomLevel = mMapPosition.getZoomLevel();
            mWaypointLayer.setZoomLevel(mMapPosition);
            return true;
        }
        return false;
    }

    @Override
    public synchronized void update(GLViewport v) {

        boolean zoomChecked = false;
        boolean zoomChanged = false;

        if (!v.changed() && !mUpdate) {
            zoomChecked = true;
            if (!chekZoomLevelChanged(v)) {
                return;
            }
        }
        if (!zoomChecked) {
            zoomChanged = chekZoomLevelChanged(v);
        }

        mUpdate = false;

        double mx = v.pos.x;
        double my = v.pos.y;
        double scale = Tile.SIZE * v.pos.scale;


        int numVisible = 0;

        //increase view to show items that are partially visible

        int mExtents = 100;
        mWaypointLayer.map().viewport().getMapExtents(mBox, mExtents);

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

        buckets.clear();

        if (numVisible == 0) {
            compile();
            return;
        }

        mMapPosition.bearing = -mMapPosition.bearing;


        if (zoomChanged) sort(mItems, 0, mItems.length);
        //log.debug(Arrays.toString(mItems));
        for (InternalItem it : mItems) {
            if (!it.visible)
                continue;

            if (it.changes) {
                it.visible = false;
                continue;
            }

            Bitmap bitmap = it.item.mapSymbol;
            if (bitmap == null)
                bitmap = mDefaultBitmap;

            SymbolItem s = SymbolItem.pool.get();
            s.set(it.x, it.y, bitmap, true);

            mSymbolLayer.pushSymbol(s);
        }

        buckets.set(mSymbolLayer);
        buckets.prepare();

        compile();
    }

    public void populate(int size) {

        InternalItem[] tmp = new InternalItem[size];

        for (int i = 0; i < size; i++) {
            InternalItem it = new InternalItem();
            tmp[i] = it;
            it.item = mWaypointLayer.createItem(i);

            /* pre-project points */

            double lat, lon;
            if (it.item instanceof Cluster) {

                //set draw point to center of cluster
                Cluster cluster = (Cluster) it.item;
                Coordinate centerCoord = cluster.getCenter();
                lat = centerCoord.latitude;
                lon = centerCoord.longitude;

            } else {
                lat = it.item.latitude;
                lon = it.item.longitude;
            }

            mMapPoint.x = (lon + 180.0) / 360.0;

            double sinLatitude = Math.sin(lat * (Math.PI / 180.0));
            mMapPoint.y = 0.5 - Math.log((1.0 + sinLatitude) / (1.0 - sinLatitude)) / (4.0 * Math.PI);

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

    private static TimSort<InternalItem> ZSORT = new TimSort<InternalItem>();

    public static void sort(InternalItem[] a, int lo, int hi) {
        int nRemaining = hi - lo;
        if (nRemaining < 2) {
            log.debug("Items not sorted");
            return;
        }

        log.debug("Sort Items");
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
        if (reuse == null) {
            reuse = new Point();
        }

        reuse.x = (p.longitude + 180.0D) / 360.0D;
        double sinLatitude = Math.sin(p.latitude * 0.017453292519943295D);
        reuse.y = 0.5D - Math.log((1.0D + sinLatitude) / (1.0D - sinLatitude)) / 12.566370614359172D;
        return reuse;
    }
}
