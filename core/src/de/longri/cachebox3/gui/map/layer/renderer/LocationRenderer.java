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


import com.badlogic.gdx.utils.Disposable;
import de.longri.cachebox3.gui.map.layer.LocationLayer;
import org.oscim.core.Point;
import org.oscim.core.PointF;
import org.oscim.core.Tile;
import org.oscim.map.Map;
import org.oscim.renderer.BucketRenderer;
import org.oscim.renderer.GLViewport;
import org.oscim.renderer.atlas.TextureRegion;
import org.oscim.renderer.bucket.SymbolBucket;
import org.oscim.renderer.bucket.SymbolItem;
import org.oscim.utils.geom.GeometryUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Longri on 14.02.17
 */
public class LocationRenderer extends BucketRenderer implements Disposable {

    public static final Logger log = LoggerFactory.getLogger(LocationRenderer.class);
    private static final PointF CENTER_OFFSET = new PointF(0.5f, 0.5f);

    private final SymbolBucket mSymbolBucket;
    private final float[] mBox = new float[8];
    private final LocationLayer locationLayer;
    private final Point mapPoint = new Point();
    private final Map mMap;


    /**
     * flag to force update with location changed
     */
    private boolean mUpdate;
    private TextureRegion arrowRegion;
    private float arrowHeading;

    public void dispose() {

    }

    public LocationRenderer(Map map, LocationLayer locationLayer) {
        mSymbolBucket = new SymbolBucket();
        this.locationLayer = locationLayer;
        this.mMap = map;

    }


    @Override
    public synchronized void update(GLViewport v) {
        if (!v.changed() && !mUpdate) return;
        mMapPosition.copy(v.pos);

        double mx = v.pos.x;
        double my = v.pos.y;
        double scale = Tile.SIZE * v.pos.scale;
        mMap.viewport().getMapExtents(mBox, 100);
        long flip = (long) (Tile.SIZE * v.pos.scale) >> 1;

        /* check visibility */
        float symbolX = (float) ((mapPoint.x - mx) * scale);
        float symbolY = (float) ((mapPoint.y - my) * scale);

        if (symbolX > flip)
            symbolX -= (flip << 1);
        else if (symbolX < -flip)
            symbolX += (flip << 1);
        buckets.clear();
        if (!GeometryUtils.pointInPoly(symbolX, symbolY, mBox, 8, 0)) {
            return;
        }

        mMapPosition.bearing = -mMapPosition.bearing;
        if (arrowRegion == null) return;
        SymbolItem symbolItem = SymbolItem.pool.get();
        symbolItem.set(symbolX, symbolY, arrowRegion, this.arrowHeading, true);
        symbolItem.offset = CENTER_OFFSET;
        mSymbolBucket.pushSymbol(symbolItem);

        buckets.set(mSymbolBucket);
        buckets.prepare();
        buckets.compile(true);
        compile();
        mUpdate = false;
    }

    public void update(double latitude, double longitude, float arrowHeading) {
        mUpdate = true;
        this.arrowHeading = -arrowHeading;
        while (this.arrowHeading < 0) this.arrowHeading += 360;
        mapPoint.x = (longitude + 180.0) / 360.0;
        double sinLatitude = Math.sin(latitude * (Math.PI / 180.0));
        mapPoint.y = 0.5 - Math.log((1.0 + sinLatitude) / (1.0 - sinLatitude)) / (4.0 * Math.PI);
        log.debug("Set x: {} y: {} head: {}", mapPoint.x, mapPoint.y, arrowHeading);
    }

    public void setTextureRegion(TextureRegion region) {
        arrowRegion = region;
    }
}
