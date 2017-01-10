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
package de.longri.cachebox3.gui.map.layer;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Matrix4;
import org.oscim.core.MapPosition;
import org.oscim.core.Tile;
import org.oscim.map.Map;
import org.oscim.renderer.GLViewport;

public class MapCamera extends Camera {

    private final Map mMap;

    public MapCamera(Map map) {
        mMap = map;

        this.near = 1;
        this.far = 8;
    }

    public MapPosition mMapPosition = new MapPosition();

    public void setPosition(MapPosition pos) {
        mMapPosition.copy(pos);

        this.viewportWidth = mMap.getWidth();
        this.viewportHeight = mMap.getHeight();
    }

    public void setMapPosition(double x, double y, double scale) {
        mMapPosition.setScale(scale);
        mMapPosition.x = x;
        mMapPosition.y = y;
    }

    public void update(GLViewport v) {
        double scale = (v.pos.scale * Tile.SIZE);

        float x = (float) ((mMapPosition.x - v.pos.x) * scale);
        float y = (float) ((mMapPosition.y - v.pos.y) * scale);
        float z = (float) (v.pos.scale / mMapPosition.scale);

        v.proj.get(projection.getValues());
        v.mvp.setTransScale(x, y, z);
        v.mvp.setValue(10, z);
        v.mvp.multiplyLhs(v.view);
        v.mvp.get(view.getValues());

        combined.set(projection);

        Matrix4.mul(combined.val, view.val);

        //if (updateFrustum) {
        invProjectionView.set(combined);
        Matrix4.inv(invProjectionView.val);
        frustum.update(invProjectionView);
        //}
    }

    @Override
    public void update() {
    }

    @Override
    public void update(boolean updateFrustum) {

    }

}
