/*
 * Copyright (C) 2019 team-cachebox.de
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
import org.oscim.core.MercatorProjection;
import org.oscim.layers.Layer;
import org.oscim.map.Map;
import org.oscim.renderer.atlas.TextureRegion;

/**
 * Created by Longri on 08.11.19.
 */
public class LocationTextureLayer extends Layer {
    public final LocationTextureRenderer locationRenderer;

    public LocationTextureLayer(Map map, TextureRegion textureRegion) {
        super(map);

        mRenderer = locationRenderer = new LocationTextureRenderer(map);
        locationRenderer.setTextureRegion(textureRegion);
    }

    @Override
    public void setEnabled(boolean enabled) {
        if (enabled == isEnabled())
            return;

        super.setEnabled(enabled);

        if (!enabled)
            locationRenderer.animate(false);
    }

    public void setPosition(double latitude, double longitude, float bearing, float accuracy) {
        double x = MercatorProjection.longitudeToX(longitude);
        double y = MercatorProjection.latitudeToY(latitude);
        bearing = -bearing;
        while (bearing < 0)
            bearing += 360;
        double radius = accuracy / MercatorProjection.groundResolutionWithScale(latitude, 1);
        locationRenderer.setLocation(x, y, bearing, radius);
        locationRenderer.animate(true);
    }
}
