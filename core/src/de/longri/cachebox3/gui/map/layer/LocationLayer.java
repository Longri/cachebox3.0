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

import com.badlogic.gdx.utils.Disposable;
import de.longri.cachebox3.gui.map.layer.renderer.LocationRenderer;
import org.oscim.layers.Layer;
import org.oscim.map.Map;
import org.oscim.renderer.atlas.TextureRegion;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;

/**
 * Created by Longri on 14.02.17
 */
public class LocationLayer extends Layer implements Disposable {
    private final static org.slf4j.Logger log = LoggerFactory.getLogger(LocationLayer.class);

    private final de.longri.cachebox3.gui.map.layer.renderer.LocationRenderer locationRenderer;
    private final LinkedHashMap<Object, TextureRegion> textureRegionMap;

    public LocationLayer(Map map, LinkedHashMap<Object, TextureRegion> textureRegionMap) {
        super(map);
        mRenderer = locationRenderer = new LocationRenderer(map, this);
        this.textureRegionMap = textureRegionMap;
        setArrow(textureRegionMap.get("mapStar1"));
    }


    public void setArrow(TextureRegion region) {
        locationRenderer.setTextureRegion(region);
    }

    @Override
    public void dispose() {
        locationRenderer.dispose();
    }

    public void setPosition(double latitude, double longitude) {
        locationRenderer.update(latitude, longitude);
    }
}
