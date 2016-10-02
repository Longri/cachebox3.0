/*
 * Copyright 2013 Ahmad Saleem
 * Copyright 2013 Hannes Janetzek
 * Copyright 2016 devemux86
 *
 * This program is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package de.longri.cachebox3.gui.map.layer;


import org.oscim.core.MercatorProjection;
import org.oscim.core.Point;
import org.oscim.layers.Layer;
import org.oscim.map.Map;

public class LocationOverlay extends Layer {
       private final Point mLocation = new Point();
      private double mRadius;
    private LocationRenderer locationRenderer;

    private final Compass mCompass;

    public LocationOverlay(Map map, Compass compass) {
        super(map);
        mRenderer = locationRenderer= new LocationRenderer(map,this);
        mCompass = compass;
        locationRenderer.setCallback(new LocationRenderer.Callback() {
            @Override
            public float getRotation() {
                return mCompass.getRotation();
            }
        });
    }

    public void setPosition(double latitude, double longitude, double accuracy) {
        mLocation.x = MercatorProjection.longitudeToX(longitude);
        mLocation.y = MercatorProjection.latitudeToY(latitude);
        mRadius = accuracy / MercatorProjection.groundResolution(latitude, 1);
             locationRenderer.setLocation(mLocation.x,mLocation.y,mRadius);
    }

    @Override
    public void setEnabled(boolean enabled) {
        if (enabled == isEnabled())
            return;

        super.setEnabled(enabled);
               mCompass.setEnabled(enabled);
    }

}
