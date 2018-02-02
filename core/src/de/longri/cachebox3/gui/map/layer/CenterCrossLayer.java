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
import de.longri.cachebox3.gui.skin.styles.MapCenterCrossStyle;
import org.oscim.backend.canvas.Paint;
import org.oscim.core.GeometryBuffer;
import org.oscim.layers.GenericLayer;
import org.oscim.map.Map;
import org.oscim.renderer.BucketRenderer;
import org.oscim.renderer.GLViewport;
import org.oscim.renderer.bucket.LineBucket;
import org.oscim.theme.styles.LineStyle;

/**
 * Created by Longri on 19.03.17.
 */
public class CenterCrossLayer extends GenericLayer implements Disposable {

    public CenterCrossLayer(Map map) {
        super(map, new Renderer());
        ((Renderer) this.mRenderer).setLayer(this);
    }

    @Override
    public void dispose() {
        ((Renderer) this.mRenderer).dispose();
    }

    private static class Renderer extends BucketRenderer {
        final MapCenterCrossStyle style = VisUI.getSkin().get("centerCross", MapCenterCrossStyle.class);
        final LineBucket ll = buckets.addLineBucket(0,
                new LineStyle(style.color.toIntBits(), CB.getScaledFloat(style.width), style.cap));

        final LineBucket lc = buckets.addLineBucket(0,
                new LineStyle(style.color.toIntBits(), CB.getScaledFloat(style.width), Paint.Cap.ROUND));

        final GeometryBuffer g1 = new GeometryBuffer(2, 1);
        final GeometryBuffer g2 = new GeometryBuffer(2, 1);
        final GeometryBuffer g3 = new GeometryBuffer(2, 1);
        final GeometryBuffer g4 = new GeometryBuffer(2, 1);
        final GeometryBuffer g5 = new GeometryBuffer(2, 1);

        private CenterCrossLayer centerCrossLayer;

        @Override
        public void update(GLViewport v) {
            buckets.clear();
            if (centerCrossLayer == null || !centerCrossLayer.isEnabled()) return;
            mMapPosition.copy(v.pos);
            buckets.set(ll);

            if (style.dotAtCenter) {
                buckets.set(lc);

                g1.clear();
                g1.startLine();
                g1.addPoint(-0.00001f, 0);
                g1.addPoint(0.00001f, 0);
                lc.addLine(g1);

                float dist = CB.getScaledFloat(style.width) * 4;
                float length = CB.getScaledFloat(style.length) - dist;

                g2.clear();
                g2.startLine();
                g2.addPoint(0, -length);
                g2.addPoint(0, -dist);
                ll.addLine(g2);

                g3.clear();
                g3.startLine();
                g3.addPoint(0, length);
                g3.addPoint(0, dist);
                ll.addLine(g3);

                g4.clear();
                g4.startLine();
                g4.addPoint(-length, 0);
                g4.addPoint(-dist, 0);
                ll.addLine(g4);

                g5.clear();
                g5.startLine();
                g5.addPoint(length, 0);
                g5.addPoint(dist, 0);
                ll.addLine(g5);

            } else {
                g1.clear();
                g1.startLine();
                g1.addPoint(-style.length, 0);
                g1.addPoint(style.length, 0);
                ll.addLine(g1);

                g2.clear();
                g2.startLine();
                g2.addPoint(0, -style.length);
                g2.addPoint(0, style.length);
                ll.addLine(g2);
            }

            compile();
        }

        public void setLayer(CenterCrossLayer centerCrossLayer) {
            this.centerCrossLayer = centerCrossLayer;
        }

        public void dispose() {
            centerCrossLayer = null;
        }
    }

}
