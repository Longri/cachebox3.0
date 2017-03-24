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

import com.badlogic.gdx.utils.Disposable;
import de.longri.cachebox3.locator.Coordinate;
import de.longri.cachebox3.locator.events.newT.*;
import de.longri.cachebox3.utils.MathUtils;
import org.oscim.backend.canvas.Color;
import org.oscim.backend.canvas.Paint;
import org.oscim.core.GeometryBuffer;
import org.oscim.core.Tile;
import org.oscim.event.Event;
import org.oscim.layers.GenericLayer;
import org.oscim.layers.Layer;
import org.oscim.map.Map;
import org.oscim.renderer.BucketRenderer;
import org.oscim.renderer.GLViewport;
import org.oscim.renderer.bucket.LineBucket;
import org.oscim.theme.styles.LineStyle;

/**
 * Created by Longri on 02.03.2017.
 */
public class DirectLineLayer extends GenericLayer implements PositionChangedListener, SelectedCoordChangedListener, Disposable {

    private final DirectLineRenderer directLineRenderer;

    /**
     * @param map ...
     */
    public DirectLineLayer(Map map) {
        super(map, new DirectLineRenderer());
        directLineRenderer = (DirectLineRenderer) mRenderer;
        directLineRenderer.setLayer(this);
        EventHandler.add(this);
    }


    private void redrawLine() {
        if (!this.isEnabled()) return;

        Coordinate selectedCoordinate = EventHandler.getSelectedCoord();
        if (selectedCoordinate == null) {
            directLineRenderer.setInvalid();
            return;
        }

        Coordinate ownPosition = EventHandler.getMyPosition();
        if (ownPosition == null) {
            directLineRenderer.setInvalid();
            return;
        }
        directLineRenderer.setLine(selectedCoordinate, ownPosition);
    }

    @Override
    public void dispose() {
        EventHandler.remove(this);
        this.directLineRenderer.dispose();
    }

    @Override
    public void selectedCoordChanged(SelectedCoordChangedEvent event) {
        redrawLine();
    }

    @Override
    public void positionChanged(PositionChangedEvent event) {
        redrawLine();
    }


    private static class DirectLineRenderer extends BucketRenderer {

        //TODO initial with style (Color, with, Cap, Texture)
        LineBucket ll = buckets.addLineBucket(0,
                new LineStyle(Color.fade(Color.RED, 0.8f), 5.5f, Paint.Cap.ROUND));

        GeometryBuffer g = new GeometryBuffer(2, 1);
        private boolean invalidLine = true;
        private final float[] buffer = new float[19];
        private final double[] doubles = new double[8];
        private Layer layer;

        private DirectLineRenderer() {
        }

        @Override
        public void update(GLViewport v) {
            buckets.clear();
            if (invalidLine || !layer.isEnabled()) {
                setReady(false);
                return;
            }

            mMapPosition.copy(v.pos);
            v.getMapExtents(buffer, mMapPosition.tilt > 0 ? 100f : 0f);

            doubles[0] = v.pos.x;
            doubles[1] = v.pos.y;
            doubles[2] = Tile.SIZE * v.pos.scale;

            buffer[8] = (float) ((doubles[3] - doubles[0]) * doubles[2]);
            buffer[9] = (float) ((doubles[4] - doubles[1]) * doubles[2]);

            buffer[10] = (float) ((doubles[5] - doubles[0]) * doubles[2]);
            buffer[11] = (float) ((doubles[6] - doubles[1]) * doubles[2]);

            if (MathUtils.clampLineToIntersectRect(buffer, 0, 8, 12, 16) == 0) return;

            buckets.set(ll);
            g.clear();
            g.startLine();
            g.addPoint(buffer[8], buffer[9]);
            g.addPoint(buffer[10], buffer[11]);
            ll.addLine(g);
            compile();
        }

        public void setInvalid() {
            this.invalidLine = true;
        }

        public void setLine(Coordinate selectedCoordinate, Coordinate ownPosition) {
            if (!layer.isEnabled()) return;
            doubles[3] = (ownPosition.longitude + 180.0) / 360.0;
            doubles[7] = Math.sin(ownPosition.latitude * (Math.PI / 180.0));
            doubles[4] = 0.5 - Math.log((1.0 + doubles[7]) / (1.0 - doubles[7])) / (4.0 * Math.PI);

            doubles[5] = (selectedCoordinate.longitude + 180.0) / 360.0;
            doubles[7] = Math.sin(selectedCoordinate.latitude * (Math.PI / 180.0));
            doubles[6] = 0.5 - Math.log((1.0 + doubles[7]) / (1.0 - doubles[7])) / (4.0 * Math.PI);
            this.invalidLine = false;
        }

        public void setLayer(DirectLineLayer layer) {
            this.layer = layer;
        }

        public void dispose() {
            this.layer = null;
        }
    }
}
