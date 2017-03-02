/*
 * Copyright (C) 2017 team-cachebox.de
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
package de.longri.cachebox3.gui.map.layer;

import de.longri.cachebox3.CB;
import de.longri.cachebox3.gui.events.SelectedCacheEvent;
import de.longri.cachebox3.gui.events.SelectedCacheEventList;
import de.longri.cachebox3.locator.Coordinate;
import de.longri.cachebox3.locator.Locator;
import de.longri.cachebox3.locator.events.PositionChangedEvent;
import de.longri.cachebox3.locator.events.PositionChangedEventList;
import de.longri.cachebox3.types.Cache;
import de.longri.cachebox3.types.Waypoint;
import org.oscim.backend.canvas.Color;
import org.oscim.backend.canvas.Paint;
import org.oscim.core.Box;
import org.oscim.core.GeometryBuffer;
import org.oscim.core.Tile;
import org.oscim.event.Event;
import org.oscim.layers.GenericLayer;
import org.oscim.map.Map;
import org.oscim.renderer.BucketRenderer;
import org.oscim.renderer.GLViewport;
import org.oscim.renderer.bucket.LineBucket;
import org.oscim.theme.styles.LineStyle;
import org.oscim.utils.FastMath;

/**
 * Created by Longri on 02.03.2017.
 */
public class DirectLineLayer extends GenericLayer implements PositionChangedEvent, SelectedCacheEvent {

    private final DirectLineRenderer directLineRenderer;
    private Event lastEvent;

    /**
     * @param map ...
     */
    public DirectLineLayer(Map map) {
        super(map, new DirectLineRenderer(map));
        directLineRenderer = (DirectLineRenderer) mRenderer;
        PositionChangedEventList.add(this);
        SelectedCacheEventList.add(this);
    }

    @Override
    public void positionChanged(Event event) {
        redrawLine(event);
    }

    @Override
    public void orientationChanged(Event event) {
        redrawLine(event);
    }

    @Override
    public void speedChanged(Event event) {
        redrawLine(event);
    }

    @Override
    public String getReceiverName() {
        return "DirectLineLayer";
    }

    @Override
    public Priority getPriority() {
        return Priority.Normal;
    }

    private void redrawLine(Event event) {
        if (lastEvent == event) return;
        lastEvent = event;

        Coordinate selectedCoordinate = CB.getSelectedCoord();
        if (selectedCoordinate == null) {
            directLineRenderer.setInvalid();
            return;
        }

        Coordinate ownPosition = Locator.getCoordinate();
        if (ownPosition == null) {
            directLineRenderer.setInvalid();
            return;
        }
        directLineRenderer.setLine(selectedCoordinate, ownPosition);
    }

    @Override
    public void selectedCacheChanged(Cache selectedCache, Waypoint waypoint, Cache LastSelectedCache, Waypoint LastWaypoint) {
        redrawLine(new Event());
    }

    private static class DirectLineRenderer extends BucketRenderer {

        //TODO initial with style (Color, with, Cap, Texture)
        LineBucket ll = buckets.addLineBucket(0,
                new LineStyle(Color.fade(Color.RED, 0.8f), 5.5f, Paint.Cap.ROUND));

        GeometryBuffer g = new GeometryBuffer(2, 1);
        private boolean invalidLine = true;
        private double startPointX, startPointY, endPointX, endPointY;
        private Box mBBox;

        private DirectLineRenderer(Map map) {
            this.mBBox = new Box();
            float halfWidth = map.getWidth() / 2;
            float halfHeight = map.getHeight() / 2;
            this.mBBox.xmin = -halfWidth;
            this.mBBox.xmax = halfWidth;
            this.mBBox.ymin = -halfHeight;
            this.mBBox.ymax = halfHeight;
        }

        @Override
        public void update(GLViewport v) {
            buckets.clear();

            if (invalidLine) return;

            mMapPosition.copy(v.pos);
            double mx = v.pos.x;
            double my = v.pos.y;
            double scale = Tile.SIZE * v.pos.scale;

            float sX = (float) ((startPointX - mx) * scale);
            float sY = (float) ((startPointY - my) * scale);
            if (!mBBox.contains(sX, sY)) {
                sX = (float) FastMath.clamp(sX, mBBox.xmin, mBBox.xmax);
                sY = (float) FastMath.clamp(sY, mBBox.ymin, mBBox.ymax);
            }


            float eX = (float) ((endPointX - mx) * scale);
            float eY = (float) ((endPointY - my) * scale);
            if (!mBBox.contains(eX, eY)) {
                eX = (float) FastMath.clamp(eX, mBBox.xmin, mBBox.xmax);
                eY = (float) FastMath.clamp(eY, mBBox.ymin, mBBox.ymax);
            }

            buckets.set(ll);
            g.clear();
            g.startLine();

            g.addPoint(sX, sY);
            g.addPoint(eX, eY);

            ll.addLine(g);

            compile();
        }

        public void setInvalid() {
            this.invalidLine = true;
        }

        public void setLine(Coordinate selectedCoordinate, Coordinate ownPosition) {
            startPointX = (ownPosition.longitude + 180.0) / 360.0;
            double sinLatitude = Math.sin(ownPosition.latitude * (Math.PI / 180.0));
            startPointY = 0.5 - Math.log((1.0 + sinLatitude) / (1.0 - sinLatitude)) / (4.0 * Math.PI);

            endPointX = (selectedCoordinate.longitude + 180.0) / 360.0;
            sinLatitude = Math.sin(selectedCoordinate.latitude * (Math.PI / 180.0));
            endPointY = 0.5 - Math.log((1.0 + sinLatitude) / (1.0 - sinLatitude)) / (4.0 * Math.PI);
            this.invalidLine = false;
        }
    }
}
