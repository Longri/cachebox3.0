/*
 * Copyright 2013 Ahmad Saleem
 * Copyright 2013 Hannes Janetzek
 * Copyright 2016 devemux86
 * Copyright 2017 - 2018 Longri
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


import com.badlogic.gdx.utils.Disposable;
import de.longri.cachebox3.gui.map.layer.renderer.LocationAccuracyRenderer;
import org.oscim.core.MercatorProjection;
import org.oscim.core.Point;
import org.oscim.layers.Layer;
import org.oscim.map.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LocationAccuracyLayer extends Layer implements Disposable {
    private final static Logger log = LoggerFactory.getLogger(LocationAccuracyLayer.class);

    private final Point location = new Point();
    private LocationAccuracyRenderer locationAccuracyRenderer;

    public LocationAccuracyLayer(Map map) {
        super(map);
        mRenderer = locationAccuracyRenderer = new LocationAccuracyRenderer(map, this);
    }

    public void setPosition(double latitude, double longitude, double accuracy) {
        location.x = MercatorProjection.longitudeToX(longitude);
        location.y = MercatorProjection.latitudeToY(latitude);
        locationAccuracyRenderer.setLocation(location.x, location.y, accuracy);
        log.debug("set x: {} y: {} radius; {}", location.x, location.y, accuracy);
    }

    @Override
    public void setEnabled(boolean enabled) {
        if (enabled == isEnabled()) return;
        super.setEnabled(enabled);
    }

    @Override
    public void dispose() {
        locationAccuracyRenderer.dispose();
        locationAccuracyRenderer = null;
        mRenderer = null;
    }

    public double getX() {
        return location.x;
    }

    public double getY() {
        return location.y;
    }
}
