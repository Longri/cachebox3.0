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
import com.kotcrab.vis.ui.VisUI;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.gui.skin.styles.DirectLineRendererStyle;
import de.longri.cachebox3.locator.Coordinate;
import de.longri.cachebox3.locator.LatLong;
import de.longri.cachebox3.utils.MathUtils;
import org.oscim.backend.canvas.Color;
import org.oscim.core.GeometryBuffer;
import org.oscim.core.Tile;
import org.oscim.layers.GenericLayer;
import org.oscim.layers.Layer;
import org.oscim.map.Map;
import org.oscim.renderer.BucketRenderer;
import org.oscim.renderer.GLViewport;
import org.oscim.renderer.MapRenderer;
import org.oscim.renderer.bucket.LineBucket;
import org.oscim.renderer.bucket.TextureItem;
import org.oscim.theme.styles.LineStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.oscim.theme.styles.LineStyle.REPEAT_GAP_DEFAULT;
import static org.oscim.theme.styles.LineStyle.REPEAT_START_DEFAULT;

/**
 * Created by Longri on 02.03.2017.
 */
public class DirectLineLayer extends GenericLayer implements Disposable {

    private final static Logger log = LoggerFactory.getLogger(DirectLineLayer.class);
    private final static short MAX_VALUE = (short) (Short.MAX_VALUE / MapRenderer.COORD_SCALE);

    private final DirectLineRenderer directLineRenderer;

    /**
     * @param map ...
     */
    public DirectLineLayer(Map map) {
        super(map, new DirectLineRenderer());
        directLineRenderer = (DirectLineRenderer) mRenderer;
        directLineRenderer.setLayer(this);
        de.longri.cachebox3.events.EventHandler.add(this);
    }


    public void redrawLine(LatLong ownPosition) {
        if (!this.isEnabled()) return;

        Coordinate selectedCoordinate = de.longri.cachebox3.events.EventHandler.getSelectedCoord();
        if (selectedCoordinate == null) {
            directLineRenderer.setInvalid();
            log.debug("Direct line are invalid");
            return;
        }

        if (ownPosition == null) {
            directLineRenderer.setInvalid();
            log.debug("Direct line are invalid");
            return;
        }

        log.debug("Redraw direct line");
        directLineRenderer.setLine(selectedCoordinate, ownPosition);
    }

    @Override
    public void dispose() {
        de.longri.cachebox3.events.EventHandler.remove(this);
        this.directLineRenderer.dispose();
    }

    private static class DirectLineRenderer extends BucketRenderer {
        DirectLineRendererStyle style = getStyle();

        private DirectLineRendererStyle getStyle() {
            try {
                style = VisUI.getSkin().get("directLine", DirectLineRendererStyle.class);
            } catch (Exception e) {
                style = new DirectLineRendererStyle();
            }
            return style;
        }

        TextureItem textureItem = style.texture == null ? null : new TextureItem(style.texture);
        LineBucket ll = buckets.addLineBucket(0,
                new LineStyle(0, "",
                        Color.get(style.color.a, (int) (style.color.r * 255), (int) (style.color.g * 255), (int) (style.color.b * 255)),
                        CB.getScaledFloat(style.width), style.cap, true, 1.0, 0, 0, 0,
                        -1, 0, false, textureItem, true, null, REPEAT_START_DEFAULT, REPEAT_GAP_DEFAULT)
        );

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
            if (invalidLine || layer == null || !layer.isEnabled()) {
                setReady(false);
                return;
            }

            mMapPosition.copy(v.pos);
            v.getMapExtents(buffer, 100f);
            for (int i = 0, n = 8; i < n; i++)
                MathUtils.clampToMinMax(buffer, i, MAX_VALUE);


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

            g.addPoint(buffer[10], buffer[11]);
            g.addPoint(buffer[8], buffer[9]);
            ll.addLine(g);
            compile();
        }

        public void setInvalid() {
            this.invalidLine = true;
        }

        public void setLine(LatLong selectedCoordinate, LatLong ownPosition) {
            if (!layer.isEnabled()) return;
            doubles[3] = (ownPosition.getLongitude() + 180.0) / 360.0;
            doubles[7] = Math.sin(ownPosition.getLatitude() * (Math.PI / 180.0));
            doubles[4] = 0.5 - Math.log((1.0 + doubles[7]) / (1.0 - doubles[7])) / (4.0 * Math.PI);

            doubles[5] = (selectedCoordinate.getLongitude() + 180.0) / 360.0;
            doubles[7] = Math.sin(selectedCoordinate.getLatitude() * (Math.PI / 180.0));
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
