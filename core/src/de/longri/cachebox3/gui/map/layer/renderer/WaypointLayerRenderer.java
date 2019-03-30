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
package de.longri.cachebox3.gui.map.layer.renderer;


import com.badlogic.gdx.scenes.scene2d.ui.Cluster;
import com.badlogic.gdx.scenes.scene2d.ui.MapWayPointItem;
import com.badlogic.gdx.utils.Disposable;
import de.longri.cachebox3.gui.map.layer.WaypointLayer;
import de.longri.cachebox3.locator.Coordinate;
import de.longri.cachebox3.locator.LatLong;
import de.longri.cachebox3.locator.geocluster.GeoBoundingBoxInt;
import de.longri.cachebox3.utils.MathUtils;
import org.oscim.backend.canvas.Bitmap;
import org.oscim.core.Box;
import org.oscim.core.Point;
import org.oscim.core.PointF;
import org.oscim.core.Tile;
import org.oscim.renderer.BucketRenderer;
import org.oscim.renderer.GLViewport;
import org.oscim.renderer.MapRenderer;
import org.oscim.renderer.atlas.TextureRegion;
import org.oscim.renderer.bucket.SymbolBucket;
import org.oscim.renderer.bucket.SymbolItem;
import org.oscim.utils.TimSort;
import org.oscim.utils.geom.GeometryUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Comparator;

public class WaypointLayerRenderer extends BucketRenderer implements Disposable {

    public static final Logger log = LoggerFactory.getLogger(WaypointLayerRenderer.class);
    private static final PointF CENTER_OFFSET = new PointF(0.5f, 0.5f);
    private final static short MAX_VALUE = (short) (Short.MAX_VALUE / MapRenderer.COORD_SCALE);

    private final Bitmap mDefaultBitmap;

    private final SymbolBucket mSymbolLayer;
    private final float[] mBox = new float[8];
    private final WaypointLayer mWaypointLayer;
    private final Point mMapPoint = new Point();
    private final Box mapVisibleBoundingBox = new Box();

    private int lastZoomLevel = -1;
    private double lastMapPosX = Double.MIN_VALUE;
    private double lastMapPosY = Double.MIN_VALUE;
    private float lastMapBearing = Float.MIN_VALUE;


    /**
     * flag to force update of Clusters
     */
    private boolean mUpdate;

    public InternalItem[] mItems;

    public void dispose() {
        Arrays.fill(mItems, null);
        mItems = null;
    }

    public static class InternalItem {
        public MapWayPointItem item;
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

    public WaypointLayerRenderer(WaypointLayer waypointLayer, Bitmap defaultSymbol) {
        mSymbolLayer = new SymbolBucket();
        mWaypointLayer = waypointLayer;
        mDefaultBitmap = defaultSymbol;
    }


    private boolean chekForClusterChanges(GLViewport v) {
        mMapPosition.copy(v.pos);
        final int zoomLevel = mMapPosition.getZoomLevel();

        if (mMapPosition.getZoomLevel() != lastZoomLevel) {
            lastZoomLevel = mMapPosition.getZoomLevel();

            mWaypointLayer.map().viewport().getBBox(mapVisibleBoundingBox, 1350);
            mapVisibleBoundingBox.map2mercator();

            mWaypointLayer.reduceCluster(new GeoBoundingBoxInt(mapVisibleBoundingBox),
                    zoomLevel < 14 ? mapVisibleBoundingBox.getWidth() * 7675 : 0, false);
            lastMapPosX = mMapPosition.x;
            lastMapPosY = mMapPosition.y;
            lastMapBearing = mMapPosition.bearing;
            return true;
        }

        // check if map pos changed
        if (lastMapPosX != mMapPosition.x ||
                lastMapPosY != mMapPosition.y || lastMapBearing != mMapPosition.bearing) {

            double moved = Math.hypot(Math.abs(lastMapPosX - mMapPosition.x),
                    Math.abs(lastMapPosY - mMapPosition.y)) * 7675;

            mWaypointLayer.map().viewport().getBBox(mapVisibleBoundingBox, 1350);
            mapVisibleBoundingBox.map2mercator();

            if (moved > mapVisibleBoundingBox.getWidth()) {
                mWaypointLayer.reduceCluster(new GeoBoundingBoxInt(mapVisibleBoundingBox),
                        zoomLevel < 14 ? mapVisibleBoundingBox.getWidth() * 7675 : 0, true);
                lastMapPosX = mMapPosition.x;
                lastMapPosY = mMapPosition.y;
                lastMapBearing = mMapPosition.bearing;
                return true;
            }
        }
        return false;
    }

    @Override
    public synchronized void update(GLViewport v) {

        boolean zoomChecked = false;
        boolean zoomChanged = false;

        if (!v.changed() && !mUpdate) {
            zoomChecked = true;
            if (!chekForClusterChanges(v)) {
                return;
            }
        }
        if (!zoomChecked) {
            zoomChanged = chekForClusterChanges(v);
        }

        mUpdate = false;

        double mx = v.pos.x;
        double my = v.pos.y;
        double scale = Tile.SIZE * v.pos.scale;


        int numVisible = 0;

        //increase view to show items that are partially visible

        int mExtents = 100;
        mWaypointLayer.map().viewport().getMapExtents(mBox, mExtents);
        for (int i = 0, n = 8; i < n; i++)
            MathUtils.clampToMinMax(mBox, i, MAX_VALUE);

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
                it.item.visible = true;
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
        int zoomLevel = mMapPosition.getZoomLevel();
        if (zoomChanged) sort(mItems);

        for (InternalItem it : mItems) {
            if (!it.visible)
                continue;

            if (it.changes) {
                it.visible = false;
                it.item.visible = false;
                continue;
            }

            TextureRegion[] textureRegions = it.item.getMapSymbol(zoomLevel);
            if (textureRegions == null) continue;

            for (TextureRegion region : textureRegions) {
                SymbolItem s = SymbolItem.pool.get();
                s.set(it.x, it.y, region, true);

                //set center offset
                s.offset = CENTER_OFFSET;
                mSymbolLayer.pushSymbol(s);
            }
        }

        buckets.set(mSymbolLayer);
        buckets.prepare();
        buckets.compile(true);
        compile();
    }

    public void populate(int size, boolean resort) {

        InternalItem[] tmp = new InternalItem[size];

        for (int i = 0; i < size; i++) {
            populateItem(tmp, i);
        }
        synchronized (this) {
            mUpdate = true;
            sort(tmp);
            mItems = tmp;
        }

    }

    public void populateItem(int index) {
        synchronized (this) {
            populateItem(mItems, index);
        }
    }

    private void populateItem(InternalItem[] tmp, int index) {
        InternalItem it = new InternalItem();
        tmp[index] = it;
        it.item = mWaypointLayer.createItem(index);

        /* pre-project points */

        double lat, lon;
        if (it.item instanceof Cluster) {
            //set draw point to center of cluster
            Coordinate centerCoord = ((Cluster) it.item).getCenter();
            lat = centerCoord.getLatitude();
            lon = centerCoord.getLongitude();

        } else {
            lat = it.item.getLatitude();
            lon = it.item.getLongitude();
        }

        mMapPoint.x = (lon + 180.0) / 360.0;

        double sinLatitude = Math.sin(lat * (Math.PI / 180.0));
        mMapPoint.y = 0.5 - Math.log((1.0 + sinLatitude) / (1.0 - sinLatitude)) / (4.0 * Math.PI);

        it.px = mMapPoint.x;
        it.py = mMapPoint.y;
    }

    public void update() {
        mUpdate = true;
    }

    private static TimSort<InternalItem> ZSORT = new TimSort<InternalItem>();

    public static void sort(InternalItem[] a) {
//        log.debug("Sort Items");
        ZSORT.doSort(a, zComparator, 0, a.length);
    }

    private final static Comparator<InternalItem> zComparator = new Comparator<InternalItem>() {
        @Override
        public int compare(InternalItem a, InternalItem b) {
            if (a.item.selected) return -1;
            if (b.item.selected) return 1;
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

        reuse.x = (p.getLongitude() + 180.0D) / 360.0D;
        double sinLatitude = Math.sin(p.getLatitude() * 0.017453292519943295D);
        reuse.y = 0.5D - Math.log((1.0D + sinLatitude) / (1.0D - sinLatitude)) / 12.566370614359172D;
        return reuse;
    }
}
