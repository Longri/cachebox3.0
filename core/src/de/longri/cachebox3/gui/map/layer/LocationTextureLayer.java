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

import de.longri.cachebox3.gui.map.layer.renderer.LocationTextureRenderer;
import org.oscim.layers.Layer;
import org.oscim.map.Map;
import org.oscim.renderer.atlas.TextureRegion;

/**
 * Created by Longri on 14.02.17
 */
public class LocationTextureLayer extends Layer {

    private final LocationTextureRenderer locationRenderer;

    public LocationTextureLayer(Map map, TextureRegion textureRegion) {
        super(map);
        mRenderer = locationRenderer = new LocationTextureRenderer(map);
        setTextureRegion(textureRegion);
    }


    public void setPosition(double latitude, double longitude, float heading, double accuracy) {
        locationRenderer.setPosition(latitude, longitude, heading, accuracy);
    }

    public void setAccuracyColor(int color) {
        locationRenderer.setAccuracyColor(color);
    }

    public void setIndicatorColor(int color) {
        locationRenderer.setIndicatorColor(color);
    }

    public void setTextureRegion(TextureRegion region) {
        locationRenderer.setTextureRegion(region);
    }

}
